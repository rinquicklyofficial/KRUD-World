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

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Snippet("command")
@Accessors(chain = true)
@NoArgsConstructor
@Desc("Represents a set of KrudWorld commands")
@Data
public class KrudWorldCommand {

    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("List of commands. KrudWorld replaces {x} {y} and {z} with the location of the entity spawn")
    private KList<String> commands = new KList<>();

    @Desc("The delay for running the command. Instant by default")
    private long delay = 0;

    @Desc("If this should be repeated (indefinitely, cannot be cancelled). This does not persist with server-restarts, so it only repeats when the chunk is generated.")
    private boolean repeat = false;

    @Desc("The delay between repeats, in server ticks (by default 100, so 5 seconds)")
    private long repeatDelay = 100;

    @Desc("The block of 24 hour time in which the command should execute.")
    private KrudWorldTimeBlock timeBlock = new KrudWorldTimeBlock();

    @Desc("The weather that is required for the command to execute.")
    private KrudWorldWeather weather = KrudWorldWeather.ANY;

    public boolean isValid(World world) {
        return timeBlock.isWithin(world) && weather.is(world);
    }

    public void run(Location at) {
        if (!isValid(at.getWorld())) {
            return;
        }

        for (String command : commands) {
            command = (command.startsWith("/") ? command.replaceFirst("/", "") : command)
                    .replaceAll("\\Q{x}\\E", String.valueOf(at.getBlockX()))
                    .replaceAll("\\Q{y}\\E", String.valueOf(at.getBlockY()))
                    .replaceAll("\\Q{z}\\E", String.valueOf(at.getBlockZ()));
            final String finalCommand = command;
            if (repeat) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(KrudWorld.instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand), delay, repeatDelay);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(KrudWorld.instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand), delay);
            }
        }
    }
}