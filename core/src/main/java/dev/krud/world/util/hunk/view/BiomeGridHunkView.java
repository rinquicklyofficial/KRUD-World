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

import dev.krud.world.core.nms.INMS;
import dev.krud.world.engine.data.chunk.LinkedTerrainChunk;
import dev.krud.world.util.hunk.Hunk;
import lombok.Getter;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

@SuppressWarnings("ClassCanBeRecord")
public class BiomeGridHunkView implements Hunk<Biome> {
    @Getter
    private final BiomeGrid chunk;
    private final int minHeight;
    private final int maxHeight;
    private int highest = -1000;

    public BiomeGridHunkView(BiomeGrid chunk, int minHeight, int maxHeight) {
        this.chunk = chunk;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
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
        return maxHeight - minHeight;
    }

    @Override
    public void setRaw(int x, int y, int z, Biome t) {
        chunk.setBiome(x, y + minHeight, z, t);

        if (y > highest) {
            highest = y;
        }
    }

    @Override
    public Biome getRaw(int x, int y, int z) {
        return chunk.getBiome(x, y + minHeight, z);
    }

    public void forceBiomeBaseInto(int x, int y, int z, Object somethingVeryDirty) {
        if (chunk instanceof LinkedTerrainChunk) {
            INMS.get().forceBiomeInto(x, y + minHeight, z, somethingVeryDirty, ((LinkedTerrainChunk) chunk).getRawBiome());
            return;
        }
        INMS.get().forceBiomeInto(x, y + minHeight, z, somethingVeryDirty, chunk);
    }
}
