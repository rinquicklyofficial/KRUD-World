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

package dev.krud.world.util.format;

import dev.krud.world.util.math.RollingSequence;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.Looper;

public class MemoryMonitor {
    private final ChronoLatch cl;
    private final RollingSequence pressureAvg;
    private final Runtime runtime;
    private Looper looper;
    private long usedMemory;
    private long garbageMemory;
    private long garbageLast;
    private long garbageBin;
    private long pressure;

    public MemoryMonitor(int sampleDelay) {
        this.runtime = Runtime.getRuntime();
        usedMemory = -1;
        pressureAvg = new RollingSequence(Math.max(Math.min(100, 1000 / sampleDelay), 3));
        garbageBin = 0;
        garbageMemory = -1;
        cl = new ChronoLatch(1000);
        garbageLast = 0;
        pressure = 0;

        looper = new Looper() {
            @Override
            protected long loop() {
                sample();
                return sampleDelay;
            }
        };
        looper.setPriority(Thread.MIN_PRIORITY);
        looper.setName("Memory Monitor");
        looper.start();
    }

    public long getGarbageBytes() {
        return garbageMemory;
    }

    public long getUsedBytes() {
        return usedMemory;
    }

    public long getMaxBytes() {
        return runtime.maxMemory();
    }

    public long getPressure() {
        return (long) pressureAvg.getAverage();
    }

    public double getUsagePercent() {
        return usedMemory / (double) getMaxBytes();
    }

    @SuppressWarnings("IfStatementWithIdenticalBranches")
    private void sample() {
        long used = getVMUse();
        if (usedMemory == -1) {
            usedMemory = used;
            garbageMemory = 0;
            return;
        }

        if (used < usedMemory) {
            usedMemory = used;
        } else {
            garbageMemory = used - usedMemory;
        }

        long g = garbageMemory - garbageLast;

        if (g >= 0) {
            garbageBin += g;
            garbageLast = garbageMemory;
        } else {
            garbageMemory = 0;
            garbageLast = 0;
        }

        if (cl.flip()) {
            if (garbageMemory > 0) {
                pressure = garbageBin;
                garbageBin = 0;
            } else {
                pressure = 0;
                garbageBin = 0;
            }
        }

        pressureAvg.put(pressure);
    }

    private long getVMUse() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public void close() {
        if (looper != null) {
            looper.interrupt();
            looper = null;
        }
    }
}
