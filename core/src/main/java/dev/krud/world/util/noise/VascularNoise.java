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

import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;

public class VascularNoise implements NoiseGenerator {
    private final FastNoiseDouble n;

    public VascularNoise(long seed) {
        this.n = new FastNoiseDouble(new RNG(seed).lmax());
        n.setNoiseType(FastNoiseDouble.NoiseType.Cellular);
        n.setCellularReturnType(FastNoiseDouble.CellularReturnType.Distance2Sub);
        n.setCellularDistanceFunction(FastNoiseDouble.CellularDistanceFunction.Natural);
    }

    private double filter(double noise) {
        return M.clip((noise / 2D) + 0.5D, 0D, 1D);
    }

    @Override
    public double noise(double x) {
        return filter(n.GetCellular(x, 0));
    }

    @Override
    public double noise(double x, double z) {
        return filter(n.GetCellular(x, z));
    }

    @Override
    public double noise(double x, double y, double z) {
        return filter(n.GetCellular(x, y, z));
    }
}
