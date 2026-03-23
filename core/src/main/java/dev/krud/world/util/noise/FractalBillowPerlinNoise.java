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

public class FractalBillowPerlinNoise implements NoiseGenerator, OctaveNoise {
    private final FastNoiseDouble n;

    public FractalBillowPerlinNoise(long seed) {
        this.n = new FastNoiseDouble(new RNG(seed).lmax());
        n.setFractalOctaves(1);
        n.setFractalType(FractalType.Billow);
    }

    public double f(double v) {
        return (v / 2D) + 0.5D;
    }

    @Override
    public double noise(double x) {
        return f(n.GetPerlinFractal(x, 0f));
    }

    @Override
    public double noise(double x, double z) {
        return f(n.GetPerlinFractal(x, z));
    }

    @Override
    public double noise(double x, double y, double z) {
        return f(n.GetPerlinFractal(x, y, z));
    }

    @Override
    public void setOctaves(int o) {
        n.setFractalOctaves(o);
    }
}
