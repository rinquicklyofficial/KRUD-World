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

public class SimplexNoise implements NoiseGenerator, OctaveNoise {
    private final FastNoiseDouble n;
    private int octaves;

    public SimplexNoise(long seed) {
        this.n = new FastNoiseDouble(new RNG(seed).lmax());
        octaves = 1;
    }

    public double f(double v) {
        return (v / 2D) + 0.5D;
    }

    @Override
    public double noise(double x) {
        if (octaves <= 1) {
            return f(n.GetSimplex(x, 0d));
        }

        double f = 1;
        double m = 0;
        double v = 0;

        for (int i = 0; i < octaves; i++) {
            v += n.GetSimplex((x * (f == 1 ? f++ : (f *= 2))), 0d) * f;
            m += f;
        }

        return f(v / m);
    }

    @Override
    public double noise(double x, double z) {
        if (octaves <= 1) {
            return f(n.GetSimplex(x, z));
        }
        double f = 1;
        double m = 0;
        double v = 0;

        for (int i = 0; i < octaves; i++) {
            f = f == 1 ? f + 1 : f * 2;
            v += n.GetSimplex((x * f), (z * f)) * f;
            m += f;
        }

        return f(v / m);
    }

    @Override
    public double noise(double x, double y, double z) {
        if (octaves <= 1) {
            return f(n.GetSimplex(x, y, z));
        }
        double f = 1;
        double m = 0;
        double v = 0;

        for (int i = 0; i < octaves; i++) {
            f = f == 1 ? f + 1 : f * 2;
            v += n.GetSimplex((x * f), (y * f), (z * f)) * f;
            m += f;
        }

        return f(v / m);
    }

    @Override
    public void setOctaves(int o) {
        octaves = o;
    }
}
