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

import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class BurstExecutor {
    private final ExecutorService executor;
    @Getter
    private final KList<Future<?>> futures;
    @Setter
    private boolean multicore = true;

    public BurstExecutor(ExecutorService executor, int burstSizeEstimate) {
        this.executor = executor;
        futures = new KList<Future<?>>(burstSizeEstimate);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Future<?> queue(Runnable r) {
        if (!multicore) {
            r.run();
            return CompletableFuture.completedFuture(null);
        }

        synchronized (futures) {

            Future<?> c = executor.submit(r);
            futures.add(c);
            return c;
        }
    }

    public BurstExecutor queue(List<Runnable> r) {
        if (!multicore) {
            for (Runnable i : new KList<>(r)) {
                i.run();
            }

            return this;
        }

        synchronized (futures) {
            for (Runnable i : new KList<>(r)) {
                queue(i);
            }
        }

        return this;
    }

    public BurstExecutor queue(Runnable[] r) {
        if (!multicore) {
            for (Runnable i : new KList<>(r)) {
                i.run();
            }

            return this;
        }

        synchronized (futures) {
            for (Runnable i : r) {
                queue(i);
            }
        }

        return this;
    }

    public void complete() {
        if (!multicore) {
            return;
        }

        synchronized (futures) {
            if (futures.isEmpty()) {
                return;
            }

            try {
                for (Future<?> i : futures) {
                    i.get();
                }

                futures.clear();
            } catch (InterruptedException | ExecutionException e) {
                KrudWorld.reportError(e);
            }
        }
    }
}
