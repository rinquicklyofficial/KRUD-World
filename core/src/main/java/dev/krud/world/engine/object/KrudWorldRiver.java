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
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("river")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents an KrudWorld river")
@Data
public class KrudWorldRiver implements IRare {
    @Required
    @Desc("Typically a 1 in RARITY on a per chunk/fork basis")
    @MinNumber(1)
    private int rarity = 15;

    @Desc("The width style of this river")
    private KrudWorldStyledRange width = new KrudWorldStyledRange(3, 6, NoiseStyle.PERLIN.style());

    @Desc("Define the shape of this river")
    private KrudWorldWorm worm = new KrudWorldWorm();

    @RegistryListResource(KrudWorldBiome.class)
    @Desc("Force this river to only generate the specified custom biome")
    private String customBiome = "";

    @Desc("The width style of this lake")
    private KrudWorldShapedGeneratorStyle widthStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN.style(), 5, 9);

    @Desc("The depth style of this lake")
    private KrudWorldShapedGeneratorStyle depthStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN.style(), 4, 7);

    public int getSize(KrudWorldData data) {
        return worm.getMaxDistance();
    }

    public void generate(MantleWriter writer, RNG rng, Engine engine, int x, int y, int z) {

    }
}
