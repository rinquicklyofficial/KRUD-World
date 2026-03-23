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

package dev.krud.world.util.data.palette;

public final class QuartPos {
    public static final int BITS = 2;

    public static final int SIZE = 4;

    private static final int SECTION_TO_QUARTS_BITS = 2;

    public static int fromBlock(int var0) {
        return var0 >> 2;
    }

    public static int toBlock(int var0) {
        return var0 << 2;
    }

    public static int fromSection(int var0) {
        return var0 << 2;
    }

    public static int toSection(int var0) {
        return var0 >> 2;
    }
}
