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
import dev.krud.world.engine.object.annotations.MaxNumber;
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.util.function.NoiseProvider;
import dev.krud.world.util.interpolation.InterpolationMethod;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Configures rotation for iris")
@Data
public class KrudWorldInterpolator {
    public static final KrudWorldInterpolator DEFAULT = new KrudWorldInterpolator();

    @Required
    @Desc("The interpolation method when two biomes use different heights but this same generator")
    private InterpolationMethod function = InterpolationMethod.BILINEAR_STARCAST_6;

    @Required
    @MinNumber(1)
    @MaxNumber(8192)
    @Desc("The range checked horizontally. Smaller ranges yeild more detail but are not as smooth.")
    private double horizontalScale = 7;

    @Override
    public int hashCode() {
        return Objects.hash(horizontalScale, function);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof KrudWorldInterpolator i) {
            return i.getFunction().equals(function) && i.getHorizontalScale() == horizontalScale;
        }

        return false;
    }

    public double interpolate(double x, double z, NoiseProvider provider) {
        return interpolate((int) Math.round(x), (int) Math.round(z), provider);
    }

    public double interpolate(int x, int z, NoiseProvider provider) {
        return KrudWorldInterpolation.getNoise(getFunction(), x, z, getHorizontalScale(), provider);
    }
}
