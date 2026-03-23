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

import dev.krud.world.engine.decorator.*;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedActuator;
import dev.krud.world.engine.framework.EngineDecorator;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.function.Predicate;

public class KrudWorldDecorantActuator extends EngineAssignedActuator<BlockData> {
    private static final Predicate<BlockData> PREDICATE_SOLID = (b) -> b != null && !b.getMaterial().isAir() && !b.getMaterial().equals(Material.WATER) && !b.getMaterial().equals(Material.LAVA);
    private final RNG rng;
    @Getter
    private final EngineDecorator surfaceDecorator;
    @Getter
    private final EngineDecorator ceilingDecorator;
    @Getter
    private final EngineDecorator seaSurfaceDecorator;
    @Getter
    private final EngineDecorator seaFloorDecorator;
    @Getter
    private final EngineDecorator shoreLineDecorator;
    private final boolean shouldRay;

    public KrudWorldDecorantActuator(Engine engine) {
        super(engine, "Decorant");
        shouldRay = shouldRayDecorate();
        this.rng = new RNG(engine.getSeedManager().getDecorator());
        surfaceDecorator = new KrudWorldSurfaceDecorator(getEngine());
        ceilingDecorator = new KrudWorldCeilingDecorator(getEngine());
        seaSurfaceDecorator = new KrudWorldSeaSurfaceDecorator(getEngine());
        shoreLineDecorator = new KrudWorldShoreLineDecorator(getEngine());
        seaFloorDecorator = new KrudWorldSeaFloorDecorator(getEngine());
    }

    @BlockCoordinates
    @Override
    public void onActuate(int x, int z, Hunk<BlockData> output, boolean multicore, ChunkContext context) {
        if (!getEngine().getDimension().isDecorate()) {
            return;
        }

        PrecisionStopwatch p = PrecisionStopwatch.start();

        for (int i = 0; i < output.getWidth(); i++) {
            int height;
            int realX = Math.round(x + i);
            int realZ;
            KrudWorldBiome biome, cave;
            for (int j = 0; j < output.getDepth(); j++) {
                boolean solid;
                int emptyFor = 0;
                int lastSolid = 0;
                realZ = Math.round(z + j);
                height = (int) Math.round(context.getHeight().get(i, j));
                biome = context.getBiome().get(i, j);
                cave = shouldRay ? context.getCave().get(i, j) : null;

                if (biome.getDecorators().isEmpty() && (cave == null || cave.getDecorators().isEmpty())) {
                    continue;
                }

                if (height < getDimension().getFluidHeight()) {
                    getSeaSurfaceDecorator().decorate(i, j,
                            realX, Math.round(i + 1), Math.round(x + i - 1),
                            realZ, Math.round(z + j + 1), Math.round(z + j - 1),
                            output, biome, getDimension().getFluidHeight(), getEngine().getHeight());
                    getSeaFloorDecorator().decorate(i, j,
                            realX, realZ, output, biome, height + 1,
                            getDimension().getFluidHeight() + 1);
                }

                if (height == getDimension().getFluidHeight()) {
                    getShoreLineDecorator().decorate(i, j,
                            realX, Math.round(x + i + 1), Math.round(x + i - 1),
                            realZ, Math.round(z + j + 1), Math.round(z + j - 1),
                            output, biome, height, getEngine().getHeight());
                }

                getSurfaceDecorator().decorate(i, j, realX, realZ, output, biome, height, getEngine().getHeight() - height);


                if (cave != null && cave.getDecorators().isNotEmpty()) {
                    for (int k = height; k > 0; k--) {
                        solid = PREDICATE_SOLID.test(output.get(i, k, j));

                        if (solid) {
                            if (emptyFor > 0) {
                                getSurfaceDecorator().decorate(i, j, realX, realZ, output, cave, k, lastSolid);
                                getCeilingDecorator().decorate(i, j, realX, realZ, output, cave, lastSolid - 1, emptyFor);
                                emptyFor = 0;
                            }
                            lastSolid = k;
                        } else {
                            emptyFor++;
                        }
                    }
                }
            }
        }

        getEngine().getMetrics().getDecoration().put(p.getMilliseconds());

    }

    private boolean shouldRayDecorate() {
        return false; // TODO CAVES
    }
}
