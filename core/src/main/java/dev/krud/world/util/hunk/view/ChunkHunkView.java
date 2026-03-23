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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.service.EditSVC;
import dev.krud.world.util.hunk.Hunk;
import org.bukkit.Chunk;
import org.bukkit.block.data.BlockData;

@SuppressWarnings("ClassCanBeRecord")
public class ChunkHunkView implements Hunk<BlockData> {
    private final Chunk chunk;

    public ChunkHunkView(Chunk chunk) {
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
        return chunk.getWorld().getMaxHeight();
    }

    @Override
    public void setRaw(int x, int y, int z, BlockData t) {
        if (t == null) {
            return;
        }

        KrudWorld.service(EditSVC.class).set(chunk.getWorld(), x + (chunk.getX() * 16), y, z + (chunk.getZ() * 16), t);
    }

    @Override
    public BlockData getRaw(int x, int y, int z) {
        return KrudWorld.service(EditSVC.class).get(chunk.getWorld(), x + (chunk.getX() * 16), y, z + (chunk.getZ() * 16));
    }
}
