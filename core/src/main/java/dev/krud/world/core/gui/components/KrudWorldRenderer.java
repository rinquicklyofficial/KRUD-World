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

package dev.krud.world.core.gui.components;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldBiomeGeneratorLink;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BiFunction;

public class KrudWorldRenderer {
    private final Engine renderer;

    public KrudWorldRenderer(Engine renderer) {
        this.renderer = renderer;
    }

    public BufferedImage render(double sx, double sz, double size, int resolution, RenderType currentType) {
        BufferedImage image = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_RGB);
        BiFunction<Double, Double, Integer> colorFunction = (d, dx) -> Color.black.getRGB();

        switch (currentType) {
            case BIOME, DECORATOR_LOAD, OBJECT_LOAD, LAYER_LOAD ->
                    colorFunction = (x, z) -> renderer.getComplex().getTrueBiomeStream().get(x, z).getColor(renderer, currentType).getRGB();
            case BIOME_LAND ->
                    colorFunction = (x, z) -> renderer.getComplex().getLandBiomeStream().get(x, z).getColor(renderer, currentType).getRGB();
            case BIOME_SEA ->
                    colorFunction = (x, z) -> renderer.getComplex().getSeaBiomeStream().get(x, z).getColor(renderer, currentType).getRGB();
            case REGION ->
                    colorFunction = (x, z) -> renderer.getComplex().getRegionStream().get(x, z).getColor(renderer.getComplex(), currentType).getRGB();
            case CAVE_LAND ->
                    colorFunction = (x, z) -> renderer.getComplex().getCaveBiomeStream().get(x, z).getColor(renderer, currentType).getRGB();
            case HEIGHT ->
                    colorFunction = (x, z) -> Color.getHSBColor(renderer.getComplex().getHeightStream().get(x, z).floatValue(), 100, 100).getRGB();
            case CONTINENT -> colorFunction = (x, z) -> {
                KrudWorldBiome b = renderer.getBiome((int) Math.round(x), renderer.getMaxHeight() - 1, (int) Math.round(z));
                KrudWorldBiomeGeneratorLink g = b.getGenerators().get(0);
                Color c;
                if (g.getMax() <= 0) {
                    // Max is below water level, so it is most likely an ocean biome
                    c = Color.BLUE;
                } else if (g.getMin() < 0) {
                    // Min is below water level, but max is not, so it is most likely a shore biome
                    c = Color.YELLOW;
                } else {
                    // Both min and max are above water level, so it is most likely a land biome
                    c = Color.GREEN;
                }
                return c.getRGB();
            };
        }

        double x, z;
        int i, j;
        for (i = 0; i < resolution; i++) {
            x = KrudWorldInterpolation.lerp(sx, sx + size, (double) i / (double) (resolution));

            for (j = 0; j < resolution; j++) {
                z = KrudWorldInterpolation.lerp(sz, sz + size, (double) j / (double) (resolution));
                image.setRGB(i, j, colorFunction.apply(x, z));
            }
        }

        return image;
    }
}
