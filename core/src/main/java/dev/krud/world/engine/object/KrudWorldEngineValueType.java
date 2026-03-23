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

package dev.krud.world.engine.object;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.annotations.Desc;

import java.util.function.Function;

@Desc("Represents a value from the engine")
public enum KrudWorldEngineValueType {
    @Desc("Represents actual height of the engine")
    ENGINE_HEIGHT((f) -> Double.valueOf(f.getHeight())),

    @Desc("Represents virtual bottom of the engine in the compound. If this engine is on top of another engine, it's min height would be at the maxHeight of the previous engine + 1")
    ENGINE_MIN_HEIGHT((f) -> Double.valueOf(f.getMinHeight())),

    @Desc("Represents virtual top of the engine in the compound. If this engine is below another engine, it's max height would be at the minHeight of the next engine - 1")
    ENGINE_MAX_HEIGHT((f) -> Double.valueOf(f.getWorld().maxHeight())),

    @Desc("The fluid height defined in the dimension file")
    FLUID_HEIGHT((f) -> Double.valueOf(f.getComplex().getFluidHeight())),
    ;

    private final Function<Engine, Double> getter;

    KrudWorldEngineValueType(Function<Engine, Double> getter) {
        this.getter = getter;
    }

    public Double get(Engine engine) {
        return getter.apply(engine);
    }
}
