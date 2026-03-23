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

import dev.krud.world.util.function.Function2;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class ChunkCache<T> {
    private final AtomicReferenceArray<T> cache;

    public ChunkCache() {
        cache = new AtomicReferenceArray<>(256);
    }

    public T compute(int x, int z, Function2<Integer, Integer, T> function) {
        T t = get(x & 15, z & 15);

        if (t == null) {
            t = function.apply(x, z);
            set(x & 15, z & 15, t);
        }

        return t;
    }

    private void set(int x, int z, T t) {
        cache.set(x * 16 + z, t);
    }

    private T get(int x, int z) {
        return cache.get(x * 16 + z);
    }
}
