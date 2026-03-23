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

package dev.krud.world.util.interpolation;

import dev.krud.world.engine.object.annotations.Desc;

@Desc("An interpolation method (or function) is simply a method of smoothing a position based on surrounding points on a grid. Bicubic for example is smoother, but has 4 times the checks than Bilinear for example. Try using BILINEAR_STARCAST_9 for beautiful results.")
public enum InterpolationMethod3D {
    TRILINEAR,
    TRICUBIC,
    TRIHERMITE,
    TRISTARCAST_3,
    TRISTARCAST_6,
    TRISTARCAST_9,
    TRISTARCAST_12,
    TRILINEAR_TRISTARCAST_3,
    TRILINEAR_TRISTARCAST_6,
    TRILINEAR_TRISTARCAST_9,
    TRILINEAR_TRISTARCAST_12,
    NONE
}
