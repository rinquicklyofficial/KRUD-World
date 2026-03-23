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

package dev.krud.world.engine.framework;

import dev.krud.world.engine.data.chunk.TerrainChunk;
import dev.krud.world.util.data.B;
import dev.krud.world.util.hunk.Hunk;
import lombok.Data;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class PregeneratedData {
    private final Hunk<BlockData> blocks;
    private final Hunk<BlockData> post;
    private final Hunk<Biome> biomes;
    private final AtomicBoolean postMod;

    public PregeneratedData(int height) {
        postMod = new AtomicBoolean(false);
        blocks = Hunk.newAtomicHunk(16, height, 16);
        biomes = Hunk.newAtomicHunk(16, height, 16);
        Hunk<BlockData> p = Hunk.newMappedHunkSynced(16, height, 16);
        post = p.trackWrite(postMod);
    }

    public Runnable inject(TerrainChunk tc) {
        blocks.iterateSync((x, y, z, b) -> {
            if (b != null) {
                tc.setBlock(x, y, z, b);
            }

            Biome bf = biomes.get(x, y, z);
            if (bf != null) {
                tc.setBiome(x, y, z, bf);
            }
        });

        if (postMod.get()) {
            return () -> Hunk.view(tc).insertSoftly(0, 0, 0, post, (b) -> b == null || B.isAirOrFluid(b));
        }

        return () -> {
        };
    }
}
