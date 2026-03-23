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

import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.util.collection.KMap;

public class ComplexCache<T> {
    private final KMap<Long, ChunkCache<T>> chunks;

    public ComplexCache() {
        chunks = new KMap<>();
    }

    public boolean has(int x, int z) {
        return chunks.containsKey(Cache.key(x, z));
    }

    public void invalidate(int x, int z) {
        chunks.remove(Cache.key(x, z));
    }

    public ChunkCache<T> chunk(int x, int z) {
        return chunks.computeIfAbsent(Cache.key(x, z), (f) -> new ChunkCache<>());
    }
}
