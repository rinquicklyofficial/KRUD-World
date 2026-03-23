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

package dev.krud.world.core.edit;

import dev.krud.world.util.math.M;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

@SuppressWarnings("ClassCanBeRecord")
public class BukkitBlockEditor implements BlockEditor {
    private final World world;

    public BukkitBlockEditor(World world) {
        this.world = world;
    }

    @Override
    public void set(int x, int y, int z, BlockData d) {
        world.getBlockAt(x, y, z).setBlockData(d, false);
    }

    @Override
    public BlockData get(int x, int y, int z) {
        return world.getBlockAt(x, y, z).getBlockData();
    }

    @Override
    public void close() {

    }

    @Override
    public long last() {
        return M.ms();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBiome(int x, int z, Biome b) {
        world.setBiome(x, z, b);
    }

    @Override
    public void setBiome(int x, int y, int z, Biome b) {
        world.setBiome(x, y, z, b);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return world.getBiome(x, y, z);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Biome getBiome(int x, int z) {
        return world.getBiome(x, z);
    }
}
