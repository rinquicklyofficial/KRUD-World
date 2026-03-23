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

import dev.krud.world.util.math.RNG;

public class CubicNoise implements NoiseGenerator {
    private final FastNoiseDouble n;

    public CubicNoise(long seed) {
        this.n = new FastNoiseDouble(new RNG(seed).lmax());
    }

    private double f(double n) {
        return (n / 2D) + 0.5D;
    }

    @Override
    public double noise(double x) {
        return f(n.GetCubic(x, 0));
    }

    @Override
    public double noise(double x, double z) {
        return f(n.GetCubic(x, z));
    }

    @Override
    public double noise(double x, double y, double z) {
        return f(n.GetCubic(x, y, z));
    }
}
