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

package dev.krud.world.engine.object.matter;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.KrudWorldStyledRange;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode()
@Accessors(chain = true)
@Desc("Represents a matter translator")
public class KrudWorldMatterTranslate {
    @Desc("For varied coordinate shifts use ranges not the literal coordinate")
    private KrudWorldStyledRange rangeX = null;
    @Desc("For varied coordinate shifts use ranges not the literal coordinate")
    private KrudWorldStyledRange rangeY = null;
    @Desc("For varied coordinate shifts use ranges not the literal coordinate")
    private KrudWorldStyledRange rangeZ = null;
    @Desc("Define an absolute shift instead of varied.")
    private int x = 0;
    @Desc("Define an absolute shift instead of varied.")
    private int y = 0;
    @Desc("Define an absolute shift instead of varied.")
    private int z = 0;

    public int xOffset(KrudWorldData data, RNG rng, int rx, int rz) {
        if (rangeX != null) {
            return (int) Math.round(rangeX.get(rng, rx, rz, data));
        }

        return x;
    }

    public int yOffset(KrudWorldData data, RNG rng, int rx, int rz) {
        if (rangeY != null) {
            return (int) Math.round(rangeY.get(rng, rx, rz, data));
        }

        return y;
    }

    public int zOffset(KrudWorldData data, RNG rng, int rx, int rz) {
        if (rangeZ != null) {
            return (int) Math.round(rangeZ.get(rng, rx, rz, data));
        }

        return z;
    }
}
