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

package dev.krud.world.engine.data.cache;

import org.bukkit.Chunk;

public interface Cache<V> {
    static long key(Chunk chunk) {
        return key(chunk.getX(), chunk.getZ());
    }

    static long key(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }

    static int keyX(long key) {
        return (int) (key >> 32);
    }

    static int keyZ(long key) {
        return (int) key;
    }

    static int to1D(int x, int y, int z, int w, int h) {
        return (z * w * h) + (y * w) + x;
    }

    static int[] to3D(int idx, int w, int h) {
        final int z = idx / (w * h);
        idx -= (z * w * h);
        final int y = idx / w;
        final int x = idx % w;
        return new int[]{x, y, z};
    }

    int getId();

    V get(int x, int z);
}
