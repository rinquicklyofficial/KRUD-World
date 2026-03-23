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
import dev.krud.world.core.link.Identifier;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedModifier;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.data.KrudWorldCustomData;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.parallel.MultiBurst;
import org.bukkit.block.data.BlockData;
public class KrudWorldCustomModifier extends EngineAssignedModifier<BlockData> {
    public KrudWorldCustomModifier(Engine engine) {
        super(engine, "Custom");
    }
    @Override
    public void onModify(int x, int z, Hunk<BlockData> output, boolean multicore, ChunkContext context) {
        var mc = getEngine().getMantle().getMantle().getChunk(x >> 4, z >> 4);
        if (!mc.isFlagged(MantleFlag.CUSTOM_ACTIVE)) return;
        mc.use();

        BurstExecutor burst = MultiBurst.burst.burst(output.getHeight());
        burst.setMulticore(multicore);
        for (int y = 0; y < output.getHeight(); y++) {
            int finalY = y;
            burst.queue(() -> {
                for (int rX = 0; rX < output.getWidth(); rX++) {
                    for (int rZ = 0; rZ < output.getDepth(); rZ++) {
                        BlockData b = output.get(rX, finalY, rZ);
                        if (!(b instanceof KrudWorldCustomData d)) continue;

                        mc.getOrCreate(finalY >> 4)
                                .slice(Identifier.class)
                                .set(rX, finalY & 15, rZ, d.getCustom());
                        output.set(rX, finalY, rZ, d.getBase());
                    }
                }
            });
        }
        burst.complete();
        mc.release();
    }
}