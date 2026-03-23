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
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.function.NastyRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

public class GroupedExecutor {
    private final ExecutorService service;
    private final KMap<String, Integer> mirror;
    private int xc;

    public GroupedExecutor(int threadLimit, int priority, String name) {
        xc = 1;
        mirror = new KMap<>();

        if (threadLimit == 1) {
            service = Executors.newSingleThreadExecutor((r) ->
            {
                Thread t = new Thread(r);
                t.setName(name);
                t.setPriority(priority);

                return t;
            });
        } else if (threadLimit > 1) {
            final ForkJoinWorkerThreadFactory factory = pool -> {
                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                worker.setName(name + " " + xc++);
                worker.setPriority(priority);
                return worker;
            };

            service = new ForkJoinPool(threadLimit, factory, null, false);
        } else {
            service = Executors.newCachedThreadPool((r) ->
            {
                Thread t = new Thread(r);
                t.setName(name + " " + xc++);
                t.setPriority(priority);

                return t;
            });
        }
    }

    public void waitFor(String g) {
        if (g == null) {
            return;
        }

        if (!mirror.containsKey(g)) {
            return;
        }

        while (true) {
            if (mirror.get(g) == 0) {
                break;
            }
        }
    }

    public void queue(String q, NastyRunnable r) {
        mirror.compute(q, (k, v) -> k == null || v == null ? 1 : v + 1);
        service.execute(() ->
        {
            try {
                r.run();
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }

            mirror.computeIfPresent(q, (k, v) -> v - 1);
        });
    }

    public void close() {
        J.a(() ->
        {
            J.sleep(100);
            service.shutdown();
        });
    }

    public void closeNow() {
        service.shutdown();
    }
}
