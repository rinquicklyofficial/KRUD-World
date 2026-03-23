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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.engine.object.KrudWorldWorld;
import dev.krud.world.util.parallel.MultiBurst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = "data")
@ToString(exclude = "data")
public class EngineTarget {
    private final MultiBurst burster;
    private final KrudWorldData data;
    private KrudWorldDimension dimension;
    private KrudWorldWorld world;

    public EngineTarget(KrudWorldWorld world, KrudWorldDimension dimension, KrudWorldData data) {
        this.world = world;
        this.dimension = dimension;
        this.data = data;
        this.burster = MultiBurst.burst;
    }

    public int getHeight() {
        return world.maxHeight() - world.minHeight();
    }

    public void close() {

    }
}
