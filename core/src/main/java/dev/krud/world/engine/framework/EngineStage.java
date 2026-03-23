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

import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

public interface EngineStage {
    @BlockCoordinates
    void generate(int x, int z, Hunk<BlockData> blocks, Hunk<Biome> biomes, boolean multicore, ChunkContext context);

    default void close() {
        if (this instanceof EngineComponent c) {
            c.close();
        }
    }
}
