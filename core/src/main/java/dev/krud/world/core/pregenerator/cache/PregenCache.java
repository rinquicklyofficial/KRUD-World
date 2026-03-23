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

import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.documentation.RegionCoordinates;

import java.io.File;

public interface PregenCache {
    default boolean isThreadSafe() {
        return false;
    }

    @ChunkCoordinates
    boolean isChunkCached(int x, int z);

    @RegionCoordinates
    boolean isRegionCached(int x, int z);

    @ChunkCoordinates
    void cacheChunk(int x, int z);

    @RegionCoordinates
    void cacheRegion(int x, int z);

    void write();

    void trim(long unloadDuration);

    static PregenCache create(File directory) {
        if (directory == null) return EMPTY;
        return new PregenCacheImpl(directory, 16);
    }

    default PregenCache sync() {
        if (isThreadSafe()) return this;
        return new SynchronizedCache(this);
    }

    PregenCache EMPTY = new PregenCache() {
        @Override
        public boolean isThreadSafe() {
            return true;
        }

        @Override
        public boolean isChunkCached(int x, int z) {
            return false;
        }

        @Override
        public boolean isRegionCached(int x, int z) {
            return false;
        }

        @Override
        public void cacheChunk(int x, int z) {}

        @Override
        public void cacheRegion(int x, int z) {}

        @Override
        public void write() {}

        @Override
        public void trim(long unloadDuration) {}
    };


}
