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

package dev.krud.world.util.stream.convert;

import dev.krud.world.util.function.Function3;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class AwareConversionStream2D<T, V> extends BasicStream<V> {
    private final ProceduralStream<T> stream;
    private final Function3<T, Double, Double, V> converter;

    public AwareConversionStream2D(ProceduralStream<T> stream, Function3<T, Double, Double, V> converter) {
        super(null);
        this.stream = stream;
        this.converter = converter;
    }

    @Override
    public double toDouble(V t) {
        if (t instanceof Double) {
            return (Double) t;
        }

        return 0;
    }

    @Override
    public V fromDouble(double d) {
        return null;
    }

    @Override
    public ProceduralStream<?> getSource() {
        return stream;
    }

    @Override
    public V get(double x, double z) {
        return converter.apply(stream.get(x, z), x, z);
    }

    @Override
    public V get(double x, double y, double z) {
        return converter.apply(stream.get(x, y, z), x, z);
    }
}
