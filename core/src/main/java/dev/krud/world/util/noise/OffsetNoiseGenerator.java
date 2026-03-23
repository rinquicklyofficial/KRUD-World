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
import org.jetbrains.annotations.NotNull;


public class OffsetNoiseGenerator implements NoiseGenerator {
    private final NoiseGenerator base;
    private final double ox, oz;

    public OffsetNoiseGenerator(NoiseGenerator base, long seed) {
        this.base = base;
        RNG rng = new RNG(seed);
        ox = rng.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
        oz = rng.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Override
    public double noise(double x) {
        return base.noise(x + ox);
    }

    @Override
    public double noise(double x, double z) {
        return base.noise(x + ox, z + oz);
    }

    @Override
    public double noise(double x, double y, double z) {
        return base.noise(x + ox, y, z + oz);
    }

    @Override
    public boolean isNoScale() {
        return base.isNoScale();
    }

    @Override
    public boolean isStatic() {
        return base.isStatic();
    }

    @NotNull
    public NoiseGenerator getBase() {
        return base;
    }
}