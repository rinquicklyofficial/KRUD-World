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

package dev.krud.world.engine.object;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Snippet("decorator")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A biome decorator is used for placing flowers, grass, cacti and so on")
@Data
public class KrudWorldDecorator {
    private final transient AtomicCache<CNG> layerGenerator = new AtomicCache<>();
    private final transient AtomicCache<CNG> varianceGenerator = new AtomicCache<>();
    private final transient AtomicCache<CNG> heightGenerator = new AtomicCache<>();
    private final transient AtomicCache<KList<BlockData>> blockData = new AtomicCache<>();
    private final transient AtomicCache<KList<BlockData>> blockDataTops = new AtomicCache<>();
    @Desc("The varience dispersion is used when multiple blocks are put in the palette. Scatter scrambles them, Wispy shows streak-looking varience")
    private KrudWorldGeneratorStyle variance = NoiseStyle.STATIC.style();
    @Desc("Forcefully place this decorant anywhere it is supposed to go even if it should not go on a specific surface block. For example, you could force tallgrass to place on top of stone by using this.")
    private boolean forcePlace = false;
    @Desc("Forced the surface block of this decorant to be the specified block. Assumes forcePlace.")
    private KrudWorldBlockData forceBlock;
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("When set, the decorator can only place onto any of these blocks.")
    private KList<KrudWorldBlockData> whitelist;
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("When set, the decorator will never place onto any of these blocks.")
    private KList<KrudWorldBlockData> blacklist;
    @Desc("The slope at which this decorator can be placed. Range from 0 to 10 by default. Calculated from a 3-block radius from the center of the decorator placement.")
    private KrudWorldSlopeClip slopeCondition = new KrudWorldSlopeClip();
    @DependsOn({"scaleStack", "stackMin", "stackMax"})
    @Desc("If stackMax is set to true, use this to limit its max height for large caverns")
    private int absoluteMaxStack = 30;
    @Desc("Dispersion is used to pick places to spawn. Scatter randomly places them (vanilla) or Wispy for a streak like patch system.")
    private KrudWorldGeneratorStyle style = NoiseStyle.STATIC.style();
    @DependsOn({"stackMin", "stackMax"})
    @Desc("If this decorator has a height more than 1 this changes how it picks the height between your maxes. Scatter = random, Wispy = wavy heights")
    private KrudWorldGeneratorStyle heightVariance = NoiseStyle.STATIC.style();
    @Desc("Tells iris where this decoration is a part of. I.e. SHORE_LINE or SEA_SURFACE")
    private KrudWorldDecorationPart partOf = KrudWorldDecorationPart.NONE;
    @DependsOn({"stackMin", "stackMax"})
    @MinNumber(1)
    @MaxNumber(2032) // TODO: WARNING HEIGHT
    @Desc("The minimum repeat stack height (setting to 3 would stack 3 of <block> on top of each other")
    private int stackMin = 1;
    @DependsOn({"stackMin", "stackMax"})
    @MinNumber(1)
    @MaxNumber(2032) // TODO: WARNING HEIGHT
    @Desc("The maximum repeat stack height")
    private int stackMax = 1;
    @DependsOn({"stackMin", "stackMax"})
    @Desc("""
            Changes stackMin and stackMin from being absolute block heights and instead uses them as a percentage to scale the stack based on the cave height

            Within a cave, setting them stackMin/max to 50 would make the stack 50% of the cave height""")
    private boolean scaleStack = false;
    @Required
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The chance for this decorator to decorate at a given X,Y coordinate. This is hit 256 times per chunk (per surface block)")
    // TODO: WARNING HEIGHT
    private double chance = 0.1;
    @Required
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("The palette of blocks to pick from when this decorator needs to place.")
    private KList<KrudWorldBlockData> palette = new KList<KrudWorldBlockData>().qadd(new KrudWorldBlockData("grass"));
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("The palette of blocks used at the very top of a 'stackMax' of higher than 1. For example, bamboo tops.")
    private KList<KrudWorldBlockData> topPalette = new KList<>();
    @DependsOn("topPalette")
    @MinNumber(0.01)
    @MaxNumber(1.0)
    @Desc("When the stack passes the top threshold, the top palette will start being used instead of the normal palette.")
    private double topThreshold = 1.0;

