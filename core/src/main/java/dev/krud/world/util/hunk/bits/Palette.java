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

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.function.Consumer2;
import dev.krud.world.util.function.Consumer2IO;

import java.io.DataInputStream;
import java.io.IOException;

public interface Palette<T> {
    T get(int id);

    int add(T t);

    int id(T t);

    int size();

    default int bits() {
        return DataContainer.bits(size() + 1);
    }

    void iterate(Consumer2<T, Integer> c);

    default void iterateIO(Consumer2IO<T, Integer> c) {
        iterate((a, b) -> {
            try {
                c.accept(a, b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    default Palette<T> from(int size, Writable<T> writable, DataInputStream in) throws IOException {
        for (int i = 0; i < size; i++) {
            add(writable.readNodeData(in));
        }

        return this;
    }

    default Palette<T> from(Palette<T> oldPalette) {
        oldPalette.iterate((k, v) -> add(k));
        return this;
    }

    default KList<T> list() {
        KList<T> t = new KList<>();
        iterate((tx, __) -> t.add(tx));
        return t;
    }
}
