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

package dev.krud.world.util.scheduling;

public class ChronoLatch {
    private final long interval;
    private long since;

    public ChronoLatch(long interval, boolean openedAtStart) {
        this.interval = interval;
        since = System.currentTimeMillis() - (openedAtStart ? interval * 2 : 0);
    }

    public ChronoLatch(long interval) {
        this(interval, true);
    }

    public void flipDown() {
        since = System.currentTimeMillis();
    }

    public boolean couldFlip() {
        return System.currentTimeMillis() - since > interval;
    }

    public boolean flip() {
        if (System.currentTimeMillis() - since > interval) {
            since = System.currentTimeMillis();
            return true;
        }

        return false;
    }
}
