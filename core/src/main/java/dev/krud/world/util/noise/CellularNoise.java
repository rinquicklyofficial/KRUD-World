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

public class CellularNoise implements NoiseGenerator {
    private final FastNoise n;

    public CellularNoise(long seed) {
        this.n = new FastNoise(new RNG(seed).imax());
        n.SetNoiseType(FastNoise.NoiseType.Cellular);
        n.SetCellularReturnType(FastNoise.CellularReturnType.CellValue);
        n.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);
    }

    @Override
    public double noise(double x) {
        return (n.GetCellular((float) x, 0) / 2D) + 0.5D;
    }

    @Override
    public double noise(double x, double z) {
        return (n.GetCellular((float) x, (float) z) / 2D) + 0.5D;
    }

    @Override
    public double noise(double x, double y, double z) {
        return (n.GetCellular((float) x, (float) y, (float) z) / 2D) + 0.5D;
    }
}
