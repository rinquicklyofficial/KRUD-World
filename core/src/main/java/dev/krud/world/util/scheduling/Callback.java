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

/**
 * Callback for async workers
 *
 * @param <T> the type of object to be returned in the runnable
 * @author cyberpwn
 */
@FunctionalInterface
public interface Callback<T> {
    /**
     * Called when the callback calls back...
     *
     * @param t the object to be called back
     */
    void run(T t);
}
