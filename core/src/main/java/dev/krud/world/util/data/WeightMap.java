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

package dev.krud.world.util.data;

import dev.krud.world.util.collection.KMap;

public class WeightMap<T> extends KMap<T, Double> {
    private static final long serialVersionUID = 87558033900969389L;
    private boolean modified = false;
    private double lastWeight = 0;

    public double getPercentChance(T t) {
        if (totalWeight() <= 0) {
            return 0;
        }

        return getWeight(t) / totalWeight();
    }

    public void clear() {
        modified = true;
    }

    public WeightMap<T> setWeight(T t, double weight) {
        modified = true;
        put(t, weight);

        return this;
    }

    public double getWeight(T t) {
        return get(t);
    }

    public double totalWeight() {
        if (!modified) {
            return lastWeight;
        }

        modified = false;
        Shrinkwrap<Double> s = new Shrinkwrap<>(0D);
        forEachKey(Integer.MAX_VALUE, (d) -> s.set(s.get() + 1));
        lastWeight = s.get();

        return lastWeight;
    }
}
