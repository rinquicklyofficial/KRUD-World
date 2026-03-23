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

import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class BilinearStream<T> extends BasicStream<T> implements Interpolator<T> {
    private final int rx;
    private final int ry;

    public BilinearStream(ProceduralStream<T> stream, int rx, int ry) {
        super(stream);
        this.rx = rx;
        this.ry = ry;
    }

    public T interpolate(double x, double y) {
        int fx = (int) Math.floor(x / rx);
        int fz = (int) Math.floor(y / ry);
        int x1 = Math.round(fx * rx);
        int z1 = Math.round(fz * ry);
        int x2 = Math.round((fx + 1) * rx);
        int z2 = Math.round((fz + 1) * ry);
        double px = KrudWorldInterpolation.rangeScale(0, 1, x1, x2, x);
        double pz = KrudWorldInterpolation.rangeScale(0, 1, z1, z2, y);

        //@builder
        return getTypedSource().fromDouble(KrudWorldInterpolation.blerp(
                getTypedSource().getDouble(x1, z1),
                getTypedSource().getDouble(x2, z1),
                getTypedSource().getDouble(x1, z2),
                getTypedSource().getDouble(x2, z2),
                px, pz));
        //@done
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
