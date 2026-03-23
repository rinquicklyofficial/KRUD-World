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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import dev.krud.world.engine.framework.MeteredCache;
import dev.krud.world.util.math.RollingSequence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KCache<K, V> implements MeteredCache {
    public static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final long max;
    private final LoadingCache<K, V> cache;
    private final boolean fastDump;
    private final RollingSequence msu = new RollingSequence(100);
    private CacheLoader<K, V> loader;

    public KCache(CacheLoader<K, V> loader, long max) {
        this(loader, max, false);
    }

    public KCache(CacheLoader<K, V> loader, long max, boolean fastDump) {
        this.max = max;
        this.fastDump = fastDump;
        this.loader = loader;
        this.cache = create(loader);
    }

    private LoadingCache<K, V> create(CacheLoader<K, V> loader) {
        return Caffeine
                .newBuilder()
                .maximumSize(max)
                .scheduler(Scheduler.systemScheduler())
                .executor(EXECUTOR)
                .initialCapacity((int) (max))
                .build((k) -> loader == null ? null : loader.load(k));
    }


    public void setLoader(CacheLoader<K, V> loader) {
        this.loader = loader;
    }

    public void invalidate(K k) {
        cache.invalidate(k);
    }

    public void invalidate() {
        cache.invalidateAll();
    }

    public V get(K k) {
        return cache.get(k);
    }

    @Override
    public long getSize() {
        return cache.estimatedSize();
    }

    @Override
    public KCache<?, ?> getRawCache() {
        return this;
    }

    @Override
    public long getMaxSize() {
        return max;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    public boolean contains(K next) {
        return cache.getIfPresent(next) != null;
    }
}
