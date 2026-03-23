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
import dev.krud.world.util.noise.FastNoiseDouble.FractalType;

public class FractalCubicNoise implements NoiseGenerator {
    private final FastNoiseDouble n;

    public FractalCubicNoise(long seed) {
        this.n = new FastNoiseDouble(new RNG(seed).lmax());
        n.setFractalType(FractalType.Billow);
    }

    private double f(double n) {
        return (n / 2D) + 0.5D;
    }

    @Override
    public double noise(double x) {
        return f(n.GetCubicFractal(x, 0));
    }

    @Override
    public double noise(double x, double z) {
        return f(n.GetCubicFractal(x, z));
    }

    @Override
    public double noise(double x, double y, double z) {
        return f(n.GetCubicFractal(x, y, z));
    }
}
