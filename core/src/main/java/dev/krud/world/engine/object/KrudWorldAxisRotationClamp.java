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
import dev.krud.world.util.math.M;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("axis-rotation")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Desc("Represents a rotation axis with intervals and maxes. The x and z axis values are defaulted to disabled. The Y axis defaults to on, rotating by 90 degree increments.")
@Data
public class KrudWorldAxisRotationClamp {
    @Desc("Should this axis be rotated at all?")
    private boolean enabled = false;

    private transient boolean forceLock = false;

    @Required
    @DependsOn({"max"})
    @MinNumber(-360)
    @MaxNumber(360)
    @Desc("The minimum angle (from) or set this and max to zero for any angle degrees. Set both to the same non-zero value to force it to that angle only")
    private double min = 0;

    @Required
    @DependsOn({"min"})
    @MinNumber(-360)
    @MaxNumber(360)
    @Desc("The maximum angle (to) or set this and min to zero for any angle degrees. Set both to the same non-zero value to force it to that angle only")
    private double max = 0;

    @Required
    @DependsOn({"min", "max"})
    @MinNumber(0)
    @MaxNumber(360)
    @Desc("KrudWorld spins the axis but not freely. For example an interval of 90 would mean 4 possible angles (right angles) degrees. \nSetting this to 0 means totally free rotation.\n\nNote that a lot of structures can have issues with non 90 degree intervals because the minecraft block resolution is so low.")
    private double interval = 0;

    public void minMax(double fd) {
        min = fd;
        max = fd;
        forceLock = true;
    }

    public boolean isUnlimited() {
        return min == max && min == 0;
    }

    public boolean isLocked() {
        return min == max && !isUnlimited();
    }

    public double getRadians(int rng) {
        if (forceLock) {
            return Math.toRadians(Math.ceil(Math.abs((max % 360D))));
        }

        if (isUnlimited()) {
            if (interval < 1) {
                interval = 1;
            }

            return Math.toRadians((interval * (Math.ceil(Math.abs((rng % 360D) / interval)))) % 360D);
        }

        if (min == max && min != 0) {
            return Math.toRadians(max);
        }

        return Math.toRadians(M.clip((interval * (Math.ceil(Math.abs((rng % 360D) / interval)))) % 360D, Math.min(min, max), Math.max(min, max)));
    }
}
