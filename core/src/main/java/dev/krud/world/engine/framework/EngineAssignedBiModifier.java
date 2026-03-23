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


import dev.krud.world.util.hunk.Hunk;

public abstract class EngineAssignedBiModifier<A, B> extends EngineAssignedComponent implements EngineBiModifier<A, B> {
    public EngineAssignedBiModifier(Engine engine, String name) {
        super(engine, name);
    }

    public abstract void onModify(int x, int z, Hunk<A> a, Hunk<B> b);

    @Override
    public void modify(int x, int z, Hunk<A> a, Hunk<B> b) {
        onModify(x, z, a, b);
    }
}
