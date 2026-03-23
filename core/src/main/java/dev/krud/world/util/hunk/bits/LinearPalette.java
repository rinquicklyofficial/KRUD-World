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

package dev.krud.world.util.hunk.bits;

import dev.krud.world.util.function.Consumer2;
import lombok.Synchronized;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LinearPalette<T> implements Palette<T> {
    private volatile AtomicReferenceArray<T> palette;
    private final AtomicInteger size;

    public LinearPalette(int initialSize) {
        this.size = new AtomicInteger(1);
        this.palette = new AtomicReferenceArray<>(initialSize);
        palette.set(0, null);
    }

    @Override
    public T get(int id) {
        if (id < 0 || id >= size.get()) {
            return null;
        }

        return palette.get(id);
    }

    @Override
    public int add(T t) {
        int index = size.getAndIncrement();
        if (palette.length() <= index)
            grow(index);
        palette.set(index, t);
        return index;
    }

    private synchronized void grow(int lastIndex) {
        if (palette.length() > lastIndex)
            return;

        AtomicReferenceArray<T> a = new AtomicReferenceArray<>(lastIndex + 1);
        for (int i = 0; i < palette.length(); i++) {
            a.set(i, palette.get(i));
        }

        palette = a;
    }

    @Override
    public int id(T t) {
        if (t == null) {
            return 0;
        }

        for (int i = 1; i < size.get(); i++) {
            if (t.equals(palette.get(i))) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int size() {
        return size.get() - 1;
    }

    @Override
    public void iterate(Consumer2<T, Integer> c) {
        for (int i = 1; i <= size(); i++) {
            c.accept(palette.get(i), i);
        }
    }
}
