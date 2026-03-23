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

@Desc("The mob spawn group")
public enum KrudWorldBiomeCustomSpawnType {
    @Desc("Typical monsters that spawn at night, like zombies and skeletons")
    MONSTER,

    @Desc("Typical creatures like sheep, pigs, cows")
    CREATURE,

    @Desc("Eg bats")
    AMBIENT,

    @Desc("Odd spawn group but ok")
    UNDERGROUND_WATER_CREATURE,

    @Desc("Water mobs like squid, dolphins")
    WATER_CREATURE,

    @Desc("Fish")
    WATER_AMBIENT,

    @Desc("Unknown")
    MISC
}
