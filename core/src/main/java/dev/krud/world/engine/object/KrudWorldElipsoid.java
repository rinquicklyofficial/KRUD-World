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

import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.matter.slices.CavernMatter;
import lombok.Data;

@Snippet("carving-elipsoid")
@Desc("Represents an procedural eliptical shape")
@Data
public class KrudWorldElipsoid implements IRare {
    private transient final AtomicCache<MatterCavern> matterNodeCache = new AtomicCache<>();
    @Required
    @Desc("Typically a 1 in RARITY on a per fork basis")
    @MinNumber(1)
    private int rarity = 1;
    @RegistryListResource(KrudWorldBiome.class)
    @Desc("Force this cave to only generate the specified custom biome")
    private String customBiome = "";
    @Desc("The styled random radius for x")
    private KrudWorldStyledRange xRadius = new KrudWorldStyledRange(1, 5, new KrudWorldGeneratorStyle(NoiseStyle.STATIC));
    @Desc("The styled random radius for y")
    private KrudWorldStyledRange yRadius = new KrudWorldStyledRange(1, 5, new KrudWorldGeneratorStyle(NoiseStyle.STATIC));
    @Desc("The styled random radius for z")
    private KrudWorldStyledRange zRadius = new KrudWorldStyledRange(1, 5, new KrudWorldGeneratorStyle(NoiseStyle.STATIC));

    @SuppressWarnings("SuspiciousNameCombination")
    public void generate(RNG rng, Engine engine, MantleWriter writer, int x, int y, int z) {
        writer.setElipsoid(x, y, z,
                xRadius.get(rng, z, y, engine.getData()),
                yRadius.get(rng, x, z, engine.getData()),
                zRadius.get(rng, y, x, engine.getData()), true, matterNodeCache.aquire(() -> CavernMatter.get(getCustomBiome(), 0)));
    }

    public double maxSize() {
        return Math.max(xRadius.getMax(), Math.max(yRadius.getMax(), zRadius.getMax()));
    }
}
