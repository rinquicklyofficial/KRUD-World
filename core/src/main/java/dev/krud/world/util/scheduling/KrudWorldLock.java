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

import dev.krud.world.KrudWorld;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.locks.ReentrantLock;

@Data
@Accessors(
        chain = true
)
public class KrudWorldLock {
    private transient final ReentrantLock lock;
    private transient final String name;
    private transient boolean disabled = false;

    public KrudWorldLock(String name) {
        this.name = name;
        lock = new ReentrantLock(false);
    }

    public void lock() {
        if (disabled) {
            return;
        }

        lock.lock();
    }

    public void unlock() {
        if (disabled) {
            return;
        }
        try {
            lock.unlock();
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }
    }
}
