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
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.parallel.MultiBurst;

public abstract class ParallelQueueJob<T> extends QueueJob<T> {
    @Override
    public void execute() {
        while (queue.isNotEmpty()) {
            BurstExecutor b = MultiBurst.burst.burst(queue.size());
            KList<T> q = queue.copy();
            queue.clear();
            for (T i : q) {
                b.queue(() -> {
                    execute(i);
                    completeWork();
                });
            }
            b.complete();
        }
    }
}
