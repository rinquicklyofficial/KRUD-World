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

import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.stream.BasicLayer;
import dev.krud.world.util.stream.ProceduralStream;

import java.util.function.Function;

public class CachedConversionStream<T, V> extends BasicLayer implements ProceduralStream<V> {
    private final ProceduralStream<T> stream;
    private final Function<T, V> converter;
    private final KMap<T, V> cache;

    public CachedConversionStream(ProceduralStream<T> stream, Function<T, V> converter) {
        super();
        this.stream = stream;
        this.converter = converter;
        cache = new KMap<>();
    }

    @Override
    public double toDouble(V t) {
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
        return stream;
    }

    @Override
    public V get(double x, double z) {
        return cache.computeIfAbsent(stream.get(x, z), converter);
    }

    @Override
    public V get(double x, double y, double z) {
        return cache.computeIfAbsent(stream.get(x, y, z), converter);
    }
}
