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

package dev.krud.world.util.data;

import dev.krud.world.util.collection.KList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataPalette<T> {
    private final KList<T> palette;

    public DataPalette() {
        this(new KList<>(16));
    }

    public DataPalette(KList<T> palette) {
        this.palette = palette;
    }

    public static <T> DataPalette<T> getPalette(IOAdapter<T> adapter, DataInputStream din) throws IOException {
        KList<T> palette = new KList<>();
        int s = din.readShort() - Short.MIN_VALUE;

        for (int i = 0; i < s; i++) {
            palette.add(adapter.read(din));
        }

        return new DataPalette<>(palette);
    }

    public KList<T> getPalette() {
        return palette;
    }

    public T get(int index) {
        synchronized (palette) {
            if (!palette.hasIndex(index)) {
                return null;
            }

            return palette.get(index);
        }
    }

    public int getIndex(T t) {
        int v = 0;

        synchronized (palette) {
            v = palette.indexOf(t);

            if (v == -1) {
                v = palette.size();
                palette.add(t);
            }
        }

        return v;
    }

    public void write(IOAdapter<T> adapter, DataOutputStream dos) throws IOException {
        synchronized (palette) {
            dos.writeShort(getPalette().size() + Short.MIN_VALUE);

            for (T t : palette) {
                adapter.write(t, dos);
            }
        }
    }
}
