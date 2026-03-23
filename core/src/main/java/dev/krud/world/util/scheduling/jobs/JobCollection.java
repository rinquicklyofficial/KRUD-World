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

public class JobCollection implements Job {
    private final String name;
    private final KList<Job> jobs;
    private String status;

    public JobCollection(String name, Job... jobs) {
        this(name, new KList<>(jobs));
    }

    public JobCollection(String name, KList<Job> jobs) {
        this.name = name;
        status = null;
        this.jobs = new KList<>(jobs);
    }

    @Override
    public String getName() {
        return status == null ? name : (name + " 》" + status);
    }

    @Override
    public void execute() {
        for (Job i : jobs) {
            status = i.getName();
            i.execute();
        }

        status = null;
    }

    @Override
    public void completeWork() {

    }

    @Override
    public int getTotalWork() {
        return jobs.stream().mapToInt(Job::getTotalWork).sum();
    }

    @Override
    public int getWorkCompleted() {
        return jobs.stream().mapToInt(Job::getWorkCompleted).sum();
    }
}
