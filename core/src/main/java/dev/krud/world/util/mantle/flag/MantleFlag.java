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

import org.jetbrains.annotations.Contract;

public sealed interface MantleFlag permits CustomFlag, ReservedFlag {
    int MIN_ORDINAL = 64;
    int MAX_ORDINAL = 255;

    MantleFlag OBJECT = ReservedFlag.OBJECT;
    MantleFlag UPDATE = ReservedFlag.UPDATE;
    MantleFlag JIGSAW = ReservedFlag.JIGSAW;
    MantleFlag FEATURE = ReservedFlag.FEATURE;
    MantleFlag INITIAL_SPAWNED = ReservedFlag.INITIAL_SPAWNED;
    MantleFlag REAL = ReservedFlag.REAL;
    MantleFlag CARVED = ReservedFlag.CARVED;
    MantleFlag FLUID_BODIES = ReservedFlag.FLUID_BODIES;
    MantleFlag INITIAL_SPAWNED_MARKER = ReservedFlag.INITIAL_SPAWNED_MARKER;
    MantleFlag CLEANED = ReservedFlag.CLEANED;
    MantleFlag PLANNED = ReservedFlag.PLANNED;
    MantleFlag ETCHED = ReservedFlag.ETCHED;
    MantleFlag TILE = ReservedFlag.TILE;
    MantleFlag CUSTOM = ReservedFlag.CUSTOM;
    MantleFlag DISCOVERED = ReservedFlag.DISCOVERED;
    MantleFlag CUSTOM_ACTIVE = ReservedFlag.CUSTOM_ACTIVE;
    MantleFlag SCRIPT = ReservedFlag.SCRIPT;

    int RESERVED_FLAGS = ReservedFlag.values().length;

    String name();
    int ordinal();

    boolean isCustom();

    @Contract(value = "_ -> new", pure = true)
    static MantleFlag of(int ordinal) {
        if (ordinal < MIN_ORDINAL || ordinal > MAX_ORDINAL)
            throw new IllegalArgumentException("Ordinal must be between " + MIN_ORDINAL + " and " + MAX_ORDINAL);
        return new CustomFlag("CUSTOM:"+ordinal, ordinal);
    }
}
