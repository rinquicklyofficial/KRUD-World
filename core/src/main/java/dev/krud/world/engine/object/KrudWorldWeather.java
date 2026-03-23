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
import org.bukkit.World;

@Desc("Represents a weather type")
public enum KrudWorldWeather {
    @Desc("Represents when weather is not causing downfall")
    NONE,

    @Desc("Represents rain or snow")
    DOWNFALL,

    @Desc("Represents rain or snow with thunder")
    DOWNFALL_WITH_THUNDER,

    @Desc("Any weather")
    ANY;

    public boolean is(World world) {
        return switch (this) {
            case NONE -> world.isClearWeather();
            case DOWNFALL -> world.hasStorm();
            case DOWNFALL_WITH_THUNDER -> world.hasStorm() && world.isThundering();
            case ANY -> true;
        };
    }
}
