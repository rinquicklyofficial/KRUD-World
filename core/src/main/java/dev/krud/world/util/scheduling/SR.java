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

import dev.krud.world.util.plugin.CancellableTask;

public abstract class SR implements Runnable, CancellableTask {
    private int id = 0;

    public SR() {
        this(0);
    }

    public SR(int interval) {
        id = J.sr(this, interval);
    }

    @Override
    public void cancel() {
        J.csr(id);
    }

    public int getId() {
        return id;
    }
}
