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

package dev.krud.world.util.data;

import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public class InvertedBiomeGrid implements BiomeGrid {
    private final BiomeGrid grid;

    public InvertedBiomeGrid(BiomeGrid real) {
        this.grid = real;
    }


    @SuppressWarnings("deprecation")
    @Override
    public Biome getBiome(int arg0, int arg1) {
        return grid.getBiome(arg0, arg1);
    }


    @Override
    public Biome getBiome(int arg0, int arg1, int arg2) {
        return grid.getBiome(arg0, 255 - arg1, arg2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBiome(int arg0, int arg1, Biome arg2) {
        grid.setBiome(arg0, arg1, arg2);
    }

    @Override
    public void setBiome(int arg0, int arg1, int arg2, Biome arg3) {
        grid.setBiome(arg0, 255 - arg1, arg2, arg3);
    }
}
