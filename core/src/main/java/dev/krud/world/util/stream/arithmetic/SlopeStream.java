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

public class SlopeStream<T> extends BasicStream<T> {
    private final int range;

    public SlopeStream(ProceduralStream<T> stream, int range) {
        super(stream);
        this.range = range;
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
        double height = getTypedSource().getDouble(x, z);
        double dx = getTypedSource().getDouble(x + range, z) - height;
        double dy = getTypedSource().getDouble(x, z + range) - height;

        return fromDouble(Math.sqrt(dx * dx + dy * dy));
    }

    @Override
    public T get(double x, double y, double z) {
        double height = getTypedSource().getDouble(x, y, z);
        double dx = getTypedSource().getDouble(x + range, y, z) - height;
        double dy = getTypedSource().getDouble(x, y + range, z) - height;
        double dz = getTypedSource().getDouble(x, y, z + range) - height;

        return fromDouble(Math.cbrt((dx * dx) + (dy * dy) + (dz * dz)));
    }

}
