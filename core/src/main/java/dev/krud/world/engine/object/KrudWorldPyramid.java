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

@Snippet("carving-pyramid")
@Desc("Represents an procedural eliptical shape")
@Data
public class KrudWorldPyramid implements IRare {
    private transient final AtomicCache<MatterCavern> matterNodeCache = new AtomicCache<>();
    @Required
    @Desc("Typically a 1 in RARITY on a per fork basis")
    @MinNumber(1)
    private int rarity = 1;
    @RegistryListResource(KrudWorldBiome.class)
    @Desc("Force this cave to only generate the specified custom biome")
    private String customBiome = "";
    @Desc("The styled random radius for x")
    private KrudWorldStyledRange baseWidth = new KrudWorldStyledRange(1, 5, new KrudWorldGeneratorStyle(NoiseStyle.STATIC));

    public void generate(RNG rng, Engine engine, MantleWriter writer, int x, int y, int z) {
        writer.setPyramid(x, y, z, matterNodeCache.aquire(() -> CavernMatter.get(getCustomBiome(), 0)),
                (int) baseWidth.get(rng, z, y, engine.getData()), true);
    }

    public double maxSize() {
        return baseWidth.getMax();
    }
}
