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

public class FlatNoise implements NoiseGenerator {
    public FlatNoise(long seed) {

    }

    @Override
    public double noise(double x) {
        return 1D;
    }

    @Override
    public double noise(double x, double z) {
        return 1D;
    }

    @Override
    public double noise(double x, double y, double z) {
        return 1D;
    }
}
