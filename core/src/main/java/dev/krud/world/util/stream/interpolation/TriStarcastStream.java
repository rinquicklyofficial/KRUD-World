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

import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class TriStarcastStream<T> extends BasicStream<T> implements Interpolator<T> {
    private final int rad;
    private final int checks;

    public TriStarcastStream(ProceduralStream<T> stream, int rad, int checks) {
        super(stream);
        this.rad = rad;
        this.checks = checks;
    }

    public T interpolate(double x, double y, double z) {
        double m = (360D / checks);
        double v = 0;

        for (int i = 0; i < 360; i += m) {
            double sin = Math.sin(Math.toRadians(i));
            double cos = Math.cos(Math.toRadians(i));
            double cx = x + ((rad * cos) - (rad * sin));
            double cy = y + ((rad * sin) + (rad * cos));
            double cz = z + ((rad * cos) - (rad * sin));
            v += getTypedSource().getDouble(cx, cy, cz);
        }

        return getTypedSource().fromDouble(v / checks);
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
