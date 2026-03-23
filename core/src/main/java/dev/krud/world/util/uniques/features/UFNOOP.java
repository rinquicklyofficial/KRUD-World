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

package dev.krud.world.util.uniques.features;

import dev.krud.world.util.math.RNG;
import dev.krud.world.util.uniques.UFeature;
import dev.krud.world.util.uniques.UFeatureMeta;
import dev.krud.world.util.uniques.UImage;

import java.util.function.Consumer;

public class UFNOOP implements UFeature {
    @Override
    public void render(UImage image, RNG rng, double t, Consumer<Double> progressor, UFeatureMeta meta) {
    }
}
