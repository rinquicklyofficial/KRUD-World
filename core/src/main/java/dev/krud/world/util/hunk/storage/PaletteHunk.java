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

import dev.krud.world.util.function.Consumer4;
import dev.krud.world.util.function.Consumer4IO;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.hunk.bits.DataContainer;
import dev.krud.world.util.hunk.bits.Writable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;

@SuppressWarnings({"Lombok"})
@Data
@EqualsAndHashCode(callSuper = false)
public class PaletteHunk<T> extends StorageHunk<T> implements Hunk<T> {
    private DataContainer<T> data;

    public PaletteHunk(int w, int h, int d, Writable<T> writer) {
        super(w, h, d);
        data = new DataContainer<>(writer, w * h * d);
    }

    public void setPalette(DataContainer<T> c) {
        data = c;
    }

    public boolean isMapped() {
        return false;
    }

    private int index(int x, int y, int z) {
        return (z * getWidth() * getHeight()) + (y * getWidth()) + x;
    }

    @Override
    public synchronized Hunk<T> iterateSync(Consumer4<Integer, Integer, Integer, T> c) {
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                for (int k = 0; k < getDepth(); k++) {
                    T t = getRaw(i, j, k);
                    if (t != null) {
                        c.accept(i, j, k, t);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public synchronized Hunk<T> iterateSyncIO(Consumer4IO<Integer, Integer, Integer, T> c) throws IOException {
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                for (int k = 0; k < getDepth(); k++) {
                    T t = getRaw(i, j, k);
                    if (t != null) {
                        c.accept(i, j, k, t);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        data.set(index(x, y, z), t);
    }

    @Override
    public T getRaw(int x, int y, int z) {
        return data.get(index(x, y, z));
    }
}
