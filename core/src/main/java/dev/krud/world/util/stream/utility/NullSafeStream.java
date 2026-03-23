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

public class NullSafeStream<T> extends BasicStream<T> implements ProceduralStream<T> {
    private final ProceduralStream<T> stream;
    private final T ifNull;

    public NullSafeStream(ProceduralStream<T> stream, T ifNull) {
        super();
        this.stream = stream;
        this.ifNull = ifNull;
    }

    @Override
    public double toDouble(T t) {
        return stream.toDouble(t);
    }

    @Override
    public T fromDouble(double d) {
        return stream.fromDouble(d);
    }

    @Override
    public T get(double x, double z) {
        T t = stream.get(x, z);

        if (t == null) {
            return ifNull;
        }

        return t;
    }

    @Override
    public T get(double x, double y, double z) {
        T t = stream.get(x, y, z);

        if (t == null) {
            return ifNull;
        }

        return t;
    }
}
