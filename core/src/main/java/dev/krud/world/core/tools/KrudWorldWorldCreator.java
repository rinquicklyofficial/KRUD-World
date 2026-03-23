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

package dev.krud.world.core.tools;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.*;
import dev.krud.world.engine.platform.BukkitChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;

public class KrudWorldWorldCreator {
    private String name;
    private boolean studio = false;
    private String dimensionName = null;
    private long seed = 1337;

    public KrudWorldWorldCreator() {

    }

    public KrudWorldWorldCreator dimension(String loadKey) {
        this.dimensionName = loadKey;
        return this;
    }

    public KrudWorldWorldCreator name(String name) {
        this.name = name;
        return this;
    }

    public KrudWorldWorldCreator seed(long seed) {
        this.seed = seed;
        return this;
    }

    public KrudWorldWorldCreator studioMode() {
        this.studio = true;
        return this;
    }

    public KrudWorldWorldCreator productionMode() {
        this.studio = false;
        return this;
    }

    public WorldCreator create() {
        KrudWorldDimension dim = KrudWorldData.loadAnyDimension(dimensionName, null);

        KrudWorldWorld w = KrudWorldWorld.builder()
                .name(name)
                .minHeight(dim.getMinHeight())
                .maxHeight(dim.getMaxHeight())
                .seed(seed)
                .worldFolder(new File(Bukkit.getWorldContainer(), name))
                .environment(findEnvironment())
                .build();
        ChunkGenerator g = new BukkitChunkGenerator(w, studio, studio
                ? dim.getLoader().getDataFolder() :
                new File(w.worldFolder(), "iris/pack"), dimensionName);


        return new WorldCreator(name)
                .environment(w.environment())
                .generateStructures(true)
                .generator(g).seed(seed);
    }

    private World.Environment findEnvironment() {
        KrudWorldDimension dim = KrudWorldData.loadAnyDimension(dimensionName, null);
        if (dim == null || dim.getEnvironment() == null) {
            return World.Environment.NORMAL;
        } else {
            return dim.getEnvironment();
        }
    }

    public KrudWorldWorldCreator studio(boolean studio) {
        this.studio = studio;
        return this;
    }
}
