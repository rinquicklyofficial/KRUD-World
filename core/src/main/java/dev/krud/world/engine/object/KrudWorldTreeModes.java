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

@Desc("Sapling override object picking options")
public enum KrudWorldTreeModes {
    @Desc("Check biome, then region, then dimension, pick the first one that has options")
    FIRST,

    @Desc("Check biome, regions, and dimensions, and pick any option from the total list")
    ALL
}