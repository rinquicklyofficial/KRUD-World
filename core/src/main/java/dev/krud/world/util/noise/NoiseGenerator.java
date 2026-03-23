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

package dev.krud.world.util.noise;

import dev.krud.world.util.stream.ProceduralStream;
import dev.krud.world.util.stream.interpolation.Interpolated;

public interface NoiseGenerator {
    double noise(double x);

    double noise(double x, double z);

    double noise(double x, double y, double z);

    default boolean isStatic() {
        return false;
    }

    default boolean isNoScale() {
        return false;
    }

    default ProceduralStream<Double> stream() {
        return ProceduralStream.of(this::noise, this::noise, Interpolated.DOUBLE);
    }

    default OffsetNoiseGenerator offset(long seed) {
        return new OffsetNoiseGenerator(this, seed);
    }
}
