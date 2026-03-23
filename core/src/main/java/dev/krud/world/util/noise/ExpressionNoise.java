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

import dev.krud.world.engine.object.KrudWorldExpression;
import dev.krud.world.util.math.RNG;

public class ExpressionNoise implements NoiseGenerator {
    private final RNG rng;
    private final KrudWorldExpression expression;

    public ExpressionNoise(RNG rng, KrudWorldExpression expression) {
        this.rng = rng;
        this.expression = expression;
    }

    @Override
    public double noise(double x) {
        return expression.evaluate(rng, x, -1);
    }

    @Override
    public double noise(double x, double z) {
        return expression.evaluate(rng, x, z);
    }

    @Override
    public double noise(double x, double y, double z) {
        return expression.evaluate(rng, x, y, z);
    }
}
