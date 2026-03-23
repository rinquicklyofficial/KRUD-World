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

package dev.krud.world.util.stream.sources;

import dev.krud.world.util.function.Function2;
import dev.krud.world.util.function.Function3;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.interpolation.Interpolated;

public class FunctionStream<T> extends BasicStream<T> {
    private final Function2<Double, Double, T> f2;
    private final Function3<Double, Double, Double, T> f3;
    private final Interpolated<T> helper;

    public FunctionStream(Function2<Double, Double, T> f2, Function3<Double, Double, Double, T> f3, Interpolated<T> helper) {
        super();
        this.f2 = f2;
        this.f3 = f3;
        this.helper = helper;
    }

    @Override
    public double toDouble(T t) {
        return helper.toDouble(t);
    }

    @Override
    public T fromDouble(double d) {
        return helper.fromDouble(d);
    }

    @Override
    public T get(double x, double z) {
        return f2.apply(x, z);
    }

    @Override
    public T get(double x, double y, double z) {
        return f3.apply(x, y, z);
    }
}
