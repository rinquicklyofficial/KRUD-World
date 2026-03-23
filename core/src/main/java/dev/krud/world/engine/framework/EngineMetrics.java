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

package dev.krud.world.engine.framework;

import dev.krud.world.util.atomics.AtomicRollingSequence;
import dev.krud.world.util.collection.KMap;
import lombok.Data;

@Data
public class EngineMetrics {
    private final AtomicRollingSequence total;
    private final AtomicRollingSequence updates;
    private final AtomicRollingSequence terrain;
    private final AtomicRollingSequence biome;
    private final AtomicRollingSequence parallax;
    private final AtomicRollingSequence parallaxInsert;
    private final AtomicRollingSequence post;
    private final AtomicRollingSequence perfection;
    private final AtomicRollingSequence api;
    private final AtomicRollingSequence decoration;
    private final AtomicRollingSequence cave;
    private final AtomicRollingSequence ravine;
    private final AtomicRollingSequence deposit;

    public EngineMetrics(int mem) {
        this.total = new AtomicRollingSequence(mem);
        this.terrain = new AtomicRollingSequence(mem);
        this.api = new AtomicRollingSequence(mem);
        this.biome = new AtomicRollingSequence(mem);
        this.perfection = new AtomicRollingSequence(mem);
        this.parallax = new AtomicRollingSequence(mem);
        this.parallaxInsert = new AtomicRollingSequence(mem);
        this.post = new AtomicRollingSequence(mem);
        this.decoration = new AtomicRollingSequence(mem);
        this.updates = new AtomicRollingSequence(mem);
        this.cave = new AtomicRollingSequence(mem);
        this.ravine = new AtomicRollingSequence(mem);
        this.deposit = new AtomicRollingSequence(mem);
    }

    public KMap<String, Double> pull() {
        KMap<String, Double> v = new KMap<>();
        v.put("total", total.getAverage());
        v.put("terrain", terrain.getAverage());
        v.put("biome", biome.getAverage());
        v.put("parallax", parallax.getAverage());
        v.put("parallax.insert", parallaxInsert.getAverage());
        v.put("post", post.getAverage());
        v.put("perfection", perfection.getAverage());
        v.put("decoration", decoration.getAverage());
        v.put("api", api.getAverage());
        v.put("updates", updates.getAverage());
        v.put("cave", cave.getAverage());
        v.put("ravine", ravine.getAverage());
        v.put("deposit", deposit.getAverage());

        return v;
    }
}
