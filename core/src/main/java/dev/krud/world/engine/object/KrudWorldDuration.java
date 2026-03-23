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
import dev.krud.world.util.format.Form;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Snippet("duration")
@Data
@Desc("Represents a combined duration. Fill each property to add time into a single duration")
public class KrudWorldDuration {
    @Desc("Milliseconds (1000ms = 1 second)")
    private int milliseconds = 0;

    @Desc("Minecraft Ticks (20 minecraft ticks = 1 second")
    private int minecraftTicks = 0;

    @Desc("Seconds (60 seconds = 1 minute)")
    private int seconds = 0;

    @Desc("Minutes (60 minutes = 1 hour)")
    private int minutes = 0;

    @Desc("Minecraft Hours (about 50 real seconds)")
    private int minecraftHours = 0;

    @Desc("Hours (24 hours = 1 day)")
    private int hours = 0;

    @Desc("Minecraft Days (1 minecraft day = 20 real minutes)")
    private int minecraftDays = 0;

    @Desc("Minecraft Weeks (1 minecraft week = 2 real hours and 18 real minutes)")
    private int minecraftWeeks = 0;

    @Desc("Minecraft Lunar Cycles (1 minecraft lunar cycle = 2 real hours and 36 real minutes)")
    private int minecraftLunarCycles = 0;

    @Desc("REAL (not minecraft) Days")
    private int days = 0;

    public String toString() {
        return Form.duration((double) toMilliseconds(), 2);
    }

    public long toMilliseconds() {
        return getMilliseconds()
                + TimeUnit.SECONDS.toMillis(getSeconds())
                + TimeUnit.MINUTES.toMillis(getMinutes())
                + TimeUnit.HOURS.toMillis(getHours())
                + TimeUnit.DAYS.toMillis(getDays())
                + (getMinecraftTicks() * 50L)
                + (getMinecraftHours() * 50000L)
                + (getMinecraftWeeks() * 50000L)
                + (getMinecraftDays() * 24000L)
                + (getMinecraftWeeks() * 168000L)
                + (getMinecraftLunarCycles() * 192000L);
    }
}
