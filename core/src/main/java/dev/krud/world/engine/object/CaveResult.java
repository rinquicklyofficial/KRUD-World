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

import lombok.Data;

@Data
public class CaveResult {
    private int floor;
    private int ceiling;

    public CaveResult(int floor, int ceiling) {
        this.floor = floor;
        this.ceiling = ceiling;
    }

    public boolean isWithin(int v) {
        return v > floor || v < ceiling;
    }
}
