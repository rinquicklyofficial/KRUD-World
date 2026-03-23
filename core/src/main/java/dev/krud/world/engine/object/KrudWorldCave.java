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
import dev.krud.world.engine.object.annotations.RegistryListResource;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KSet;
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
public class KrudWorldCave extends KrudWorldRegistrant {
    @Desc("Define the shape of this cave")
    private KrudWorldWorm worm = new KrudWorldWorm();

    @Desc("Define potential forking features")
    private KrudWorldCarving fork = new KrudWorldCarving();

    @RegistryListResource(KrudWorldBiome.class)
    @Desc("Force this cave to only generate the specified custom biome")
    private String customBiome = "";

    @Desc("Limit the worm from ever getting higher or lower than this range")
    private KrudWorldRange verticalRange = new KrudWorldRange(3, 255);

    @Desc("Shape of the caves")
    private KrudWorldCaveShape shape = new KrudWorldCaveShape();

    @Override
    public String getFolderName() {
        return "caves";
    }

    @Override
    public String getTypeName() {
        return "Cave";
    }

    public void generate(MantleWriter writer, RNG rng, Engine engine, int x, int y, int z) {
        generate(writer, rng, new RNG(engine.getSeedManager().getCarve()), engine, x, y, z, 0, -1, true);
    }

    public void generate(MantleWriter writer, RNG rng, RNG base, Engine engine, int x, int y, int z, int recursion, int waterHint, boolean breakSurface) {
        double girth = getWorm().getGirth().get(base.nextParallelRNG(465156), x, z, engine.getData());
        KList<KrudWorldPosition> points = getWorm().generate(base.nextParallelRNG(784684), engine.getData(), writer, verticalRange, x, y, z, breakSurface, girth + 9);
        int highestWater = Math.max(waterHint, -1);

        if (highestWater == -1) {
            for (KrudWorldPosition i : points) {
                double yy = i.getY() + girth;
                int th = engine.getHeight(x, z, true);

                if (yy > th && th < engine.getDimension().getFluidHeight()) {
                    highestWater = Math.max(highestWater, (int) yy);
                    break;
                }
            }
        }


        int h = Math.min(highestWater, engine.getDimension().getFluidHeight());

        for (KrudWorldPosition i : points) {
            fork.doCarving(writer, rng, base, engine, i.getX(), i.getY(), i.getZ(), recursion, h);
        }

        MatterCavern c = new MatterCavern(true, customBiome, (byte) 0);
        MatterCavern w = new MatterCavern(true, customBiome, (byte) 1);

        CNG cng = shape.getNoise(base.nextParallelRNG(8131545), engine);
        KSet<KrudWorldPosition> mask = shape.getMasked(rng, engine);
        writer.setNoiseMasked(points,
                girth, shape.getNoiseThreshold() < 0 ? cng.noise(x, y, z) : shape.getNoiseThreshold(), cng, mask, true,
                (xf, yf, zf) -> yf <= h ? w : c);
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }

    public int getMaxSize(KrudWorldData data, int depth) {
        return (int) (Math.ceil(getWorm().getGirth().getMax() * 2) + getWorm().getMaxDistance() + fork.getMaxRange(data, depth));
    }
}
