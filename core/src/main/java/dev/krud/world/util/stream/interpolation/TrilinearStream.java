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

public class TrilinearStream<T> extends BasicStream<T> implements Interpolator<T> {
    private final int rx;
    private final int ry;
    private final int rz;

    public TrilinearStream(ProceduralStream<T> stream, int rx, int ry, int rz) {
        super(stream);
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    public T interpolate(double x, double y, double z) {
        int fx = (int) Math.floor(x / rx);
        int fy = (int) Math.floor(y / ry);
        int fz = (int) Math.floor(z / rz);
        int x1 = Math.round(fx * rx);
        int y1 = Math.round(fy * ry);
        int z1 = Math.round(fz * rz);
        int x2 = Math.round((fx + 1) * rx);
        int y2 = Math.round((fy + 1) * ry);
        int z2 = Math.round((fz + 1) * rz);
        double px = KrudWorldInterpolation.rangeScale(0, 1, x1, x2, x);
        double py = KrudWorldInterpolation.rangeScale(0, 1, y1, y2, y);
        double pz = KrudWorldInterpolation.rangeScale(0, 1, z1, z2, z);

        //@builder
        return getTypedSource().fromDouble(KrudWorldInterpolation.trilerp(
                getTypedSource().getDouble(x1, y1, z1),
                getTypedSource().getDouble(x2, y1, z1),
                getTypedSource().getDouble(x1, y1, z2),
                getTypedSource().getDouble(x2, y1, z2),
                getTypedSource().getDouble(x1, y2, z1),
                getTypedSource().getDouble(x2, y2, z1),
                getTypedSource().getDouble(x1, y2, z2),
                getTypedSource().getDouble(x2, y2, z2),
                px, pz, py));
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
        return interpolate(x, 0, z);
    }

    @Override
    public T get(double x, double y, double z) {
        return interpolate(x, y, z);
    }
}
