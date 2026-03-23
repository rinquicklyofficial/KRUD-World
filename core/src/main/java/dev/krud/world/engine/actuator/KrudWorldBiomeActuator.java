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

package dev.krud.world.engine.actuator;

import dev.krud.world.core.nms.INMS;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedActuator;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldBiomeCustom;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterBiomeInject;
import dev.krud.world.util.matter.slices.BiomeInjectMatter;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import org.bukkit.block.Biome;

public class KrudWorldBiomeActuator extends EngineAssignedActuator<Biome> {
    private final RNG rng;
    private final ChronoLatch cl = new ChronoLatch(5000);

    public KrudWorldBiomeActuator(Engine engine) {
        super(engine, "Biome");
        rng = new RNG(engine.getSeedManager().getBiome());
    }

    @BlockCoordinates
    @Override
    public void onActuate(int x, int z, Hunk<Biome> h, boolean multicore, ChunkContext context) {
        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            for (int xf = 0; xf < h.getWidth(); xf++) {
                KrudWorldBiome ib;
                for (int zf = 0; zf < h.getDepth(); zf++) {
                    ib = context.getBiome().get(xf, zf);
                    MatterBiomeInject matter;

                    if (ib.isCustom()) {
                        KrudWorldBiomeCustom custom = ib.getCustomBiome(rng, x, 0, z);
                        matter = BiomeInjectMatter.get(INMS.get().getBiomeBaseIdForKey(getDimension().getLoadKey() + ":" + custom.getId()));
                    } else {
                        Biome v = ib.getSkyBiome(rng, x, 0, z);
                        matter = BiomeInjectMatter.get(v);
                    }

                    getEngine().getMantle().getMantle().set(x + xf, 0, z + zf, matter);
                }
            }
            getEngine().getMetrics().getBiome().put(p.getMilliseconds());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
