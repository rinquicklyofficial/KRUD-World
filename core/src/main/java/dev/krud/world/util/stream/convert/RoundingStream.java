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

public class RoundingStream extends BasicStream<Integer> {
    private final ProceduralStream<?> stream;

    public RoundingStream(ProceduralStream<?> stream) {
        super();
        this.stream = stream;
    }

    @Override
    public double toDouble(Integer t) {
        return t.doubleValue();
    }

    @Override
    public Integer fromDouble(double d) {
        return (int) Math.round(d);
    }

    private int round(double v) {
        return (int) Math.round(v);
    }

    @Override
    public Integer get(double x, double z) {
        return round(stream.getDouble(x, z));
    }

    @Override
    public Integer get(double x, double y, double z) {
        return round(stream.getDouble(x, y, z));
    }
}
