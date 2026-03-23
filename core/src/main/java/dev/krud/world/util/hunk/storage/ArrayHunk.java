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

import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.util.hunk.Hunk;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

@SuppressWarnings("Lombok")
@Data
@EqualsAndHashCode(callSuper = false)
public class ArrayHunk<T> extends StorageHunk<T> implements Hunk<T> {
    private final T[] data;

    @SuppressWarnings("unchecked")
    public ArrayHunk(int w, int h, int d) {
        super(w, h, d);
        data = (T[]) new Object[w * h * d];
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        data[index(x, y, z)] = t;
    }

    @Override
    public T getRaw(int x, int y, int z) {
        return data[index(x, y, z)];
    }

    private int index(int x, int y, int z) {
        return Cache.to1D(x, y, z, getWidth(), getHeight());
    }

    @Override
    public void fill(T t) {
        Arrays.fill(data, t);
    }
}
