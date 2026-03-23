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

package dev.krud.world.engine.modifier;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedModifier;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.data.B;
import dev.krud.world.util.data.HeightMap;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.mantle.MantleChunk;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

public class KrudWorldDepositModifier extends EngineAssignedModifier<BlockData> {
    private final RNG rng;

    public KrudWorldDepositModifier(Engine engine) {
        super(engine, "Deposit");
        rng = new RNG(getEngine().getSeedManager().getDeposit());
    }

    @Override
    public void onModify(int x, int z, Hunk<BlockData> output, boolean multicore, ChunkContext context) {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        generateDeposits(output, Math.floorDiv(x, 16), Math.floorDiv(z, 16), multicore, context);
        getEngine().getMetrics().getDeposit().put(p.getMilliseconds());
    }

    public void generateDeposits(Hunk<BlockData> terrain, int x, int z, boolean multicore, ChunkContext context) {
        KrudWorldRegion region = context.getRegion().get(7, 7);
        KrudWorldBiome biome = context.getBiome().get(7, 7);
        BurstExecutor burst = burst().burst(multicore);

        long seed = x * 341873128712L + z * 132897987541L;
        long mask = 0;
        MantleChunk chunk = getEngine().getMantle().getMantle().getChunk(x, z).use();
        for (KrudWorldDepositGenerator k : getDimension().getDeposits()) {
            long finalSeed = seed * ++mask;
            burst.queue(() -> generate(k, chunk, terrain, rng.nextParallelRNG(finalSeed), x, z, false, context));
        }

        for (KrudWorldDepositGenerator k : region.getDeposits()) {
            long finalSeed = seed * ++mask;
            burst.queue(() -> generate(k, chunk, terrain, rng.nextParallelRNG(finalSeed), x, z, false, context));
        }

        for (KrudWorldDepositGenerator k : biome.getDeposits()) {
            long finalSeed = seed * ++mask;
            burst.queue(() -> generate(k, chunk, terrain, rng.nextParallelRNG(finalSeed), x, z, false, context));
        }
        burst.complete();
        chunk.release();
    }

    public void generate(KrudWorldDepositGenerator k, MantleChunk chunk, Hunk<BlockData> data, RNG rng, int cx, int cz, boolean safe, ChunkContext context) {
        generate(k, chunk, data, rng, cx, cz, safe, null, context);
    }

    public void generate(KrudWorldDepositGenerator k, MantleChunk chunk, Hunk<BlockData> data, RNG rng, int cx, int cz, boolean safe, HeightMap he, ChunkContext context) {
        if (k.getSpawnChance() < rng.d())
            return;

        for (int l = 0; l < rng.i(k.getMinPerChunk(), k.getMaxPerChunk() + 1); l++) {
            if (k.getPerClumpSpawnChance() < rng.d())
                continue;

            KrudWorldObject clump = k.getClump(getEngine(), rng, getData());

            int dim = clump.getW();
            int min = dim / 2;
            int max = (int) (16D - dim / 2D);

            if (min > max || min < 0 || max > 15) {
                min = 6;
                max = 9;
            }

            int x = rng.i(min, max + 1);
            int z = rng.i(min, max + 1);
            int height = (he != null ? he.getHeight((cx << 4) + x, (cz << 4) + z) : (int) (Math.round(
                    context.getHeight().get(x, z)
            ))) - 7;

            if (height <= 0)
                continue;

            int minY = Math.max(0, k.getMinHeight());
            // TODO: WARNING HEIGHT
            int maxY = Math.min(height, Math.min(getEngine().getHeight(), k.getMaxHeight()));

            if (minY >= maxY)
                continue;

            int y = rng.i(minY, maxY + 1);

            if (y > k.getMaxHeight() || y < k.getMinHeight() || y > height - 2)
                continue;

            for (BlockVector j : clump.getBlocks().keys()) {
                int nx = j.getBlockX() + x;
                int ny = j.getBlockY() + y;
                int nz = j.getBlockZ() + z;

                if (ny > height || nx > 15 || nx < 0 || ny > getEngine().getHeight() || ny < 0 || nz < 0 || nz > 15) {
                    continue;
                }
                if (!k.isReplaceBedrock() && data.get(nx, ny, nz).getMaterial() == Material.BEDROCK) {
                    continue;
                }

                if (chunk.get(nx, ny, nz, MatterCavern.class) == null) {
                    data.set(nx, ny, nz, B.toDeepSlateOre(data.get(nx, ny, nz), clump.getBlocks().get(j)));
                }
            }
        }
    }
}
