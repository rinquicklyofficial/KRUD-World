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

package dev.krud.world.core.pregenerator.methods;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.pregenerator.PregenListener;
import dev.krud.world.core.pregenerator.PregeneratorMethod;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.math.M;
import dev.krud.world.util.scheduling.J;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MedievalPregenMethod implements PregeneratorMethod {
    private final World world;
    private final KList<CompletableFuture<?>> futures;
    private final Map<Chunk, Long> lastUse;

    public MedievalPregenMethod(World world) {
        this.world = world;
        futures = new KList<>();
        this.lastUse = new KMap<>();
    }

    private void waitForChunks() {
        for (CompletableFuture<?> i : futures) {
            try {
                i.get();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        futures.clear();
    }

    private void unloadAndSaveAllChunks() {
        try {
            J.sfut(() -> {
                if (world == null) {
                    KrudWorld.warn("World was null somehow...");
                    return;
                }

                for (Chunk i : new ArrayList<>(lastUse.keySet())) {
                    Long lastUseTime = lastUse.get(i);
                    if (lastUseTime != null && M.ms() - lastUseTime >= 10) {
                        i.unload();
                        lastUse.remove(i);
                    }
                }
                world.save();
            }).get();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        unloadAndSaveAllChunks();
    }

    @Override
    public void close() {
        unloadAndSaveAllChunks();
    }

    @Override
    public void save() {
        unloadAndSaveAllChunks();
    }

    @Override
    public boolean supportsRegions(int x, int z, PregenListener listener) {
        return false;
    }

    @Override
    public void generateRegion(int x, int z, PregenListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMethod(int x, int z) {
        return "Medieval";
    }

    @Override
    public void generateChunk(int x, int z, PregenListener listener) {
        if (futures.size() > KrudWorldSettings.getThreadCount(KrudWorldSettings.get().getConcurrency().getParallelism())) {
            waitForChunks();
        }

        listener.onChunkGenerating(x, z);
        futures.add(J.sfut(() -> {
            world.getChunkAt(x, z);
            Chunk c = Bukkit.getWorld(world.getUID()).getChunkAt(x, z);
            lastUse.put(c, M.ms());
            listener.onChunkGenerated(x, z);
            listener.onChunkCleaned(x, z);
        }));
    }

    @Override
    public Mantle getMantle() {
        if (KrudWorldToolbelt.isKrudWorldWorld(world)) {
            return KrudWorldToolbelt.access(world).getEngine().getMantle().getMantle();
        }

        return null;
    }
}
