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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"Lombok"})
@Data
@EqualsAndHashCode(callSuper = false)
public class MappedSyncHunk<T> extends StorageHunk<T> implements Hunk<T> {
    private final Map<Integer, T> data;

    public MappedSyncHunk(int w, int h, int d) {
        super(w, h, d);
        data = new HashMap<>();
    }

    public int getEntryCount() {
        return data.size();
    }

    public boolean isMapped() {
        return true;
    }

    public boolean isEmpty() {
        synchronized (data) {
            return data.isEmpty();
        }
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        synchronized (data) {
            if (t == null) {
                data.remove(index(x, y, z));
                return;
            }

            data.put(index(x, y, z), t);
        }
    }

    private Integer index(int x, int y, int z) {
        return (z * getWidth() * getHeight()) + (y * getWidth()) + x;
    }

    @Override
    public synchronized Hunk<T> iterateSync(Consumer4<Integer, Integer, Integer, T> c) {
        synchronized (data) {
            int idx, z;

            for (Map.Entry<Integer, T> g : data.entrySet()) {
                idx = g.getKey();
                z = idx / (getWidth() * getHeight());
                idx -= (z * getWidth() * getHeight());
                c.accept(idx % getWidth(), idx / getWidth(), z, g.getValue());
            }

            return this;
        }
    }

    @Override
    public synchronized Hunk<T> iterateSyncIO(Consumer4IO<Integer, Integer, Integer, T> c) throws IOException {
        synchronized (data) {
            int idx, z;

            for (Map.Entry<Integer, T> g : data.entrySet()) {
                idx = g.getKey();
                z = idx / (getWidth() * getHeight());
                idx -= (z * getWidth() * getHeight());
                c.accept(idx % getWidth(), idx / getWidth(), z, g.getValue());
            }

            return this;
        }
    }

    @Override
    public void empty(T b) {
        synchronized (data) {
            data.clear();
        }
    }

    @Override
    public T getRaw(int x, int y, int z) {
        synchronized (data) {
            return data.get(index(x, y, z));
        }
    }
}
