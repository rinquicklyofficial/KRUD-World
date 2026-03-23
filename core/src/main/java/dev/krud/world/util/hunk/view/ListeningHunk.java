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

import dev.krud.world.util.function.Consumer4;
import dev.krud.world.util.hunk.Hunk;

@SuppressWarnings("ClassCanBeRecord")
public class ListeningHunk<T> implements Hunk<T> {
    private final Hunk<T> src;
    private final Consumer4<Integer, Integer, Integer, T> listener;

    public ListeningHunk(Hunk<T> src, Consumer4<Integer, Integer, Integer, T> listener) {
        this.src = src;
        this.listener = listener;
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        listener.accept(x, y, z, t);
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
