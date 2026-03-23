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

import dev.krud.world.util.function.NoiseProvider;
import dev.krud.world.util.interpolation.InterpolationMethod;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.uniques.UFeature;
import dev.krud.world.util.uniques.UFeatureMeta;
import dev.krud.world.util.uniques.UImage;

import java.awt.*;
import java.util.function.Consumer;

public class UFInterpolator implements UFeature {
    @Override
    public void render(UImage image, RNG rng, double t, Consumer<Double> progressor, UFeatureMeta meta) {
        UImage ref = image.copy();
        CNG rmod = generator("interpolator_radius", rng, 1, 33004, meta);

        NoiseProvider nHue = (x, y) -> {
            int ix = Math.abs(((int) x) % ref.getWidth());
            int iy = Math.abs(((int) y) % ref.getHeight());
            Color color = ref.get(ix, iy);
            float[] hsv = new float[3];
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getGreen(), hsv);
            return hsv[0];
        };
        NoiseProvider nSat = (x, y) -> {
            int ix = Math.abs(((int) x) % ref.getWidth());
            int iy = Math.abs(((int) y) % ref.getHeight());
            Color color = ref.get(ix, iy);
            float[] hsv = new float[3];
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getGreen(), hsv);
            return hsv[1];
        };
        NoiseProvider nBri = (x, y) -> {
            int ix = Math.abs(((int) x) % ref.getWidth());
            int iy = Math.abs(((int) y) % ref.getHeight());
            Color color = ref.get(ix, iy);
            float[] hsv = new float[3];
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getGreen(), hsv);
            return hsv[2];
        };
        InterpolationMethod method = interpolator(rng);
        int sizeMin = Math.min(image.getWidth(), image.getHeight());
        double radius = Math.max(4, rmod.fit(sizeMin / 256, sizeMin / 4, t * rng.d(0.03, 1.25), t * rng.d(0.01, 2.225)));
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                image.set(i, j, Color.getHSBColor(
                        (float) Math.max(Math.min(1D, KrudWorldInterpolation.getNoise(method, i, j, radius, nHue)), 0D),
                        (float) Math.max(Math.min(1D, KrudWorldInterpolation.getNoise(method, i, j, radius, nSat)), 0D),
                        (float) Math.max(Math.min(1D, KrudWorldInterpolation.getNoise(method, i, j, radius, nBri)), 0D)
                ));
            }

            progressor.accept(i / (double) image.getWidth());
        }
    }
}
