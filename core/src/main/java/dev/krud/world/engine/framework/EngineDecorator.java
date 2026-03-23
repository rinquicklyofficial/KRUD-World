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

import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.util.data.B;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.hunk.Hunk;
import org.bukkit.block.data.BlockData;

public interface EngineDecorator extends EngineComponent {

    @BlockCoordinates
    void decorate(int x, int z, int realX, int realX1, int realX_1, int realZ, int realZ1, int realZ_1, Hunk<BlockData> data, KrudWorldBiome biome, int height, int max);

    @BlockCoordinates
    default void decorate(int x, int z, int realX, int realZ, Hunk<BlockData> data, KrudWorldBiome biome, int height, int max) {
        decorate(x, z, realX, realX, realX, realZ, realZ, realZ, data, biome, height, max);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean canGoOn(BlockData decorant, BlockData atop) {
        if (atop == null || B.isAir(atop)) {
            return false;
        }

        return B.canPlaceOnto(decorant.getMaterial(), atop.getMaterial());
    }
}
