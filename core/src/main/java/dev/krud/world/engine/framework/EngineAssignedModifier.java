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
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;

public abstract class EngineAssignedModifier<T> extends EngineAssignedComponent implements EngineModifier<T> {
    public EngineAssignedModifier(Engine engine, String name) {
        super(engine, name);
    }

    @BlockCoordinates
    public abstract void onModify(int x, int z, Hunk<T> output, boolean multicore, ChunkContext context);

    @BlockCoordinates
    @Override
    public void modify(int x, int z, Hunk<T> output, boolean multicore, ChunkContext context) {
        try {
            onModify(x, z, output, multicore, context);
        } catch (Throwable e) {
            KrudWorld.error("Modifier Failure: " + getName());
            e.printStackTrace();
        }
    }
}
