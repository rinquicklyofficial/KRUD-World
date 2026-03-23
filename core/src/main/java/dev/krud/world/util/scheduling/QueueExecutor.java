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

public class QueueExecutor extends Looper {
    private final Queue<Runnable> queue;
    private boolean shutdown;

    public QueueExecutor() {
        queue = new ShurikenQueue<>();
        shutdown = false;
    }

    public Queue<Runnable> queue() {
        return queue;
    }

    @Override
    protected long loop() {
        while (queue.hasNext()) {
            try {
                queue.next().run();
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
        }

        if (shutdown && !queue.hasNext()) {
            interrupt();
            return -1;
        }

        return Math.max(500, (long) getRunTime() * 10);
    }

    public double getRunTime() {
        return 0;
    }

    public void shutdown() {
        shutdown = true;
    }
}
