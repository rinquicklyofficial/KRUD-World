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

import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("range")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a range")
@Data
public class KrudWorldRange {
    @Desc("The minimum value")
    private double min = 16;

    @Desc("The maximum value")
    private double max = 32;

    public double get(RNG rng) {
        if (min == max) {
            return min;
        }

        return rng.d(min, max);
    }

    public boolean contains(int v) {
        return v >= min && v <= max;
    }

    public KrudWorldRange merge(KrudWorldRange other) {
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
        return this;
    }
}
