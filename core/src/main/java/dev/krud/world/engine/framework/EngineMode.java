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

import dev.krud.world.engine.KrudWorldComplex;
import dev.krud.world.engine.mantle.EngineMantle;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.context.KrudWorldContext;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.math.RollingSequence;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.parallel.MultiBurst;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

public interface EngineMode extends Staged {
    RollingSequence r = new RollingSequence(64);
    RollingSequence r2 = new RollingSequence(256);

    void close();

    Engine getEngine();

    default MultiBurst burst() {
        return getEngine().burst();
    }

    default EngineStage burst(EngineStage... stages) {
        return (x, z, blocks, biomes, multicore, ctx) -> {
            BurstExecutor e = burst().burst(stages.length);
            e.setMulticore(multicore);

            for (EngineStage i : stages) {
                e.queue(() -> i.generate(x, z, blocks, biomes, multicore, ctx));
            }

            e.complete();
        };
    }

    default KrudWorldComplex getComplex() {
        return getEngine().getComplex();
    }

    default EngineMantle getMantle() {
        return getEngine().getMantle();
    }

    default void generateMatter(int x, int z, boolean multicore, ChunkContext context) {
        getMantle().generateMatter(x, z, multicore, context);
    }

    @BlockCoordinates
    default void generate(int x, int z, Hunk<BlockData> blocks, Hunk<Biome> biomes, boolean multicore) {
        ChunkContext ctx = new ChunkContext(x, z, getComplex());
        KrudWorldContext.getOr(getEngine()).setChunkContext(ctx);

        for (EngineStage i : getStages()) {
            i.generate(x, z, blocks, biomes, multicore, ctx);
        }
    }
}
