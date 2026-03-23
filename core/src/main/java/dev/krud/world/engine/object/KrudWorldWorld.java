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

package dev.krud.world.engine.object;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.util.collection.KList;
import lombok.*;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.util.Collection;
import java.util.List;

@Builder
@Data
@Accessors(chain = true, fluent = true)
public class KrudWorldWorld {
    private static final KList<Player> NO_PLAYERS = new KList<>();
    private static final KList<? extends Entity> NO_ENTITIES = new KList<>();
    private String name;
    private File worldFolder;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long seed;
    private World.Environment environment;
    private World realWorld;
    private int minHeight;
    private int maxHeight;

    public static KrudWorldWorld fromWorld(World world) {
        return bindWorld(KrudWorldWorld.builder().build(), world);
    }

    private static KrudWorldWorld bindWorld(KrudWorldWorld iw, World world) {
        return iw.name(world.getName())
                .worldFolder(world.getWorldFolder())
                .minHeight(world.getMinHeight())
                .maxHeight(world.getMaxHeight())
                .realWorld(world)
                .environment(world.getEnvironment());
    }

    public long getRawWorldSeed() {
        return seed;
    }

    public void setRawWorldSeed(long seed) {
        this.seed = seed;
    }

    public boolean tryGetRealWorld() {
        if (hasRealWorld()) {
            return true;
        }

        World w = Bukkit.getWorld(name);

        if (w != null) {
            realWorld = w;
            return true;
        }

        return false;
    }

    public boolean hasRealWorld() {
        return realWorld != null;
    }

    public List<Player> getPlayers() {

        if (hasRealWorld()) {
            return realWorld().getPlayers();
        }

        return NO_PLAYERS;
    }

    public void evacuate() {
        if (hasRealWorld()) {
            KrudWorldToolbelt.evacuate(realWorld());
        }
    }

    public void bind(WorldInfo worldInfo) {
        name(worldInfo.getName())
                .worldFolder(new File(Bukkit.getWorldContainer(), worldInfo.getName()))
                .minHeight(worldInfo.getMinHeight())
                .maxHeight(worldInfo.getMaxHeight())
                .environment(worldInfo.getEnvironment());
    }

    public void bind(World world) {
        if (hasRealWorld()) {
            return;
        }

        bindWorld(this, world);
    }

    public Location spawnLocation() {
        if (hasRealWorld()) {
            return realWorld().getSpawnLocation();
        }

        KrudWorld.error("This world is not real yet, cannot get spawn location! HEADLESS!");
        return null;
    }

    public <T extends Entity> Collection<? extends T> getEntitiesByClass(Class<T> t) {
        if (hasRealWorld()) {
            return realWorld().getEntitiesByClass(t);
        }

        return (KList<? extends T>) NO_ENTITIES;
    }

    public int getHeight() {
        return maxHeight - minHeight;
    }
}
