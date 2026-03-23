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

import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

import java.util.List;

public class SelectionStream<T> extends BasicStream<T> {
    private final ProceduralStream<Integer> stream;
    private final T[] options;

    public SelectionStream(ProceduralStream<?> stream, T[] options) {
        super();
        this.stream = stream.fit(0, options.length - 1).round();
        this.options = options;
    }

    @SuppressWarnings("unchecked")
    public SelectionStream(ProceduralStream<?> stream, List<T> options) {
        this(stream, (T[]) options.toArray());
    }

    @Override
    public double toDouble(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T fromDouble(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(double x, double z) {
        if (options.length == 0) {
            return null;
        }

        return options[stream.get(x, z)];
    }

    @Override
    public T get(double x, double y, double z) {
        if (options.length == 0) {
            return null;
        }

        return options[stream.get(x, y, z)];
    }

}
