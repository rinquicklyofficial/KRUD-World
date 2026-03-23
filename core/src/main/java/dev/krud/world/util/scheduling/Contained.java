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

import java.util.function.Function;

public class Contained<T> {
    private T t;

    public void mod(Function<T, T> x) {
        set(x.apply(t));
    }

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }
}
