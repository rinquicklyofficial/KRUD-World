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

public class RotatedZHunkView<T> implements Hunk<T> {
    private final Hunk<T> src;
    private final double sin;
    private final double cos;

    public RotatedZHunkView(Hunk<T> src, double deg) {
        this.src = src;
        this.sin = Math.sin(Math.toRadians(deg));
        this.cos = Math.cos(Math.toRadians(deg));
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        int xc = (int) Math.round(cos * (getWidth() / 2f) - sin * (getHeight() / 2f));
        int yc = (int) Math.round(sin * (getWidth() / 2f) + cos * (getHeight() / 2f));
        src.setIfExists((int) Math.round(cos * (x - xc) - sin * (y - yc)) - xc, (int) Math.round(sin * (x - xc) + cos * (y - yc)) - yc, z, t);
    }

    @Override
    public T getRaw(int x, int y, int z) {
        int xc = (int) Math.round(cos * (getWidth() / 2f) - sin * (getHeight() / 2f));
        int yc = (int) Math.round(sin * (getWidth() / 2f) + cos * (getHeight() / 2f));
        return src.getIfExists((int) Math.round(cos * (x - xc) - sin * (y - yc)) - xc,
                (int) Math.round(sin * (x - xc) + cos * (y - yc)) - yc
                , z);
    }

    @Override
    public int getWidth() {
        return src.getWidth();
    }

    @Override
    public int getDepth() {
        return src.getDepth();
    }

    @Override
    public int getHeight() {
        return src.getHeight();
    }

    @Override
    public Hunk<T> getSource() {
        return src;
    }
}
