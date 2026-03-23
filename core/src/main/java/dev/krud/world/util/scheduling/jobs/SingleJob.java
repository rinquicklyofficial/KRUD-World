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

public class SingleJob implements Job {
    private final String name;
    private final Runnable runnable;
    private boolean done;

    public SingleJob(String name, Runnable runnable) {
        this.name = name;
        done = false;
        this.runnable = runnable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute() {
        runnable.run();
        completeWork();
    }

    @Override
    public void completeWork() {
        done = true;
    }

    @Override
    public int getTotalWork() {
        return 1;
    }

    @Override
    public int getWorkCompleted() {
        return done ? 1 : 0;
    }
}
