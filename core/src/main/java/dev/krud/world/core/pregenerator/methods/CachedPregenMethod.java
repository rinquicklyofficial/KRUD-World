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

package dev.krud.world.core.pregenerator.methods;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.pregenerator.PregenListener;
import dev.krud.world.core.pregenerator.PregeneratorMethod;
import dev.krud.world.core.pregenerator.cache.PregenCache;
import dev.krud.world.core.service.GlobalCacheSVC;
import dev.krud.world.util.mantle.Mantle;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CachedPregenMethod implements PregeneratorMethod {
    private final PregeneratorMethod method;
    private final PregenCache cache;

    public CachedPregenMethod(PregeneratorMethod method, String worldName) {
        this.method = method;
        var cache = KrudWorld.service(GlobalCacheSVC.class).get(worldName);
        if (cache == null) {
            KrudWorld.debug("Could not find existing cache for " + worldName  + " creating fallback");
            cache = GlobalCacheSVC.createDefault(worldName);
        }
        this.cache = cache;
    }

    @Override
    public void init() {
        method.init();
    }

    @Override
    public void close() {
        method.close();
        cache.write();
    }

    @Override
    public void save() {
        method.save();
        cache.write();
    }

    @Override
    public boolean supportsRegions(int x, int z, PregenListener listener) {
        return cache.isRegionCached(x, z) || method.supportsRegions(x, z, listener);
    }

    @Override
    public String getMethod(int x, int z) {
        return method.getMethod(x, z);
    }

    @Override
    public void generateRegion(int x, int z, PregenListener listener) {
        if (cache.isRegionCached(x, z)) {
            listener.onRegionGenerated(x, z);

            int rX = x << 5, rZ = z << 5;
            for (int cX = 0; cX < 32; cX++) {
                for (int cZ = 0; cZ < 32; cZ++) {
                    listener.onChunkGenerated(rX + cX, rZ + cZ, true);
                    listener.onChunkCleaned(rX + cX, rZ + cZ);
                }
            }
            return;
        }
        method.generateRegion(x, z, listener);
        cache.cacheRegion(x, z);
    }

    @Override
    public void generateChunk(int x, int z, PregenListener listener) {
        if (cache.isChunkCached(x, z)) {
            listener.onChunkGenerated(x, z, true);
            listener.onChunkCleaned(x, z);
            return;
        }
        method.generateChunk(x, z, listener);
        cache.cacheChunk(x, z);
    }

    @Override
    public Mantle getMantle() {
        return method.getMantle();
    }
}
