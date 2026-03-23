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

import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

import java.io.Closeable;

public interface BlockEditor extends Closeable {
    long last();

    void set(int x, int y, int z, BlockData d);

    BlockData get(int x, int y, int z);

    void setBiome(int x, int z, Biome b);

    void setBiome(int x, int y, int z, Biome b);

    @Override
    void close();

    Biome getBiome(int x, int y, int z);

    Biome getBiome(int x, int z);
}
