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

package dev.krud.world.util.parallel;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import dev.krud.world.KrudWorld;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.util.function.NastyRunnable;
import dev.krud.world.util.function.NastySupplier;
import dev.krud.world.util.io.IORunnable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class HyperLock {
    private final ConcurrentLinkedHashMap<Long, ReentrantLock> locks;
    private boolean enabled = true;
    private boolean fair = false;

    public HyperLock() {
        this(1024, false);
    }

    public HyperLock(int capacity) {
        this(capacity, false);
    }

    public HyperLock(int capacity, boolean fair) {
        this.fair = fair;
        locks = new ConcurrentLinkedHashMap.Builder<Long, ReentrantLock>()
                .initialCapacity(capacity)
                .maximumWeightedCapacity(capacity)
                .listener((k, v) -> {
                    if (v.isLocked() || v.isHeldByCurrentThread()) {
                        KrudWorld.warn("InfiniLock Eviction of " + k + " still has locks on it!");
                    }
                })
                .concurrencyLevel(32)
                .build();
    }

    public void with(int x, int z, Runnable r) {
        lock(x, z);
        try {
            r.run();
        } finally {
            unlock(x, z);
        }
    }

    public void withLong(long k, Runnable r) {
        int x = Cache.keyX(k), z = Cache.keyZ(k);
        lock(x, z);
        try {
            r.run();
        } finally {
            unlock(x, z);
        }
    }

    public void withNasty(int x, int z, NastyRunnable r) throws Throwable {
        lock(x, z);
        Throwable ee = null;
        try {
            r.run();
        } catch (Throwable e) {
            ee = e;
        } finally {
            unlock(x, z);

            if (ee != null) {
                throw ee;
            }
        }
    }

    public void withIO(int x, int z, IORunnable r) throws IOException {
        lock(x, z);
        IOException ee = null;
        try {
            r.run();
        } catch (IOException e) {
            ee = e;
        } finally {
            unlock(x, z);

            if (ee != null) {
                throw ee;
            }
        }
    }

    public <T> T withResult(int x, int z, Supplier<T> r) {
        lock(x, z);
        try {
            return r.get();
        } finally {
            unlock(x, z);
        }
    }

    public <T> T withNastyResult(int x, int z, NastySupplier<T> r) throws Throwable {
        lock(x, z);
        try {
            return r.get();
        } finally {
            unlock(x, z);
        }
    }

    public boolean tryLock(int x, int z) {
        return getLock(x, z).tryLock();
    }

    public boolean tryLock(int x, int z, long timeout) {
        try {
            return getLock(x, z).tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            KrudWorld.reportError(e);
        }

        return false;
    }

    private ReentrantLock getLock(int x, int z) {
        return locks.computeIfAbsent(Cache.key(x, z), k -> new ReentrantLock(fair));
    }

    public void lock(int x, int z) {
        if (!enabled) {
            return;
        }

        getLock(x, z).lock();
    }

    public void unlock(int x, int z) {
        if (!enabled) {
            return;
        }

        getLock(x, z).unlock();
    }

    public void disable() {
        enabled = false;
        locks.values().forEach(ReentrantLock::lock);
    }
}
