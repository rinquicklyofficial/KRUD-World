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

package dev.krud.world.util.data;

import java.util.Arrays;

public class HeightMap {
    private final int[] height;

    public HeightMap() {
        height = new int[256];
        Arrays.fill(height, 0);
    }

    public void setHeight(int x, int z, int h) {
        height[x * 16 + z] = (h);
    }

    public int getHeight(int x, int z) {
        return height[x * 16 + z];
    }
}
