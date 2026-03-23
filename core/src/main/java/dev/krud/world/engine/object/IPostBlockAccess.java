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

import dev.krud.world.util.collection.KList;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public interface IPostBlockAccess {
    BlockData getPostBlock(int x, int y, int z, int currentPostX, int currentPostZ, ChunkData currentData);

    void setPostBlock(int x, int y, int z, BlockData d, int currentPostX, int currentPostZ, ChunkData currentData);

    int highestTerrainOrFluidBlock(int x, int z);

    int highestTerrainBlock(int x, int z);

    void updateHeight(int x, int z, int h);

    KList<CaveResult> caveFloors(int x, int z);
}
