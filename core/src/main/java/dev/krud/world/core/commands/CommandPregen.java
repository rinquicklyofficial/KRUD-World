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

package dev.krud.world.core.commands;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.gui.PregeneratorJob;
import dev.krud.world.core.pregenerator.PregenTask;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.format.C;
import dev.krud.world.util.math.Position2;
import org.bukkit.World;
import org.bukkit.util.Vector;

@Decree(name = "pregen", aliases = "pregenerate", description = "Pregenerate your KrudWorld worlds!")
public class CommandPregen implements DecreeExecutor {
    @Decree(description = "Pregenerate a world")
    public void start(
            @Param(description = "The radius of the pregen in blocks", aliases = "size")
            int radius,
            @Param(description = "The world to pregen", contextual = true)
            World world,
            @Param(aliases = "middle", description = "The center location of the pregen. Use \"me\" for your current location", defaultValue = "0,0")
            Vector center,
            @Param(description = "Open the KrudWorld pregen gui", defaultValue = "true")
            boolean gui
            ) {
        try {
            if (sender().isPlayer() && access() == null) {
                sender().sendMessage(C.RED + "The engine access for this world is null!");
                sender().sendMessage(C.RED + "Please make sure the world is loaded & the engine is initialized. Generate a new chunk, for example.");
            }
            radius = Math.max(radius, 1024);
            KrudWorldToolbelt.pregenerate(PregenTask
                    .builder()
                    .center(new Position2(center.getBlockX(), center.getBlockZ()))
                    .gui(gui)
                    .radiusX(radius)
                    .radiusZ(radius)
                    .build(), world);
            String msg = C.GREEN + "Pregen started in " + C.GOLD + world.getName() + C.GREEN + " of " + C.GOLD + (radius * 2) + C.GREEN + " by " + C.GOLD + (radius * 2) + C.GREEN + " blocks from " + C.GOLD + center.getX() + "," + center.getZ();
            sender().sendMessage(msg);
            KrudWorld.info(msg);
        } catch (Throwable e) {
            sender().sendMessage(C.RED + "Epic fail. See console.");
            KrudWorld.reportError(e);
            e.printStackTrace();
        }
    }

    @Decree(description = "Stop the active pregeneration task", aliases = "x")
    public void stop() {
        if (PregeneratorJob.shutdownInstance()) {
            KrudWorld.info( C.BLUE + "Finishing up mca region...");
        } else {
            sender().sendMessage(C.YELLOW + "No active pregeneration tasks to stop");
        }
    }

    @Decree(description = "Pause / continue the active pregeneration task", aliases = {"t", "resume", "unpause"})
    public void pause() {
        if (PregeneratorJob.pauseResume()) {
            sender().sendMessage(C.GREEN + "Paused/unpaused pregeneration task, now: " + (PregeneratorJob.isPaused() ? "Paused" : "Running") + ".");
        } else {
            sender().sendMessage(C.YELLOW + "No active pregeneration tasks to pause/unpause.");
        }
    }
}
