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

package dev.krud.world.engine.platform;

import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class DummyBiomeGrid implements ChunkGenerator.BiomeGrid {
    @NotNull
    @Override
    public Biome getBiome(int x, int z) {
        return null;
    }

    @NotNull
    @Override
    public Biome getBiome(int x, int y, int z) {
        return null;
    }

    @Override
    public void setBiome(int x, int z, @NotNull Biome bio) {

    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome bio) {

    }
}
