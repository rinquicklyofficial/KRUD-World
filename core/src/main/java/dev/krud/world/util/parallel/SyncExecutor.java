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

import dev.krud.world.util.math.M;
import dev.krud.world.util.scheduling.SR;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SyncExecutor implements Executor, AutoCloseable {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public SyncExecutor(int msPerTick) {
        new SR() {
            @Override
            public void run() {
                var time = M.ms() + msPerTick;
                while (time > M.ms()) {
                    Runnable r = queue.poll();
                    if (r == null) break;
                    r.run();
                }

                if (closed.get() && queue.isEmpty()) {
                    cancel();
                    latch.countDown();
                }
            }
        };
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (closed.get()) throw new IllegalStateException("Executor is closed!");
        queue.add(command);
    }

    @Override
    public void close() throws Exception {
        closed.set(true);
        latch.await();
    }
}
