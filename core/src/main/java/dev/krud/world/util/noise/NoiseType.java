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

import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.util.interpolation.InterpolationMethod;

public enum NoiseType {
    WHITE(WhiteNoise::new),
    WHITE_BILINEAR((s) -> new InterpolatedNoise(s, WHITE, InterpolationMethod.BILINEAR)),
    WHITE_BICUBIC((s) -> new InterpolatedNoise(s, WHITE, InterpolationMethod.BICUBIC)),
    WHITE_HERMITE((s) -> new InterpolatedNoise(s, WHITE, InterpolationMethod.HERMITE)),
    SIMPLEX(SimplexNoise::new),
    PERLIN(seed -> new PerlinNoise(seed).hermite()),
    FRACTAL_BILLOW_SIMPLEX(FractalBillowSimplexNoise::new),
    FRACTAL_BILLOW_PERLIN(FractalBillowPerlinNoise::new),
    FRACTAL_FBM_SIMPLEX(FractalFBMSimplexNoise::new),
    FRACTAL_RIGID_MULTI_SIMPLEX(FractalRigidMultiSimplexNoise::new),
    FLAT(FlatNoise::new),
    CELLULAR(CellularNoise::new),
    CELLULAR_BILINEAR((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.BILINEAR)),
    CELLULAR_BILINEAR_STARCAST_3((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.BILINEAR_STARCAST_3)),
    CELLULAR_BILINEAR_STARCAST_6((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.BILINEAR_STARCAST_6)),
    CELLULAR_BILINEAR_STARCAST_9((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.BILINEAR_STARCAST_9)),
    CELLULAR_BILINEAR_STARCAST_12((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.BILINEAR_STARCAST_12)),
    CELLULAR_BICUBIC((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.BICUBIC)),
    CELLULAR_HERMITE((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.HERMITE)),
    CELLULAR_STARCAST_3((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.STARCAST_3)),
    CELLULAR_STARCAST_6((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.STARCAST_6)),
    CELLULAR_STARCAST_9((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.STARCAST_9)),
    CELLULAR_STARCAST_12((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.STARCAST_12)),
    CELLULAR_HERMITE_STARCAST_3((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.HERMITE_STARCAST_3)),
    CELLULAR_HERMITE_STARCAST_6((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.HERMITE_STARCAST_6)),
    CELLULAR_HERMITE_STARCAST_9((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.HERMITE_STARCAST_9)),
    CELLULAR_HERMITE_STARCAST_12((s) -> new InterpolatedNoise(s, CELLULAR, InterpolationMethod.HERMITE_STARCAST_12)),
    GLOB(GlobNoise::new),
    CUBIC(CubicNoise::new),
    FRACTAL_CUBIC(FractalCubicNoise::new),
    CELLULAR_HEIGHT(CellHeightNoise::new),
    CLOVER(CloverNoise::new),
    CLOVER_BILINEAR((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.BILINEAR)),
    CLOVER_BILINEAR_STARCAST_3((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.BILINEAR_STARCAST_3)),
    CLOVER_BILINEAR_STARCAST_6((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.BILINEAR_STARCAST_6)),
    CLOVER_BILINEAR_STARCAST_9((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.BILINEAR_STARCAST_9)),
    CLOVER_BILINEAR_STARCAST_12((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.BILINEAR_STARCAST_12)),
    CLOVER_BICUBIC((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.BICUBIC)),
    CLOVER_HERMITE((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.HERMITE)),
    CLOVER_STARCAST_3((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.STARCAST_3)),
    CLOVER_STARCAST_6((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.STARCAST_6)),
    CLOVER_STARCAST_9((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.STARCAST_9)),
    CLOVER_STARCAST_12((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.STARCAST_12)),
    CLOVER_HERMITE_STARCAST_3((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.HERMITE_STARCAST_3)),
    CLOVER_HERMITE_STARCAST_6((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.HERMITE_STARCAST_6)),
    CLOVER_HERMITE_STARCAST_9((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.HERMITE_STARCAST_9)),
    CLOVER_HERMITE_STARCAST_12((s) -> new InterpolatedNoise(s, CLOVER, InterpolationMethod.HERMITE_STARCAST_12)),
    VASCULAR(VascularNoise::new);

    private final NoiseFactory f;

    NoiseType(NoiseFactory f) {
        this.f = f;
    }

    public NoiseGenerator create(long seed) {
        if (KrudWorldSettings.get().getGenerator().offsetNoiseTypes) return f.create(seed).offset(seed);
        else return f.create(seed);
    }
}
