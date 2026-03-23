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

package dev.krud.world.util.hunk.view;

import dev.krud.world.util.hunk.storage.AtomicHunk;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;

@SuppressWarnings("ClassCanBeRecord")
public class ChunkDataHunkHolder extends AtomicHunk<BlockData> {
    private static final BlockData AIR = Material.AIR.createBlockData();
    private final ChunkData chunk;

    public ChunkDataHunkHolder(ChunkData chunk) {
        super(16, chunk.getMaxHeight() - chunk.getMinHeight(), 16);
        this.chunk = chunk;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getDepth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return chunk.getMaxHeight() - chunk.getMinHeight();
    }

    @Override
    public BlockData getRaw(int x, int y, int z) {
        BlockData b = super.getRaw(x, y, z);

        return b != null ? b : AIR;
    }

    public void apply() {
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                for (int k = 0; k < getDepth(); k++) {
                    BlockData b = super.getRaw(j, i, k);

                    if (b != null) {
                        chunk.setBlock(j, i + chunk.getMinHeight(), k, b);
                    }
                }
            }
        }
    }
}
