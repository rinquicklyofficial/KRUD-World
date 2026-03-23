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
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.PointedDripstone;

public class KrudWorldCeilingDecorator extends KrudWorldEngineDecorator {
    public KrudWorldCeilingDecorator(Engine engine) {
        super(engine, "Ceiling", KrudWorldDecorationPart.CEILING);
    }

    @BlockCoordinates
    @Override
    public void decorate(int x, int z, int realX, int realX1, int realX_1, int realZ, int realZ1, int realZ_1, Hunk<BlockData> data, KrudWorldBiome biome, int height, int max) {
        RNG rng = getRNG(realX, realZ);
        KrudWorldDecorator decorator = getDecorator(rng, biome, realX, realZ);

        if (decorator != null) {
            if (!decorator.isStacking()) {
                data.set(x, height, z, fixFaces(decorator.getBlockData100(biome, rng, realX, height, realZ, getData()), data, x, z, realX, height, realZ));
            } else {
                int stack = decorator.getHeight(rng, realX, realZ, getData());
                if (decorator.isScaleStack()) {
                    stack = Math.min((int) Math.ceil((double) max * ((double) stack / 100)), decorator.getAbsoluteMaxStack());
                } else {
                    stack = Math.min(max, stack);
                }

                if (stack == 1) {
                    data.set(x, height, z, decorator.getBlockDataForTop(biome, rng, realX, height, realZ, getData()));
                    return;
                }

                for (int i = 0; i < stack; i++) {
                    int h = height - i;
                    if (h < getEngine().getMinHeight()) {
                        continue;
                    }

                    double threshold = (((double) i) / (double) (stack - 1));

                    BlockData bd = threshold >= decorator.getTopThreshold() ?
                            decorator.getBlockDataForTop(biome, rng, realX, h, realZ, getData()) :
                            decorator.getBlockData100(biome, rng, realX, h, realZ, getData());

                    if (bd instanceof PointedDripstone) {
                        PointedDripstone.Thickness th = PointedDripstone.Thickness.BASE;

                        if (stack == 2) {
                            th = PointedDripstone.Thickness.FRUSTUM;

                            if (i == stack - 1) {
                                th = PointedDripstone.Thickness.TIP;
                            }
                        } else {
                            if (i == stack - 1) {
                                th = PointedDripstone.Thickness.TIP;
                            } else if (i == stack - 2) {
                                th = PointedDripstone.Thickness.FRUSTUM;
                            }
                        }


                        bd = Material.POINTED_DRIPSTONE.createBlockData();
                        ((PointedDripstone) bd).setThickness(th);
                        ((PointedDripstone) bd).setVerticalDirection(BlockFace.DOWN);
                    }

                    data.set(x, h, z, bd);
                }
            }
        }
    }
}
