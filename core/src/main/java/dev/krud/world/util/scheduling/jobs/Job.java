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

import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.PrecisionStopwatch;

import java.util.concurrent.CompletableFuture;

public interface Job {
    String getName();

    void execute();

    void completeWork();

    int getTotalWork();

    default int getWorkRemaining() {
        return getTotalWork() - getWorkCompleted();
    }

    int getWorkCompleted();

    default String getProgressString() {
        return Form.pc(getProgress(), 0);
    }

    default double getProgress() {
        return (double) getWorkCompleted() / (double) getTotalWork();
    }


    default void execute(VolmitSender sender) {
        execute(sender, () -> {
        });
    }


    default void execute(VolmitSender sender, Runnable whenComplete) {
        execute(sender, false, whenComplete);
    }

    default void execute(VolmitSender sender, boolean silentMsg, Runnable whenComplete) {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        CompletableFuture<?> f = J.afut(this::execute);
        int c = J.ar(() -> {
            if (sender.isPlayer()) {
                sender.sendProgress(getProgress(), getName());
            } else {
                sender.sendMessage(getName() + ": " + getProgressString());
            }
        }, sender.isPlayer() ? 0 : 20);
        f.whenComplete((fs, ff) -> {
            J.car(c);
            if (!silentMsg) {
                sender.sendMessage(C.AQUA + "Completed " + getName() + " in " + Form.duration(p.getMilliseconds(), 1));
            }
            whenComplete.run();
        });
    }
}
