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

public class WhiteNoise implements NoiseGenerator {
    private final FastNoise n;

    public WhiteNoise(long seed) {
        n = new FastNoise(new RNG(seed).imax());
    }

    public boolean isStatic() {
        return true;
    }

    public boolean isNoScale() {
        return true;
    }

    private double f(double m) {
        return (m % 8192) * 1024;
    }

    @Override
    public double noise(double x) {
        return (n.GetWhiteNoise(f(x), 0d) / 2D) + 0.5D;
    }

    @Override
    public double noise(double x, double z) {
        return (n.GetWhiteNoise(f(x), f(z)) / 2D) + 0.5D;
    }

    @Override
    public double noise(double x, double y, double z) {
        return (n.GetWhiteNoise(f(x), f(y), f(z)) / 2D) + 0.5D;
    }
}
