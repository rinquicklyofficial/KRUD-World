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

import dev.krud.world.util.mantle.MantleChunk;
import dev.krud.world.util.math.RNG;
import org.bukkit.Chunk;
import org.bukkit.block.data.BlockData;

public interface BlockUpdater {

    void catchBlockUpdates(int x, int y, int z, BlockData data);

    void updateChunk(Chunk c);

    void update(int x, int y, int z, Chunk c, MantleChunk mc, RNG rf);
}
