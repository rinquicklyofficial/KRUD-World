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

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.InferredType;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldDecorationPart;
import dev.krud.world.engine.object.KrudWorldDecorator;
import dev.krud.world.util.data.B;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.math.RNG;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.PointedDripstone;

public class KrudWorldSurfaceDecorator extends KrudWorldEngineDecorator {
    public KrudWorldSurfaceDecorator(Engine engine) {
        super(engine, "Surface", KrudWorldDecorationPart.NONE);
    }

    @BlockCoordinates
    @Override
    public void decorate(int x, int z, int realX, int realX1, int realX_1, int realZ, int realZ1, int realZ_1, Hunk<BlockData> data, KrudWorldBiome biome, int height, int max) {
        if (biome.getInferredType().equals(InferredType.SHORE) && height < getDimension().getFluidHeight()) {
            return;
        }

        BlockData bd, bdx;
        RNG rng = getRNG(realX, realZ);
        KrudWorldDecorator decorator = getDecorator(rng, biome, realX, realZ);
        bdx = data.get(x, height, z);
        boolean underwater = height < getDimension().getFluidHeight();

        if (decorator != null) {
            if (!decorator.isForcePlace() && !decorator.getSlopeCondition().isDefault()
                    && !decorator.getSlopeCondition().isValid(getComplex().getSlopeStream().get(realX, realZ))) {
                return;
            }

            if (!decorator.isStacking()) {
                bd = decorator.getBlockData100(biome, rng, realX, height, realZ, getData());

                if (!underwater) {
                    if (!canGoOn(bd, bdx) && (!decorator.isForcePlace() && decorator.getForceBlock() == null)) {
                        return;
                    }
                }

                if (decorator.getForceBlock() != null) {
                    data.set(x, height, z, fixFaces(decorator.getForceBlock().getBlockData(getData()), data, x, z, realX, height, realZ));
                } else if (!decorator.isForcePlace()) {
                    if (decorator.getWhitelist() != null && decorator.getWhitelist().stream().noneMatch(d -> d.getBlockData(getData()).equals(bdx))) {
                        return;
                    }
                    if (decorator.getBlacklist() != null && decorator.getBlacklist().stream().anyMatch(d -> d.getBlockData(getData()).equals(bdx))) {
                        return;
                    }
                }

                if (bd instanceof Bisected) {
                    bd = bd.clone();
                    ((Bisected) bd).setHalf(Bisected.Half.TOP);
                    try {
                        data.set(x, height + 2, z, bd);
                    } catch (Throwable e) {
                        KrudWorld.reportError(e);
                    }
                    bd = bd.clone();
                    ((Bisected) bd).setHalf(Bisected.Half.BOTTOM);
                }

                if (B.isAir(data.get(x, height + 1, z))) {
                    data.set(x, height + 1, z, fixFaces(bd, data, x, z, realX, height + 1, realZ));
                }
            } else {
                if (height < getDimension().getFluidHeight()) {
                    max = getDimension().getFluidHeight();
                }

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
                    int h = height + i;
                    double threshold = ((double) i) / (stack - 1);
                    bd = threshold >= decorator.getTopThreshold() ?
                            decorator.getBlockDataForTop(biome, rng, realX, h, realZ, getData()) :
                            decorator.getBlockData100(biome, rng, realX, h, realZ, getData());

                    if (bd == null) {
                        break;
                    }

                    if (i == 0 && !underwater && !canGoOn(bd, bdx)) {
                        break;
                    }

                    if (underwater && height + 1 + i > getDimension().getFluidHeight()) {
                        break;
                    }

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
                        ((PointedDripstone) bd).setVerticalDirection(BlockFace.UP);
                    }

                    data.set(x, height + 1 + i, z, bd);
                }
            }
        }
    }
}
