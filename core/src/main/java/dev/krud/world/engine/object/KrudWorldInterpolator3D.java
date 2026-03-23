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

import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.function.NoiseProvider3;
import dev.krud.world.util.interpolation.InterpolationMethod3D;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("interpolator-3d")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Configures interpolatin in 3D")
@Data
public class KrudWorldInterpolator3D {
    @Required
    @Desc("The interpolation method when two biomes use different heights but this same generator")
    private InterpolationMethod3D function = InterpolationMethod3D.TRILINEAR;

    @Required
    @MinNumber(1)
    @MaxNumber(8192)
    @Desc("The range checked in all dimensions. Smaller ranges yeild more detail but are not as smooth.")
    private double scale = 4;

    public double interpolate(double x, double y, double z, NoiseProvider3 provider) {
        return interpolate((int) Math.round(x), (int) Math.round(y), (int) Math.round(z), provider);
    }

    public double interpolate(int x, int y, int z, NoiseProvider3 provider) {
        return KrudWorldInterpolation.getNoise3D(getFunction(), x, y, z, getScale(), provider);
    }
}
