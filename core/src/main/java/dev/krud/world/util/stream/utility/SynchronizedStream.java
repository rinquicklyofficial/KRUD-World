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

package dev.krud.world.util.stream.utility;

import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class SynchronizedStream<T> extends BasicStream<T> {
    public SynchronizedStream(ProceduralStream<T> stream) {
        super(stream);
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
        synchronized (getTypedSource()) {
            return getTypedSource().get(x, z);
        }
    }

    @Override
    public T get(double x, double y, double z) {
        synchronized (getTypedSource()) {
            return getTypedSource().get(x, y, z);
        }
    }
}
