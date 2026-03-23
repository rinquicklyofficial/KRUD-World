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

import dev.krud.world.engine.object.annotations.Desc;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;

@Desc("The type of surface entities should spawn on")
public enum KrudWorldSurface {

    @Desc("Land surfaces")
    LAND,

    @Desc("Any surfaces animals can spawn on, such as dirt, grass and podzol")
    ANIMAL,

    @Desc("Within the water")
    WATER,

    @Desc("On land or on water")
    OVERWORLD,

    @Desc("Within lava")
    LAVA;

    /**
     * Check if this KrudWorld surface matches the blockstate provided
     *
     * @param state The blockstate
     * @return True if it matches
     */
    public boolean matches(Block state) {
        Material type = state.getType();
        if (type.isSolid()) {
            return this == LAND || this == OVERWORLD || (this == ANIMAL
                    && (type == Material.GRASS_BLOCK || type == Material.DIRT
                    || type == Material.DIRT_PATH || type == Material.COARSE_DIRT
                    || type == Material.ROOTED_DIRT || type == Material.PODZOL
                    || type == Material.MYCELIUM || type == Material.SNOW_BLOCK));
        }
        if (type == Material.LAVA) return this == LAVA;
        if (type == Material.WATER || type == Material.SEAGRASS
                || type == Material.TALL_SEAGRASS || type == Material.KELP_PLANT
                || type == Material.KELP ||
                (state instanceof Waterlogged && ((Waterlogged) state).isWaterlogged()))
            return this == WATER || this == OVERWORLD;

        return false;
    }
}
