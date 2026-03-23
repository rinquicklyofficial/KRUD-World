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

package dev.krud.world.core.scripting.func;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface UpdateExecutor {

    @NotNull Runnable wrap(int delay, @NotNull Runnable runnable);

    @NotNull
    default Runnable wrap(@NotNull Runnable runnable) {
        return wrap(1, runnable);
    }

    default void execute(@NotNull Runnable runnable) {
        execute(1, runnable);
    }

    default void execute(int delay, @NotNull Runnable runnable) {
        wrap(delay, runnable).run();
    }
}
