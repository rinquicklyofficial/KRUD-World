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

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.stream.ArraySignificance;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;
import dev.krud.world.util.stream.Significance;

public class SignificanceStream<K extends Significance<T>, T> extends BasicStream<K> {
    private final ProceduralStream<T> stream;
    private final double radius;
    private final int checks;

    public SignificanceStream(ProceduralStream<T> stream, double radius, int checks) {
        super();
        this.stream = stream;
        this.radius = radius;
        this.checks = checks;
    }

    @Override
    public double toDouble(K t) {
        return 0;
    }

    @Override
    public K fromDouble(double d) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public K get(double x, double z) {
        KList<T> ke = new KList<>(8);
        KList<Double> va = new KList<>(8);

        double m = (360d / checks);
        double v = 0;

        for (int i = 0; i < 360; i += m) {
            double sin = Math.sin(Math.toRadians(i));
            double cos = Math.cos(Math.toRadians(i));
            double cx = x + ((radius * cos) - (radius * sin));
            double cz = z + ((radius * sin) + (radius * cos));
            T t = stream.get(cx, cz);

            if (ke.addIfMissing(t)) {
                va.add(1D);
                v++;
            } else {
                int ind = ke.indexOf(t);
                va.set(ind, va.get(ind) + 1D);
            }
        }

        for (int i = 0; i < va.size(); i++) {
            va.set(i, va.get(i) / v);
        }

        return (K) new ArraySignificance<>(ke, va);
    }

    @Override
    public K get(double x, double y, double z) {
        return get(x, z);
    }
}
