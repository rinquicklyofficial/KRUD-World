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

package dev.krud.world.engine.platform.studio;

import dev.krud.world.engine.data.chunk.TerrainChunk;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.WrongEngineBroException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class EnginedStudioGenerator implements StudioGenerator {
    private final Engine engine;

    @Override
    public abstract void generateChunk(Engine engine, TerrainChunk tc, int x, int z) throws WrongEngineBroException;
}
