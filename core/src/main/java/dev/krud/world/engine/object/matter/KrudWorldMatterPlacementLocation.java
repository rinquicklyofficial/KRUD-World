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

package dev.krud.world.engine.object.matter;

import dev.krud.world.engine.KrudWorldEngine;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.util.function.Function3;

@Desc("WHERE THINGS PLACE")
public enum KrudWorldMatterPlacementLocation {
    SURFACE((e, x, z) -> e.getHeight(x, z, true)),
    SURFACE_ON_FLUID((e, x, z) -> e.getHeight(x, z, false)),
    BEDROCK((e, x, z) -> 0),
    SKY((e, x, z) -> e.getHeight());

    private final Function3<KrudWorldEngine, Integer, Integer, Integer> computer;

    KrudWorldMatterPlacementLocation(Function3<KrudWorldEngine, Integer, Integer, Integer> computer) {
        this.computer = computer;
    }

    public int at(KrudWorldEngine engine, int x, int z) {
        return computer.apply(engine, x, z);
    }
}
