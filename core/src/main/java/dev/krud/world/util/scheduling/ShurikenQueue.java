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

public class ShurikenQueue<T> implements Queue<T> {
    private KList<T> queue;
    private boolean randomPop;
    private boolean reversePop;

    public ShurikenQueue() {
        clear();
    }

    public ShurikenQueue<T> responsiveMode() {
        reversePop = true;
        return this;
    }

    public ShurikenQueue<T> randomMode() {
        randomPop = true;
        return this;
    }

    @Override
    public ShurikenQueue<T> queue(T t) {
        queue.add(t);
        return this;
    }

    @Override
    public ShurikenQueue<T> queue(KList<T> t) {
        queue.add(t);
        return this;
    }

    @Override
    public boolean hasNext(int amt) {
        return queue.size() >= amt;
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        return reversePop ? queue.popLast() : randomPop ? queue.popRandom() : queue.pop();
    }

    @Override
    public KList<T> next(int amt) {
        KList<T> t = new KList<>();

        for (int i = 0; i < amt; i++) {
            if (!hasNext()) {
                break;
            }

            t.add(next());
        }

        return t;
    }

    @Override
    public ShurikenQueue<T> clear() {
        queue = new KList<>();
        return this;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean contains(T p) {
        return queue.contains(p);
    }
}
