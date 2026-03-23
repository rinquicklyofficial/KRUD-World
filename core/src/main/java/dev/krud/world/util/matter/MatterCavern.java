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

package dev.krud.world.util.matter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatterCavern {
    private final boolean cavern;
    private final String customBiome;
    private final byte liquid; // 0 none 1 water 2 lava

    public boolean isAir() {
        return liquid == 0;
    }

    public boolean isWater() {
        return liquid == 1;
    }

    public boolean isLava() {
        return liquid == 2;
    }
}
