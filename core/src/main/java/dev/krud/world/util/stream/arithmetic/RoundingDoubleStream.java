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

package dev.krud.world.util.stream.arithmetic;

import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class RoundingDoubleStream extends BasicStream<Double> {
    private final ProceduralStream<?> stream;

    public RoundingDoubleStream(ProceduralStream<?> stream) {
        super();
        this.stream = stream;
    }

    @Override
    public double toDouble(Double t) {
        return t;
    }

    @Override
    public Double fromDouble(double d) {
        return (double) Math.round(d);
    }

    private double round(double v) {
        return Math.round(v);
    }

    @Override
    public Double get(double x, double z) {
        return round(stream.getDouble(x, z));
    }

    @Override
    public Double get(double x, double y, double z) {
        return round(stream.getDouble(x, y, z));
    }

}
