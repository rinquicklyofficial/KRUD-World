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

import dev.krud.world.util.stream.BasicLayer;
import dev.krud.world.util.stream.ProceduralStream;

import java.util.function.Function;

public class ConversionStream<T, V> extends BasicLayer implements ProceduralStream<V> {
    private final ProceduralStream<T> stream;
    private final Function<T, V> converter;

    public ConversionStream(ProceduralStream<T> stream, Function<T, V> converter) {
        super();
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
    public ProceduralStream<V> getTypedSource() {
        return null;
    }

    @Override
    public ProceduralStream<?> getSource() {
        return null;
    }

    @Override
    public V get(double x, double z) {
        return converter.apply(stream.get(x, z));
    }

    @Override
    public V get(double x, double y, double z) {
        return converter.apply(stream.get(x, y, z));
    }
}
