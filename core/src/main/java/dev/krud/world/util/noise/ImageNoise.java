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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.KrudWorldImageMap;

public class ImageNoise implements NoiseGenerator {
    private final KrudWorldImageMap expression;
    private final KrudWorldData data;

    public ImageNoise(KrudWorldData data, KrudWorldImageMap expression) {
        this.data = data;
        this.expression = expression;
    }

    @Override
    public double noise(double x) {
        return noise(x, 0);
    }

    @Override
    public double noise(double x, double z) {
        return expression.getNoise(data, (int) x, (int) z);
    }

    @Override
    public double noise(double x, double y, double z) {
        return noise(x, z + y);
    }
}
