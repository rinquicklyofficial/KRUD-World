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

import dev.krud.world.util.collection.KList;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class QueueJob<T> implements Job {
    final KList<T> queue;
    private final AtomicInteger completed;
    protected int totalWork;

    public QueueJob() {
        totalWork = 0;
        completed = new AtomicInteger(0);
        queue = new KList<>();
    }

    public QueueJob queue(T t) {
        queue.add(t);
        totalWork++;
        return this;
    }

    public QueueJob queue(KList<T> f) {
        queue.addAll(f);
        totalWork += f.size();
        return this;
    }

    public abstract void execute(T t);

    @Override
    public void execute() {
        totalWork = queue.size();
        while (queue.isNotEmpty()) {
            execute(queue.pop());
            completeWork();
        }
    }

    @Override
    public void completeWork() {
        completed.incrementAndGet();
    }

    @Override
    public int getTotalWork() {
        return totalWork;
    }

    @Override
    public int getWorkCompleted() {
        return completed.get();
    }
}
