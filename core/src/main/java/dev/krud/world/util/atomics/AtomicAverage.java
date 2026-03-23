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

package dev.krud.world.util.atomics;

import com.google.common.util.concurrent.AtomicDoubleArray;
import dev.krud.world.KrudWorld;
import dev.krud.world.util.data.DoubleArrayUtils;

/**
 * Provides an incredibly fast averaging object. It swaps values from a sum
 * using an array. Averages do not use any form of looping. An average of 10,000
 * entries is the same speed as an average with 5 entries.
 *
 * @author cyberpwn
 */
public class AtomicAverage {
    protected final AtomicDoubleArray values;
    protected transient int cursor;
    private transient double average;
    private transient double lastSum;
    private transient boolean dirty;
    private transient boolean brandNew;

    /**
     * Create an average holder
     *
     * @param size the size of entries to keep
     */
    public AtomicAverage(int size) {
        values = new AtomicDoubleArray(size);
        DoubleArrayUtils.fill(values, 0);
        brandNew = true;
        average = 0;
        cursor = 0;
        lastSum = 0;
        dirty = false;
    }

    /**
     * Put a value into the average (rolls over if full)
     *
     * @param i the value
     */
    public synchronized void put(double i) {

        try {
            dirty = true;

            if (brandNew) {
                DoubleArrayUtils.fill(values, i);
                lastSum = size() * i;
                brandNew = false;
                return;
            }

            double current = values.get(cursor);
            lastSum = (lastSum - current) + i;
            values.set(cursor, i);
            cursor = cursor + 1 < size() ? cursor + 1 : 0;
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }
    }

    /**
     * Get the current average
     *
     * @return the average
     */
    public double getAverage() {
        if (dirty) {
            calculateAverage();
            return getAverage();
        }

        return average;
    }

    private void calculateAverage() {
        average = lastSum / (double) size();
        dirty = false;
    }

    public int size() {
        return values.length();
    }

    public boolean isDirty() {
        return dirty;
    }
}
