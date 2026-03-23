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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.MaxNumber;
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.RegistryListResource;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Translate objects")
@Data
public class KrudWorldRavine extends KrudWorldRegistrant {
    @Desc("Define the shape of this ravine (2d, ignores Y)")
    private KrudWorldWorm worm = new KrudWorldWorm();

    @RegistryListResource(KrudWorldBiome.class)
    @Desc("Force this cave to only generate the specified custom biome")
    private String customBiome = "";

    @Desc("Define potential forking features")
    private KrudWorldCarving fork = new KrudWorldCarving();

    @Desc("The style used to determine the curvature of this worm's y")
    private KrudWorldShapedGeneratorStyle depthStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN, 5, 18);

    @Desc("The style used to determine the curvature of this worm's y")
    private KrudWorldShapedGeneratorStyle baseWidthStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN, 3, 6);

    @MinNumber(1)
    @MaxNumber(100)
    @Desc("The angle at which the ravine widens as it gets closer to the surface")
    private double angle = 18;

    @MinNumber(1)
    @MaxNumber(100)
    @Desc("The angle at which the ravine widens as it gets closer to the surface")
    private double topAngle = 38;

    @Desc("To fill this cave with lava, set the lava level to a height from the bottom most point of the cave.")
    private int lavaLevel = -1;

    @Desc("How many worm nodes must be placed to actually generate a ravine? Higher reduces the chances but also reduces ravine 'holes'")
    private int nodeThreshold = 5;

    @MinNumber(1)
    @MaxNumber(8)
    @Desc("The thickness of the ravine ribs")
    private double ribThickness = 3;

    @Override
    public String getFolderName() {
        return "ravines";
    }

    @Override
    public String getTypeName() {
        return "Ravine";
    }

    public void generate(MantleWriter writer, RNG rng, Engine engine, int x, int y, int z) {
        generate(writer, rng, new RNG(engine.getSeedManager().getCarve()), engine, x, y, z, 0, -1);
    }

    public void generate(MantleWriter writer, RNG rng, RNG base, Engine engine, int x, int y, int z, int recursion, int waterHint) {
        KList<KrudWorldPosition> pos = getWorm().generate(base.nextParallelRNG(879615), engine.getData(), writer, null, x, y, z, true, 0);
        CNG dg = depthStyle.getGenerator().create(base.nextParallelRNG(7894156), engine.getData());
        CNG bw = baseWidthStyle.getGenerator().create(base.nextParallelRNG(15315456), engine.getData());
        int highestWater = Math.max(waterHint, -1);
        boolean water = false;

        if (highestWater == -1) {
            for (KrudWorldPosition i : pos) {
                int rsurface = y == -1 ? engine.getComplex().getHeightStream().get(x, z).intValue() : y;
                int depth = (int) Math.round(dg.fitDouble(depthStyle.getMin(), depthStyle.getMax(), i.getX(), i.getZ()));
                int surface = (int) Math.round(rsurface - depth * 0.45);
                int yy = surface + depth;
                int th = engine.getHeight(x, z, true);

                if (yy > th && th < engine.getDimension().getFluidHeight()) {
                    highestWater = Math.max(highestWater, yy);
                    water = true;
                    break;
                }
            }
        } else {
            water = true;
        }

        MatterCavern c = new MatterCavern(true, customBiome, (byte) (water ? 1 : 0));
        MatterCavern l = new MatterCavern(true, customBiome, (byte) 2);

        if (pos.size() < nodeThreshold) {
            return;
        }

        for (KrudWorldPosition p : pos) {
            int rsurface = y == -1 ? engine.getComplex().getHeightStream().get(x, z).intValue() : y;
            int depth = (int) Math.round(dg.fitDouble(depthStyle.getMin(), depthStyle.getMax(), p.getX(), p.getZ()));
            int width = (int) Math.round(bw.fitDouble(baseWidthStyle.getMin(), baseWidthStyle.getMax(), p.getX(), p.getZ()));
            int surface = (int) Math.round(rsurface - depth * 0.45);

            fork.doCarving(writer, rng, base, engine, p.getX(), rng.i(surface - depth, surface), p.getZ(), recursion, highestWater);

            for (int i = surface + depth; i >= surface; i--) {
                if (i % ribThickness == 0) {
                    double v = width + ((((surface + depth) - i) * (angle / 360D)));

                    if (v <= 0.25) {
                        break;
                    }

                    if (i <= ribThickness + 2) {
                        break;
                    }

                    if (lavaLevel >= 0 && i <= lavaLevel + (surface - depthStyle.getMid())) {
                        writer.setElipsoid(p.getX(), i, p.getZ(), v, ribThickness, v, true, l);
                    } else {
                        writer.setElipsoid(p.getX(), i, p.getZ(), v, ribThickness, v, true, c);
                    }
                }
            }

            for (int i = surface - depth; i <= surface; i++) {
                if (i % ribThickness == 0) {
                    double v = width - ((((surface - depth) - i) * (angle / 360D)));

                    if (v <= 0.25) {
                        break;
                    }

                    if (i <= ribThickness + 2) {
                        break;
                    }

                    if (lavaLevel >= 0 && i <= lavaLevel + (surface - depthStyle.getMid())) {
                        writer.setElipsoid(p.getX(), i, p.getZ(), v, ribThickness, v, true, l);
                    } else {
                        writer.setElipsoid(p.getX(), i, p.getZ(), v, ribThickness, v, true, c);
                    }
                }
            }
        }
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }

    public int getMaxSize(KrudWorldData data, int depth) {
        return getWorm().getMaxDistance() + fork.getMaxRange(data, depth);
    }
}
