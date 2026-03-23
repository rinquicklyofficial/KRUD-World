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

package dev.krud.world.util.scheduling.jobs;

import dev.krud.world.util.math.Spiraler;
import dev.krud.world.util.parallel.MultiBurst;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ParallelRadiusJob implements Job {
    private final ExecutorService service;
    private final AtomicInteger completed;
    private volatile int radiusX, radiusZ;
    private volatile int offsetX, offsetZ;
    private volatile int total;
    private final Semaphore lock;
    private final int lockSize;

    public ParallelRadiusJob(int concurrent) {
        this(concurrent, MultiBurst.burst);
    }

    public ParallelRadiusJob(int concurrent, ExecutorService service) {
        this.service = service;
        completed = new AtomicInteger(0);
        lock = new Semaphore(concurrent);
        lockSize = concurrent;
    }

    public ParallelRadiusJob retarget(int radius, int offsetX, int offsetZ) {
        return retarget(radius, radius, offsetX, offsetZ);
    }

    @Synchronized
    public ParallelRadiusJob retarget(int radiusX, int radiusZ, int offsetX, int offsetZ) {
        completed.set(0);
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        total = (radiusX * 2 + 1) * (radiusZ * 2 + 1);
        return this;
    }

    @Override
    @SneakyThrows
    @Synchronized
    public void execute() {
        new Spiraler(radiusX * 2 + 3, radiusZ * 2 + 3, this::submit).drain();
        lock.acquire(lockSize);
        lock.release(lockSize);
    }

    @SneakyThrows
    private void submit(int x, int z) {
        if (Math.abs(x) > radiusX || Math.abs(z) > radiusZ) return;
        lock.acquire();
        service.submit(() -> {
            try {
                execute(x + offsetX, z + offsetZ);
            } finally {
                completeWork();
            }
        });
    }

    protected abstract void execute(int x, int z);

    @Override
    public void completeWork() {
        completed.incrementAndGet();
        lock.release();
    }

    @Override
    public int getTotalWork() {
        return total;
    }

    @Override
    public int getWorkCompleted() {
        return completed.get();
    }
}
