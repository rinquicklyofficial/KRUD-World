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

import java.util.function.Function;

public class FunctionalHunkView<R, T> implements Hunk<T> {
    private final Hunk<R> src;
    private final Function<R, T> converter;
    private final Function<T, R> backConverter;

    public FunctionalHunkView(Hunk<R> src, Function<R, T> converter, Function<T, R> backConverter) {
        this.src = src;
        this.converter = converter;
        this.backConverter = backConverter;
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        if (backConverter == null) {
            throw new UnsupportedOperationException("You cannot writeNodeData to this hunk (Read Only)");
        }

        src.setRaw(x, y, z, backConverter.apply(t));
    }

    @Override
    public T getRaw(int x, int y, int z) {
        if (converter == null) {
            throw new UnsupportedOperationException("You cannot read this hunk (Write Only)");
        }

        return converter.apply(src.getRaw(x, y, z));
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
        throw new UnsupportedOperationException("You cannot read this hunk's source because it's a different type.");
    }
}
