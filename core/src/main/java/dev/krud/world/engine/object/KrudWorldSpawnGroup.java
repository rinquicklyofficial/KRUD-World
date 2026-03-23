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

@Desc("Terrain modes are used to decide the generator type currently used")
public enum KrudWorldSpawnGroup {
    @Desc("Spawns on the terrain surface")
    NORMAL,

    @Desc("Spawns in cave-air and low light level areas")
    CAVE,

    @Desc("Spawns underwater")
    UNDERWATER,

    @Desc("Spawns in beaches")
    BEACH
}
