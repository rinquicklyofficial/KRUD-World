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
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("slope-clip")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Translate objects")
@Data
public class KrudWorldSlopeClip {
    @MinNumber(0)
    @MaxNumber(1024)
    @Desc("The minimum slope for placement")
    private double minimumSlope = 0;

    @MinNumber(0)
    @MaxNumber(1024)
    @Desc("The maximum slope for placement")
    private double maximumSlope = 10;

    public boolean isDefault() {
        return minimumSlope <= 0 && maximumSlope >= 10;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid(double slope) {
        if (isDefault()) {
            return true;
        }

        return !(minimumSlope > slope) && !(maximumSlope < slope);
    }
}
