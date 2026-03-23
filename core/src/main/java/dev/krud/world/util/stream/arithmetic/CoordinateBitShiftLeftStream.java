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

package dev.krud.world.util.stream.arithmetic;

import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class CoordinateBitShiftLeftStream<T> extends BasicStream<T> implements ProceduralStream<T> {
    private final int amount;

    public CoordinateBitShiftLeftStream(ProceduralStream<T> stream, int amount) {
        super(stream);
        this.amount = amount;
    }

    @Override
    public double toDouble(T t) {
        return getTypedSource().toDouble(t);
    }

    @Override
    public T fromDouble(double d) {
        return getTypedSource().fromDouble(d);
    }

    @Override
    public T get(double x, double z) {
        return getTypedSource().get((int) x << amount, (int) z << amount);
    }

    @Override
    public T get(double x, double y, double z) {
        return getTypedSource().get((int) x << amount, (int) y << amount, (int) z << amount);
    }

}
