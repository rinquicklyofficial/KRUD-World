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

package dev.krud.world.util.stream.interpolation;

import dev.krud.world.util.function.NoiseProvider;
import dev.krud.world.util.interpolation.InterpolationMethod;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class InterpolatingStream<T> extends BasicStream<T> implements Interpolator<T> {
    private final InterpolationMethod type;
    private final NoiseProvider np;
    private final int rx;

    public InterpolatingStream(ProceduralStream<T> stream, int rx, InterpolationMethod type) {
        super(stream);
        this.type = type;
        this.rx = rx;
        this.np = (xf, zf) -> getTypedSource().getDouble(xf, zf);
    }

    public T interpolate(double x, double y) {
        return fromDouble(KrudWorldInterpolation.getNoise(type, (int) x, (int) y, rx, np));
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
        return interpolate(x, z);
    }

    @Override
    public T get(double x, double y, double z) {
        return interpolate(x, z);
    }
}
