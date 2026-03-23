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

package dev.krud.world.util.scheduling;

import dev.krud.world.util.collection.KList;

@SuppressWarnings("ALL")
public interface Queue<T> {
    static <T> Queue<T> create(KList<T> t) {
        return new ShurikenQueue<T>().queue(t);
    }

    @SuppressWarnings("unchecked")
    static <T> Queue<T> create(T... t) {
        return new ShurikenQueue<T>().queue(new KList<T>().add(t));
    }

    Queue<T> queue(T t);

    Queue<T> queue(KList<T> t);

    boolean hasNext(int amt);

    boolean hasNext();

    T next();

    KList<T> next(int amt);

    Queue<T> clear();

    int size();

    boolean contains(T p);
}
