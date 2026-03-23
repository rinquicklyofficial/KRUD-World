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

import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.function.Consumer2;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HashPalette<T> implements Palette<T> {
    private final Object lock = new Object();
    private final KMap<T, Integer> palette;
    private final KMap<Integer, T> lookup;
    private final AtomicInteger size;

    public HashPalette() {
        this.size = new AtomicInteger(1);
        this.palette = new KMap<>();
        this.lookup = new KMap<>();
    }

    @Override
    public T get(int id) {
        if (id <= 0 || id >= size.get()) {
            return null;
        }

        return lookup.get(id);
    }

    @Override
    public int add(T t) {
        if (t == null) {
            return 0;
        }

        return palette.computeIfAbsent(t, $ -> {
            synchronized (lock) {
                int index = size.getAndIncrement();
                lookup.put(index, t);
                return index;
            }
        });
    }

    @Override
    public int id(T t) {
        if (t == null) {
            return 0;
        }

        Integer v = palette.get(t);
        return v != null ? v : -1;
    }

    @Override
    public int size() {
        return size.get() - 1;
    }

    @Override
    public void iterate(Consumer2<T, Integer> c) {
        synchronized (lock) {
            for (int i = 1; i < size.get(); i++) {
                c.accept(lookup.get(i), i);
            }
        }
    }

    @Override
    public Palette<T> from(Palette<T> oldPalette) {
        oldPalette.iterate((t, i) -> {
            if (t == null) throw new NullPointerException("Null palette entries are not allowed!");
            lookup.put(i, t);
            palette.put(t, i);
        });
        size.set(oldPalette.size() + 1);
        return this;
    }

    @Override
    public Palette<T> from(int size, Writable<T> writable, DataInputStream in) throws IOException {
        for (int i = 1; i <= size; i++) {
            T t = writable.readNodeData(in);
            if (t == null) throw new NullPointerException("Null palette entries are not allowed!");
            lookup.put(i, t);
            palette.put(t, i);
        }
        this.size.set(size + 1);
        return this;
    }
}
