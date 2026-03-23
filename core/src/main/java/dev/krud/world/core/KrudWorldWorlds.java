/**
 * KRUD World — World Generator
 * Copyright (C) 2026 Krud Studio
 *
 * Based on KrudWorld World Generator:
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 * https://github.com/VolmitSoftware/KrudWorld
 * License: GPL-3.0
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License.
 */

package dev.krud.world.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.misc.ServerProperties;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.stream.Stream;

public class KrudWorldWorlds {
    private static final AtomicCache<KrudWorldWorlds> cache = new AtomicCache<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = TypeToken.getParameterized(KMap.class, String.class, String.class).getType();
    private final KMap<String, String> worlds;
    private volatile boolean dirty = false;

    private KrudWorldWorlds(KMap<String, String> worlds) {
        this.worlds = worlds;
        readBukkitWorlds().forEach(this::put0);
        save();
    }

    public static KrudWorldWorlds get() {
        return cache.aquire(() -> {
            File file = KrudWorld.instance.getDataFile("worlds.json");
            if (!file.exists()) {
                return new KrudWorldWorlds(new KMap<>());
            }

            try {
                String json = IO.readAll(file);
                KMap<String, String> worlds = GSON.fromJson(json, TYPE);
                return new KrudWorldWorlds(Objects.requireNonNullElseGet(worlds, KMap::new));
            } catch (Throwable e) {
                KrudWorld.error("Failed to load worlds.json!");
                e.printStackTrace();
                KrudWorld.reportError(e);
            }

            return new KrudWorldWorlds(new KMap<>());
        });
    }

    public void put(String name, String type) {
        put0(name, type);
        save();
    }

    private void put0(String name, String type) {
        String old = worlds.put(name, type);
        if (!type.equals(old))
            dirty = true;
    }

    public KMap<String, String> getWorlds() {
        clean();
        return readBukkitWorlds().put(worlds);
    }

    public Stream<KrudWorldData> getPacks() {
        return getDimensions()
                .map(KrudWorldDimension::getLoader)
                .filter(Objects::nonNull);
    }

    public Stream<KrudWorldDimension> getDimensions() {
        return getWorlds()
                .entrySet()
                .stream()
                .map(entry -> KrudWorld.loadDimension(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull);
    }

    public void clean() {
        dirty = worlds.entrySet().removeIf(entry -> !new File(Bukkit.getWorldContainer(), entry.getKey() + "/iris/pack/dimensions/" + entry.getValue() + ".json").exists());
    }

    public synchronized void save() {
        clean();
        if (!dirty) return;
        try {
            IO.write(KrudWorld.instance.getDataFile("worlds.json"), OutputStreamWriter::new, writer -> GSON.toJson(worlds, TYPE, writer));
            dirty = false;
        } catch (IOException e) {
            KrudWorld.error("Failed to save worlds.json!");
            e.printStackTrace();
            KrudWorld.reportError(e);
        }
    }

    public static KMap<String, String> readBukkitWorlds() {
        var bukkit = YamlConfiguration.loadConfiguration(ServerProperties.BUKKIT_YML);
        var worlds = bukkit.getConfigurationSection("worlds");
        if (worlds == null) return new KMap<>();

        var result = new KMap<String, String>();
        for (String world : worlds.getKeys(false)) {
            var gen = worlds.getString(world + ".generator");
            if (gen == null) continue;

            String loadKey;
            if (gen.equalsIgnoreCase("iris")) {
                loadKey = KrudWorldSettings.get().getGenerator().getDefaultWorldType();
            } else if (gen.startsWith("KrudWorld:")) {
                loadKey = gen.substring(5);
            } else continue;

            result.put(world, loadKey);
        }

        return result;
    }
}
