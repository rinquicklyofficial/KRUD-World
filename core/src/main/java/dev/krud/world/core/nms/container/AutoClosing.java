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

package dev.krud.world.core.nms.container;

import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.function.NastyRunnable;
import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class AutoClosing implements AutoCloseable {
    private static final KMap<Thread, AutoClosing> CONTEXTS = new KMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final NastyRunnable action;

    @Override
    public void close() {
        if (closed.getAndSet(true)) return;
        try {
            removeContext();
            action.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void storeContext() {
        CONTEXTS.put(Thread.currentThread(), this);
    }

    public void removeContext() {
        CONTEXTS.values().removeIf(c -> c == this);
    }

    public static void closeContext() {
        AutoClosing closing = CONTEXTS.remove(Thread.currentThread());
        if (closing == null) return;
        closing.close();
    }
}
