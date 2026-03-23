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
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.Data;
import org.bukkit.World;

@Snippet("time-block")
@Data
@Desc("Represents a time of day (24h time, not 12h am/pm). Set both to the same number for any time. If they are both set to -1, it will always be not allowed.")
public class KrudWorldTimeBlock {
    @Desc("The beginning hour. Set both to the same number for any time. If they are both set to -1, it will always be not allowed.")
    private double startHour = 0;

    @Desc("The ending hour. Set both to the same number for any time. If they are both set to -1, it will always be not allowed.")
    private double endHour = 0;

    public boolean isWithin(World world) {
        return isWithin(((world.getTime() / 1000D) + 6) % 24);
    }

    public boolean isWithin(double hour) {
        if (startHour == endHour) {
            return endHour != -1;
        }

        if (startHour > endHour) {
            return hour >= startHour || hour <= endHour;
        }

        return hour >= startHour && hour <= endHour;
    }
}
