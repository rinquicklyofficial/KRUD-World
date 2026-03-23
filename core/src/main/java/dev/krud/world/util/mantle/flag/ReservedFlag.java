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

package dev.krud.world.util.mantle.flag;

public enum ReservedFlag implements MantleFlag {
    OBJECT,
    UPDATE,
    JIGSAW,
    FEATURE,
    INITIAL_SPAWNED,
    REAL,
    CARVED,
    FLUID_BODIES,
    INITIAL_SPAWNED_MARKER,
    CLEANED,
    PLANNED,
    ETCHED,
    TILE,
    CUSTOM,
    DISCOVERED,
    CUSTOM_ACTIVE,
    SCRIPT;

    @Override
    public boolean isCustom() {
        return false;
    }
}
