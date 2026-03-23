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

import dev.krud.world.util.hunk.bits.DataContainer;
import lombok.Getter;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class DataBitCache<T> implements ArrayCache<T> {
    @Getter
    private final int width;
    @Getter
    private final int height;
    private final DataContainer<T> cache;

    public DataBitCache(int width, int height) {
        this.width = width;
        this.height = height;
        cache = new DataContainer<>(this, width * height);
    }

    public void set(int i, T v) {
        cache.set(i, v);
    }

    public T get(int i) {
        return cache.get(i);
    }

    @Override
    public void writeCache(DataOutputStream dos) throws IOException {
        dos.writeInt(width);
        dos.writeInt(height);
        cache.writeDos(dos);
    }
}
