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


import com.google.common.util.concurrent.AtomicDoubleArray;

import java.util.Arrays;

public class DoubleArrayUtils {
    public static void shiftRight(double[] values, double push) {
        if (values.length - 2 + 1 >= 0) System.arraycopy(values, 0, values, 1, values.length - 2 + 1);

        values[0] = push;
    }

    public static void wrapRight(double[] values) {
        double last = values[values.length - 1];
        shiftRight(values, last);
    }

    public static void fill(double[] values, double value) {
        Arrays.fill(values, value);
    }

    public static void fill(AtomicDoubleArray values, double value) {
        for (int i = 0; i < values.length(); i++) {
            values.set(i, value);
        }
    }

}
