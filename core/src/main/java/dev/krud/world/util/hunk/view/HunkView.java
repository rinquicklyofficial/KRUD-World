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

public class HunkView<T> implements Hunk<T> {
    private final int ox;
    private final int oy;
    private final int oz;
    private final int w;
    private final int h;
    private final int d;
    private final Hunk<T> src;

    public HunkView(Hunk<T> src) {
        this(src, src.getWidth(), src.getHeight(), src.getDepth());
    }

    public HunkView(Hunk<T> src, int w, int h, int d) {
        this(src, w, h, d, 0, 0, 0);
    }

    public HunkView(Hunk<T> src, int w, int h, int d, int ox, int oy, int oz) {
        this.src = src;
        this.w = w;
        this.h = h;
        this.d = d;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        src.setRaw(x + ox, y + oy, z + oz, t);
    }

    @Override
    public T getRaw(int x, int y, int z) {
        return src.getRaw(x + ox, y + oy, z + oz);
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getDepth() {
        return d;
    }

    @Override
    public int getHeight() {
        return h;
    }

    @Override
    public Hunk<T> getSource() {
        return src;
    }
}
