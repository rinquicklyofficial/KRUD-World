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

public class ZoomStream<T> extends BasicStream<T> {
    private final double ox;
    private final double oy;
    private final double oz;

    public ZoomStream(ProceduralStream<T> stream, double x, double y, double z) {
        super(stream);
        this.ox = x;
        this.oy = y;
        this.oz = z;
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
        return getTypedSource().get(x / ox, z / oz);
    }

    @Override
    public T get(double x, double y, double z) {
        return getTypedSource().get(x / ox, y / oy, z / oz);
    }

}
