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
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.uniques.UFeature;
import dev.krud.world.util.uniques.UFeatureMeta;
import dev.krud.world.util.uniques.UImage;

import java.util.function.Consumer;

public class UFWarpedBackground implements UFeature {
    @Override
    public void render(UImage image, RNG rng, double t, Consumer<Double> progressor, UFeatureMeta meta) {
        CNG hue = generator("color_hue", rng, rng.d(0.001, rng.d(2, 5)), rng.i(0, 3), rng.i(0, 3), 31007, meta);
        CNG sat = generator("color_sat", rng, rng.d(0.001, rng.d(2, 5)), rng.i(0, 2), rng.i(0, 2), 33004, meta);
        CNG bri = generator("color_bri", rng, rng.d(0.001, rng.d(2, 5)), rng.i(0, 1), rng.i(0, 1), 32005, meta).patch(0.145);
        double tcf = rng.d(0.15, 0.55);

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                image.set(i, j, color(hue, sat, bri, i, j, tcf * t));
            }

            progressor.accept(i / (double) image.getWidth());
        }
    }
}
