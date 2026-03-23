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

package dev.krud.world.engine.object;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("generator")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A noise generator")
@Data
public class KrudWorldNoiseGenerator {
    private final transient AtomicCache<CNG> generator = new AtomicCache<>();
    @MinNumber(0.0001)
    @Desc("The coordinate input zoom")
    private double zoom = 1;
    @Desc("Reverse the output. So that noise = -noise + opacity")
    private boolean negative = false;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The output multiplier")
    private double opacity = 1;
    @Desc("Coordinate offset x")
    private double offsetX = 0;
    @Desc("Height output offset y. Avoid using with terrain generation.")
    private double offsetY = 0;
    @Desc("Coordinate offset z")
    private double offsetZ = 0;
    @Required
    @Desc("The seed")
    private long seed = 0;
    @Desc("Apply a parametric curve on the output")
    private boolean parametric = false;
    @Desc("Apply a bezier curve on the output")
    private boolean bezier = false;
    @Desc("Apply a sin-center curve on the output (0, and 1 = 0 and 0.5 = 1.0 using a sinoid shape.)")
    private boolean sinCentered = false;
    @Desc("The exponent noise^EXPONENT")
    private double exponent = 1;
    @Desc("Enable / disable. Outputs offsetY if disabled")
    private boolean enabled = true;
    @Required
    @Desc("The Noise Style")
    private KrudWorldGeneratorStyle style = NoiseStyle.IRIS.style();
    @MinNumber(1)
    @Desc("Multiple octaves for multple generators of changing zooms added together")
    private int octaves = 1;
    @ArrayType(min = 1, type = KrudWorldNoiseGenerator.class)
    @Desc("Apply a child noise generator to fracture the input coordinates of this generator")
    private KList<KrudWorldNoiseGenerator> fracture = new KList<>();

    public KrudWorldNoiseGenerator(boolean enabled) {
        this();
        this.enabled = enabled;
    }

    protected CNG getGenerator(long superSeed, KrudWorldData data) {
        return generator.aquire(() -> style.create(new RNG(superSeed + 33955677 - seed), data).oct(octaves));
    }

    public double getMax() {
        return getOffsetY() + opacity;
    }

    public double getNoise(long superSeed, double xv, double zv, KrudWorldData data) {
        if (!enabled) {
            return offsetY;
        }

        double x = xv;
        double z = zv;
        int g = 33;

        for (KrudWorldNoiseGenerator i : fracture) {
            if (i.isEnabled()) {
                x += i.getNoise(superSeed + seed + g, xv, zv, data) - (opacity / 2D);
                z -= i.getNoise(superSeed + seed + g, zv, xv, data) - (opacity / 2D);
            }
            g += 819;
        }

        double n = getGenerator(superSeed, data).fitDouble(0, opacity, (x / zoom) + offsetX, (z / zoom) + offsetZ);
        n = negative ? (-n + opacity) : n;
        n = (exponent != 1 ? n < 0 ? -Math.pow(-n, exponent) : Math.pow(n, exponent) : n) + offsetY;
        n = parametric ? KrudWorldInterpolation.parametric(n, 1) : n;
        n = bezier ? KrudWorldInterpolation.bezier(n) : n;
        n = sinCentered ? KrudWorldInterpolation.sinCenter(n) : n;

        return n;
    }

    public KList<KrudWorldNoiseGenerator> getAllComposites() {
        KList<KrudWorldNoiseGenerator> g = new KList<>();

        g.add(this);

        for (KrudWorldNoiseGenerator i : getFracture()) {
            g.addAll(i.getAllComposites());
        }

        return g;
    }
}