    public int getHeight(RNG rng, double x, double z, KrudWorldData data) {
        if (stackMin == stackMax) {
            return stackMin;
        }

        return getHeightGenerator(rng, data)
                .fit(stackMin, stackMax,
                        x / heightVariance.getZoom(),
                        z / heightVariance.getZoom()) + 1;
    }

    public CNG getHeightGenerator(RNG rng, KrudWorldData data) {
        return heightGenerator.aquire(() ->
                heightVariance.create(rng.nextParallelRNG(getBlockData(data).size() + stackMax + stackMin), data));
    }

    public CNG getGenerator(RNG rng, KrudWorldData data) {
        return layerGenerator.aquire(() -> style.create(rng.nextParallelRNG(getBlockData(data).size()), data));
    }

    public CNG getVarianceGenerator(RNG rng, KrudWorldData data) {
        return varianceGenerator.aquire(() ->
                variance.create(
                                rng.nextParallelRNG(getBlockData(data).size()), data)
                        .scale(1D / variance.getZoom()));
    }

    public KList<KrudWorldBlockData> add(String b) {
        palette.add(new KrudWorldBlockData(b));
        return palette;
    }

    public BlockData getBlockData(KrudWorldBiome b, RNG rng, double x, double z, KrudWorldData data) {
        if (getBlockData(data).isEmpty()) {
            KrudWorld.warn("Empty Block Data for " + b.getName());
            return null;
        }

        double xx = x / style.getZoom();
        double zz = z / style.getZoom();

        if (getGenerator(rng, data).fitDouble(0D, 1D, xx, zz) <= chance) {
            if (getBlockData(data).size() == 1) {
                return getBlockData(data).get(0);
            }

            return getVarianceGenerator(rng, data).fit(getBlockData(data), z, x); //X and Z must be switched
        }

        return null;
    }

    public BlockData getBlockData100(KrudWorldBiome b, RNG rng, double x, double y, double z, KrudWorldData data) {
        if (getBlockData(data).isEmpty()) {
            KrudWorld.warn("Empty Block Data for " + b.getName());
            return null;
        }

        double xx = x;
        double yy = y;
        double zz = z;

        if (!getVarianceGenerator(rng, data).isStatic()) {
            xx = x / style.getZoom();
            yy = y / style.getZoom();
            zz = z / style.getZoom();
        }

        if (getBlockData(data).size() == 1) {
            return getBlockData(data).get(0);
        }

        return getVarianceGenerator(rng, data).fit(getBlockData(data), z, y, x).clone(); //X and Z must be switched
    }

    public BlockData getBlockDataForTop(KrudWorldBiome b, RNG rng, double x, double y, double z, KrudWorldData data) {
        if (getBlockDataTops(data).isEmpty()) {
            return getBlockData100(b, rng, x, y, z, data);
        }

        double xx = x / style.getZoom();
        double zz = z / style.getZoom();

        if (getGenerator(rng, data).fitDouble(0D, 1D, xx, zz) <= chance) { //Exclude y from here
            if (getBlockData(data).size() == 1) {
                return getBlockDataTops(data).get(0);
            }

            return getVarianceGenerator(rng, data).fit(getBlockDataTops(data), z, y, x); //X and Z must be switched
        }

        return null;
    }

    public KList<BlockData> getBlockData(KrudWorldData data) {
        return blockData.aquire(() ->
        {
            KList<BlockData> blockData = new KList<>();
            for (KrudWorldBlockData i : palette) {
                BlockData bx = i.getBlockData(data);
                if (bx != null) {
                    for (int n = 0; n < i.getWeight(); n++) {
                        blockData.add(bx);
                    }
                }
            }

            return blockData;
        });
    }

    public KList<BlockData> getBlockDataTops(KrudWorldData data) {
        return blockDataTops.aquire(() ->
        {
            KList<BlockData> blockDataTops = new KList<>();
            for (KrudWorldBlockData i : topPalette) {
                BlockData bx = i.getBlockData(data);
                if (bx != null) {
                    for (int n = 0; n < i.getWeight(); n++) {
                        blockDataTops.add(bx);
                    }
                }
            }

            return blockDataTops;
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isStacking() {
        return getStackMax() > 1;
    }
}
