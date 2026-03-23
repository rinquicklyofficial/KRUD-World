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

package dev.krud.world.core.pregenerator.cache;

record SynchronizedCache(PregenCache cache) implements PregenCache {
    @Override
    public boolean isThreadSafe() {
        return true;
    }

    @Override
    public boolean isChunkCached(int x, int z) {
        synchronized (cache) {
            return cache.isChunkCached(x, z);
        }
    }

    @Override
    public boolean isRegionCached(int x, int z) {
        synchronized (cache) {
            return cache.isRegionCached(x, z);
        }
    }

    @Override
    public void cacheChunk(int x, int z) {
        synchronized (cache) {
            cache.cacheChunk(x, z);
        }
    }

    @Override
    public void cacheRegion(int x, int z) {
        synchronized (cache) {
            cache.cacheRegion(x, z);
        }
    }

    @Override
    public void write() {
        synchronized (cache) {
            cache.write();
        }
    }

    @Override
    public void trim(long unloadDuration) {
        synchronized (cache) {
            cache.trim(unloadDuration);
        }
    }
}
