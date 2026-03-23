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

import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.math.RNG;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Snippet("command-registry")
@Accessors(chain = true)
@NoArgsConstructor
@Desc("Represents a casting location for a command")
@Data
public class KrudWorldCommandRegistry {
    @Required
    @ArrayType(min = 1, type = KrudWorldCommand.class)
    @Desc("Run commands, at the exact location of the player")
    private KList<KrudWorldCommand> rawCommands = new KList<>();
    @DependsOn({"rawCommands"})
    @MinNumber(-8)
    @MaxNumber(8)
    @Desc("The alt x, usually represents motion if the particle count is zero. Otherwise an offset.")
    private double commandOffsetX = 0;
    @DependsOn({"rawCommands"})
    @MinNumber(-8)
    @MaxNumber(8)
    @Desc("The alt y, usually represents motion if the particle count is zero. Otherwise an offset.")
    private double commandOffsetY = 0;
    @DependsOn({"rawCommands"})
    @MinNumber(-8)
    @MaxNumber(8)
    @Desc("The alt z, usually represents motion if the particle count is zero. Otherwise an offset.")
    private double commandOffsetZ = 0;
    @DependsOn({"rawCommands"})
    @Desc("Randomize the altX from -altX to altX")
    private boolean commandRandomAltX = true;
    @DependsOn({"rawCommands"})
    @Desc("Randomize the altY from -altY to altY")
    private boolean commandRandomAltY = false;
    @DependsOn({"rawCommands"})
    @Desc("Randomize the altZ from -altZ to altZ")
    private boolean commandRandomAltZ = true;
    @DependsOn({"rawCommands"})
    @Desc("Randomize location for all separate commands (true), or run all on the same location (false)")
    private boolean commandAllRandomLocations = true;

    public void run(Player p) {
        if (rawCommands.isNotEmpty()) {
            Location part = p.getLocation().clone().add(
                    commandRandomAltX ? RNG.r.d(-commandOffsetX, commandOffsetX) : commandOffsetX,
                    commandRandomAltY ? RNG.r.d(-commandOffsetY, commandOffsetY) : commandOffsetY,
                    commandRandomAltZ ? RNG.r.d(-commandOffsetZ, commandOffsetZ) : commandOffsetZ);
            for (KrudWorldCommand rawCommand : rawCommands) {
                rawCommand.run(part);
                if (commandAllRandomLocations) {
                    part = p.getLocation().clone().add(
                            commandRandomAltX ? RNG.r.d(-commandOffsetX, commandOffsetX) : commandOffsetX,
                            commandRandomAltY ? RNG.r.d(-commandOffsetY, commandOffsetY) : commandOffsetY,
                            commandRandomAltZ ? RNG.r.d(-commandOffsetZ, commandOffsetZ) : commandOffsetZ);
                }
            }
        }
    }
}
