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

package dev.krud.world.engine.decorator;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldDecorationPart;
import dev.krud.world.engine.object.KrudWorldDecorator;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.math.RNG;
import org.bukkit.block.data.BlockData;

public class KrudWorldShoreLineDecorator extends KrudWorldEngineDecorator {
    public KrudWorldShoreLineDecorator(Engine engine) {
        super(engine, "Shore Line", KrudWorldDecorationPart.SHORE_LINE);
    }

    @BlockCoordinates
    @Override
    public void decorate(int x, int z, int realX, int realX1, int realX_1, int realZ, int realZ1, int realZ_1, Hunk<BlockData> data, KrudWorldBiome biome, int height, int max) {

        if (height == getDimension().getFluidHeight()) {
            if (Math.round(getComplex().getHeightStream().get(realX1, realZ)) < getComplex().getFluidHeight() ||
                    Math.round(getComplex().getHeightStream().get(realX_1, realZ)) < getComplex().getFluidHeight() ||
                    Math.round(getComplex().getHeightStream().get(realX, realZ1)) < getComplex().getFluidHeight() ||
                    Math.round(getComplex().getHeightStream().get(realX, realZ_1)) < getComplex().getFluidHeight()
            ) {
                RNG rng = getRNG(realX, realZ);
                KrudWorldDecorator decorator = getDecorator(rng, biome, realX, realZ);

                if (decorator != null) {
                    if (!decorator.isForcePlace() && !decorator.getSlopeCondition().isDefault()
                            && !decorator.getSlopeCondition().isValid(getComplex().getSlopeStream().get(realX, realZ))) {
                        return;
                    }

                    if (!decorator.isStacking()) {
                        data.set(x, height + 1, z, decorator.getBlockData100(biome, rng, realX, height, realZ, getData()));
                    } else {
                        int stack = decorator.getHeight(rng, realX, realZ, getData());
                        if (decorator.isScaleStack()) {
                            int maxStack = max - height;
                            stack = (int) Math.ceil((double) maxStack * ((double) stack / 100));
                        } else stack = Math.min(max - height, stack);

                        if (stack == 1) {
                            data.set(x, height, z, decorator.getBlockDataForTop(biome, rng, realX, height, realZ, getData()));
                            return;
                        }

                        for (int i = 0; i < stack; i++) {
                            int h = height + i;
                            double threshold = ((double) i) / (stack - 1);
                            data.set(x, h + 1, z, threshold >= decorator.getTopThreshold() ?
                                    decorator.getBlockDataForTop(biome, rng, realX, h, realZ, getData()) :
                                    decorator.getBlockData100(biome, rng, realX, h, realZ, getData()));
                        }
                    }
                }
            }
        }
    }
}
