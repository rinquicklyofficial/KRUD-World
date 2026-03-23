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

public class GlobNoise implements NoiseGenerator {
    private final FastNoiseDouble n;

    public GlobNoise(long seed) {
        this.n = new FastNoiseDouble(new RNG(seed).lmax());
        n.setNoiseType(FastNoiseDouble.NoiseType.Cellular);
        n.setCellularReturnType(FastNoiseDouble.CellularReturnType.Distance2Div);
        n.setCellularDistanceFunction(FastNoiseDouble.CellularDistanceFunction.Natural);
    }

    private double f(double n) {
        return n + 1D;
    }

    @Override
    public double noise(double x) {
        return f(n.GetCellular(x, 0));
    }

    @Override
    public double noise(double x, double z) {
        return f(n.GetCellular(x, z));
    }

    @Override
    public double noise(double x, double y, double z) {
        return f(n.GetCellular(x, y, z));
    }
}
