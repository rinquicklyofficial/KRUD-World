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

import dev.krud.world.KrudWorld;
import dev.krud.world.util.math.RollingSequence;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = "engine")
@ToString(exclude = "engine")
public class EngineAssignedComponent implements EngineComponent {
    private final Engine engine;
    private final RollingSequence metrics;
    private final String name;

    public EngineAssignedComponent(Engine engine, String name) {
        KrudWorld.debug("Engine: " + engine.getCacheID() + " Starting " + name);
        this.engine = engine;
        this.metrics = new RollingSequence(16);
        this.name = name;
    }
}
