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

public class Wrapper<T> {
    private T t;

    public Wrapper(T t) {
        set(t);
    }

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((t == null) ? 0 : t.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Wrapper<?> other)) {
            return false;
        }

        if (t == null) {
            return other.t == null;
        } else return t.equals(other.t);
    }

    @Override
    public String toString() {
        if (t != null) {
            return get().toString();
        }

        return super.toString() + " (null)";
    }
}
