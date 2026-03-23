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

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KeyPair;

import java.util.Random;

public class WeightedRandom<T> {

    private final KList<KeyPair<T, Integer>> weightedObjects = new KList<>();
    private final Random random;
    private int totalWeight = 0;

    public WeightedRandom(Random random) {
        this.random = random;
    }

    public WeightedRandom() {
        this.random = new Random();
    }

    public void put(T object, int weight) {
        weightedObjects.add(new KeyPair<>(object, weight));
        totalWeight += weight;
    }

    public WeightedRandom<T> merge(WeightedRandom<T> other) {
        weightedObjects.addAll(other.weightedObjects);
        totalWeight += other.totalWeight;
        return this;
    }

    public T pullRandom() {
        int pull = random.nextInt(totalWeight);
        int index = 0;
        while (pull > 0) {
            pull -= weightedObjects.get(index).getV();
            if (pull <= 0) break;
            index++;
        }
        return weightedObjects.get(index).getK();
    }

    public int getSize() {
        return weightedObjects.size();
    }

    public void shuffle() {
        weightedObjects.shuffle(random);
    }
}
