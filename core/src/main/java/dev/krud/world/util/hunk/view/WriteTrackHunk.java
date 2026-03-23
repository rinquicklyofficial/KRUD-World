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

package dev.krud.world.util.hunk.view;

import dev.krud.world.util.hunk.Hunk;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("ClassCanBeRecord")
public class WriteTrackHunk<T> implements Hunk<T> {
    private final Hunk<T> src;
    private final AtomicBoolean b;

    public WriteTrackHunk(Hunk<T> src, AtomicBoolean b) {
        this.src = src;
        this.b = b;
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        if (!b.get()) {
            b.set(true);
        }

        src.setRaw(x, y, z, t);
    }

    @Override
    public T getRaw(int x, int y, int z) {
        return src.getRaw(x, y, z);
    }

    @Override
    public int getWidth() {
        return src.getWidth();
    }

    @Override
    public int getHeight() {
        return src.getHeight();
    }

    @Override
    public int getDepth() {
        return src.getDepth();
    }

    @Override
    public Hunk<T> getSource() {
        return src;
    }
}
