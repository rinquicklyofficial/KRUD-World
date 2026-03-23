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
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("shaped-style")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("This represents a generator with a min and max height")
@Data
public class KrudWorldShapedGeneratorStyle {
    @Required
    @Desc("The generator id")
    private KrudWorldGeneratorStyle generator = new KrudWorldGeneratorStyle(NoiseStyle.IRIS);

    @Required
    @MinNumber(-2032) // TODO: WARNING HEIGHT
    @MaxNumber(2032) // TODO: WARNING HEIGHT
    @Desc("The min block value")
    private int min = 0;

    @Required
    @MinNumber(-2032) // TODO: WARNING HEIGHT
    @MaxNumber(2032) // TODO: WARNING HEIGHT
    @Desc("The max block value")
    private int max = 0;

    public KrudWorldShapedGeneratorStyle(NoiseStyle style, int min, int max) {
        this(style);
        this.min = min;
        this.max = max;
    }

    public KrudWorldShapedGeneratorStyle(NoiseStyle style) {
        this.generator = new KrudWorldGeneratorStyle(style);
    }

    public double get(RNG rng, KrudWorldData data, double... dim) {
        return generator.create(rng, data).fitDouble(min, max, dim);
    }

    public boolean isFlat() {
        return min == max || getGenerator().isFlat();
    }

    public int getMid() {
        return (getMax() + getMin()) / 2;
    }
}
