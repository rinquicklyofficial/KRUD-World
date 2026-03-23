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

public class O<T> implements Observable<T> {
    private T t = null;
    private KList<Observer<T>> observers;

    @Override
    public T get() {
        return t;
    }

    @Override
    public O<T> set(T t) {
        this.t = t;

        if (observers != null && observers.hasElements()) {
            observers.forEach((o) -> o.onChanged(t, t));
        }

        return this;
    }

    @Override
    public boolean has() {
        return t != null;
    }

    @Override
    public O<T> clearObservers() {
        observers.clear();
        return this;
    }

    @Override
    public O<T> observe(Observer<T> t) {
        if (observers == null) {
            observers = new KList<>();
        }

        observers.add(t);

        return this;
    }
}
