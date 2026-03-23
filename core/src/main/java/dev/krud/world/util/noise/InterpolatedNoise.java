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

import dev.krud.world.util.function.NoiseProvider;
import dev.krud.world.util.interpolation.InterpolationMethod;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;

public class InterpolatedNoise implements NoiseGenerator {
    private final InterpolationMethod method;
    private final NoiseProvider p;

    public InterpolatedNoise(long seed, NoiseType type, InterpolationMethod method) {
        this.method = method;
        NoiseGenerator g = type.create(seed);
        p = g::noise;
    }

    @Override
    public double noise(double x) {
        return noise(x, 0);
    }

    @Override
    public double noise(double x, double z) {
        return KrudWorldInterpolation.getNoise(method, (int) x, (int) z, 32, p);
    }

    @Override
    public double noise(double x, double y, double z) {
        if (z == 0) {
            return noise(x, y);
        }

        return KrudWorldInterpolation.getNoise(method, (int) x, (int) z, 32, p);
    }
}
