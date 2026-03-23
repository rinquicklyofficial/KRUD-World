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
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.stream.ProceduralStream;
import dev.krud.world.util.stream.interpolation.Interpolated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("style-range")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a range styled with a custom generator")
@Data
public class KrudWorldStyledRange {
    @Desc("The minimum value")
    private double min = 16;

    @Desc("The maximum value")
    private double max = 32;

    @Desc("The style to pick the range")
    private KrudWorldGeneratorStyle style = new KrudWorldGeneratorStyle(NoiseStyle.STATIC);

    public double get(RNG rng, double x, double z, KrudWorldData data) {
        if (min == max) {
            return min;
        }

        if (style.isFlat()) {
            return M.lerp(min, max, 0.5);
        }

        return style.create(rng, data).fitDouble(min, max, x, z);
    }

    public ProceduralStream<Double> stream(RNG rng, KrudWorldData data) {
        return ProceduralStream.of((x, z) -> get(rng, x, z, data), Interpolated.DOUBLE);
    }

    public boolean isFlat() {
        return getMax() == getMin() || style.isFlat();
    }

    public int getMid() {
        return (int) ((getMax() + getMin()) / 2);
    }
}
