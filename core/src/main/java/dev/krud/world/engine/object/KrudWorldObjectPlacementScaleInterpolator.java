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

import dev.krud.world.engine.object.annotations.Desc;

@Desc("Use 3D Interpolation on scaled objects if they are larger than the origin.")
public enum KrudWorldObjectPlacementScaleInterpolator {
    @Desc("Don't interpolate, big cubes")
    NONE,
    @Desc("Uses linear interpolation in 3 dimensions, generally pretty good, but slow")
    TRILINEAR,
    @Desc("Uses cubic spline interpolation in 3 dimensions, even better, but extreme slowdowns")
    TRICUBIC,
    @Desc("Uses hermite spline interpolation in 3 dimensions, even better, but extreme slowdowns")
    TRIHERMITE
}
