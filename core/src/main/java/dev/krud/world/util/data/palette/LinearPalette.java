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

package dev.krud.world.util.data.palette;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LinearPalette<T> implements Palette<T> {
    private final AtomicReferenceArray<T> values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;
    private int size;

    public LinearPalette(int var1, PaletteResize<T> var2) {
        this.values = new AtomicReferenceArray<>(1 << var1);
        this.bits = var1;
        this.resizeHandler = var2;
    }

    public int idFor(T var0) {
        int var1;
        for (var1 = 0; var1 < size; var1++) {
            if (values.get(var1) == null && var0 == null) {
                return var1;
            }

            if (values.get(var1) != null && values.get(var1).equals(var0)) {
                return var1;
            }
        }
        var1 = size;
        if (var1 < values.length()) {
            values.set(var1, var0);
            size++;
            return var1;
        }
        return resizeHandler.onResize(bits + 1, var0);
    }

    public T valueFor(int var0) {
        if (var0 >= 0 && var0 < size) {
            return this.values.get(var0);
        }
        return null;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void read(List<T> fromList) {
        for (int i = 0; i < fromList.size(); i++) {
            values.set(i, fromList.get(i));
        }

        size = fromList.size();
    }

    @Override
    public void write(List<T> toList) {
        for (int i = 0; i < size; i++) {
            T v = values.get(i);
            toList.add(v);
        }
    }
}
