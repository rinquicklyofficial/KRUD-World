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

package dev.krud.world.util.parallel;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class AtomicBooleanArray implements Serializable {
    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(boolean[].class);
    private final boolean[] array;

    public AtomicBooleanArray(int length) {
        array = new boolean[length];
    }

    public final int length() {
        return array.length;
    }

    public final boolean get(int index) {
        return (boolean) AA.getVolatile(array, index);
    }

    public final void set(int index, boolean newValue) {
        AA.setVolatile(array, index, newValue);
    }

    public final boolean getAndSet(int index, boolean newValue) {
        return (boolean) AA.getAndSet(array, index, newValue);
    }

    public final boolean compareAndSet(int index, boolean expectedValue, boolean newValue) {
        return (boolean) AA.compareAndSet(array, index, expectedValue, newValue);
    }

    @Override
    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(get(i));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }
}
