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
import org.bukkit.block.Biome;

@SuppressWarnings("ClassCanBeRecord")
public class ChunkBiomeHunkView implements Hunk<Biome> {
    private final Chunk chunk;

    public ChunkBiomeHunkView(Chunk chunk) {
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
    public void setRaw(int x, int y, int z, Biome t) {
        if (t == null) {
            return;
        }

        KrudWorld.service(EditSVC.class).setBiome(chunk.getWorld(), x + (chunk.getX() * 16), y, z + (chunk.getZ() * 16), t);
    }

    @Override
    public Biome getRaw(int x, int y, int z) {
        return KrudWorld.service(EditSVC.class)
                .getBiome(chunk.getWorld(), x + (chunk.getX() * 16), y, z + (chunk.getZ() * 16));
    }
}
