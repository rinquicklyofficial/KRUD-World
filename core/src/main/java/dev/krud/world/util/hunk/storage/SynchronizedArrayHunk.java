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

package dev.krud.world.util.hunk.storage;

import dev.krud.world.util.hunk.Hunk;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

@SuppressWarnings({"Lombok"})
@Data
@EqualsAndHashCode(callSuper = false)
public class SynchronizedArrayHunk<T> extends StorageHunk<T> implements Hunk<T> {
    private final T[] data;

    @SuppressWarnings("unchecked")
    public SynchronizedArrayHunk(int w, int h, int d) {
        super(w, h, d);
        data = (T[]) new Object[w * h * d];
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        synchronized (data) {
            data[index(x, y, z)] = t;
        }
    }

    @Override
    public T getRaw(int x, int y, int z) {
        synchronized (data) {
            return data[index(x, y, z)];
        }
    }

    private int index(int x, int y, int z) {
        return (z * getWidth() * getHeight()) + (y * getWidth()) + x;
    }

    @Override
    public void fill(T t) {
        synchronized (data) {
            Arrays.fill(data, t);
        }
    }
}
