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

package dev.krud.world.engine.framework;

import dev.krud.world.KrudWorld;

public interface Fallible {
    default void fail(String error) {
        try {
            throw new RuntimeException();
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            fail(error, e);
        }
    }

    default void fail(Throwable e) {
        fail("Failed to generate", e);
    }

    void fail(String error, Throwable e);

    boolean hasFailed();
}
