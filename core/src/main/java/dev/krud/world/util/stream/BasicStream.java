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

package dev.krud.world.util.stream;

public abstract class BasicStream<T> extends BasicLayer implements ProceduralStream<T> {
    private final ProceduralStream<T> source;

    public BasicStream(ProceduralStream<T> source) {
        super();
        this.source = source;
    }

    public BasicStream() {
        this(null);
    }


    @Override
    public ProceduralStream<T> getTypedSource() {
        return source;
    }

    @Override
    public ProceduralStream<?> getSource() {
        return getTypedSource();
    }

    @Override
    public abstract T get(double x, double z);

    @Override
    public abstract T get(double x, double y, double z);

    @Override
    public abstract double toDouble(T t);

    @Override
    public abstract T fromDouble(double d);
}
