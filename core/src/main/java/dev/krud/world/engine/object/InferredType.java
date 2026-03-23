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

@Desc("Represents a biome type")
public enum InferredType {
    @Desc("Represents any shore biome type")
    SHORE,

    @Desc("Represents any land biome type")
    LAND,

    @Desc("Represents any sea biome type")
    SEA,

    @Desc("Represents any cave biome type")
    CAVE
}
