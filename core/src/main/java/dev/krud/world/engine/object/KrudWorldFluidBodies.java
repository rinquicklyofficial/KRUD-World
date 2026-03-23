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
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("fluid-bodies")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a fluid body configuration")
@Data
public class KrudWorldFluidBodies {
    @ArrayType(type = KrudWorldRiver.class, min = 1)
    @Desc("Define rivers")
    private KList<KrudWorldRiver> rivers = new KList<>();

    @ArrayType(type = KrudWorldLake.class, min = 1)
    @Desc("Define lakes")
    private KList<KrudWorldLake> lakes = new KList<>();

    @BlockCoordinates
    public void generate(MantleWriter writer, RNG rng, Engine engine, int x, int y, int z) {
        if (rivers.isNotEmpty()) {
            for (KrudWorldRiver i : rivers) {
                i.generate(writer, rng, engine, x, y, z);
            }
        }

        if (lakes.isNotEmpty()) {
            for (KrudWorldLake i : lakes) {
                i.generate(writer, rng, engine, x, y, z);
            }
        }
    }

    public int getMaxRange(KrudWorldData data) {
        int max = 0;

        for (KrudWorldRiver i : rivers) {
            max = Math.max(max, i.getSize(data));
        }

        for (KrudWorldLake i : lakes) {
            max = Math.max(max, i.getSize(data));
        }


        return max;
    }
}
