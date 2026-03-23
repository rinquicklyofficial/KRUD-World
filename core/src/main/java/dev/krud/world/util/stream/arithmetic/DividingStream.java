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

import dev.krud.world.util.function.Function2;
import dev.krud.world.util.function.Function3;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class DividingStream<T> extends BasicStream<T> implements ProceduralStream<T> {
    private final Function3<Double, Double, Double, Double> add;

    public DividingStream(ProceduralStream<T> stream, Function3<Double, Double, Double, Double> add) {
        super(stream);
        this.add = add;
    }

    public DividingStream(ProceduralStream<T> stream, Function2<Double, Double, Double> add) {
        this(stream, (x, y, z) -> add.apply(x, z));
    }

    public DividingStream(ProceduralStream<T> stream, double add) {
        this(stream, (x, y, z) -> add);
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
        return fromDouble(getTypedSource().getDouble(x, z) / add.apply(x, 0D, z));
    }

    @Override
    public T get(double x, double y, double z) {
        return fromDouble(getTypedSource().getDouble(x, y, z) / add.apply(x, y, z));
    }
}
