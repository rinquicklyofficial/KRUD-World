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

import dev.krud.world.util.collection.KList;

public class ArraySignificance<T> implements Significance<T> {
    private final KList<T> types;
    private final KList<Double> significance;
    private final T significant;

    public ArraySignificance(KList<T> types, KList<Double> significance, T significant) {
        this.types = types;
        this.significance = significance;
        this.significant = significant;
    }

    public ArraySignificance(KList<T> types, KList<Double> significance) {
        this.types = types;
        this.significance = significance;
        double s = 0;
        int v = 0;
        for (int i = 0; i < significance.size(); i++) {
            if (significance.get(i) > s) {
                s = significance.get(i);
                v = i;
            }
        }

        significant = types.get(v);
    }

    @Override
    public KList<T> getFactorTypes() {
        return types;
    }

    @Override
    public double getSignificance(T t) {
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i).equals(t)) {
                return significance.get(i);
            }
        }

        return 0;
    }

    @Override
    public T getMostSignificantType() {
        return significant;
    }
}
