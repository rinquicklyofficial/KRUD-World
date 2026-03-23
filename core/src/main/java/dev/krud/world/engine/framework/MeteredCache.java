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

import dev.krud.world.util.data.KCache;

public interface MeteredCache {
    long getSize();

    KCache<?, ?> getRawCache();

    long getMaxSize();

    default double getUsage() {
        return (double) getSize() / (double) getMaxSize();
    }

    boolean isClosed();
}
