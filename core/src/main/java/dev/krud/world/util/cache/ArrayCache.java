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

package dev.krud.world.util.cache;

import dev.krud.world.util.hunk.bits.Writable;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ArrayCache<T> extends Writable<T> {
    static int zigZag(int coord, int size) {
        if (coord < 0) {
            coord = Math.abs(coord);
        }

        if (coord % (size * 2) >= size) {
            return (size) - (coord % size) - 1;
        } else {
            return coord % size;
        }
    }

    T get(int i);

    void set(int i, T t);

    void iset(int i, int v);

    int getWidth();

    int getHeight();

    void writeCache(DataOutputStream dos) throws IOException;

    default void set(int x, int y, T v) {
        set((zigZag(y, getHeight()) * getWidth()) + zigZag(x, getWidth()), v);
    }

    default T get(int x, int y) {
        try {
            return get((zigZag(y, getHeight()) * getWidth()) + zigZag(x, getWidth()));
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    default void iset(int x, int y, int v) {
        iset((zigZag(y, getHeight()) * getWidth()) + zigZag(x, getWidth()), v);
    }
}
