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
import dev.krud.world.core.pregenerator.LazyPregenerator;
import dev.krud.world.core.pregenerator.TurboPregenerator;
import dev.krud.world.core.pregenerator.TurboPregenerator;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.format.C;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

@Decree(name = "turbopregen", aliases = "turbo", description = "Pregenerate your KrudWorld worlds!")
public class CommandTurboPregen implements DecreeExecutor {
    public String worldName;
    @Decree(description = "Pregenerate a world")
    public void start(
            @Param(description = "The radius of the pregen in blocks", aliases = "size")
            int radius,
            @Param(description = "The world to pregen", contextual = true)
            World world,
            @Param(aliases = "middle", description = "The center location of the pregen. Use \"me\" for your current location", defaultValue = "0,0")
            Vector center
            ) {

        worldName = world.getName();
        File worldDirectory = new File(Bukkit.getWorldContainer(), world.getName());
        File TurboFile = new File(worldDirectory, "turbogen.json");
        if (TurboFile.exists()) {
            if (TurboPregenerator.getInstance() != null) {
                sender().sendMessage(C.BLUE + "Turbo pregen is already in progress");
                KrudWorld.info(C.YELLOW + "Turbo pregen is already in progress");
                return;
            } else {
                try {
                    TurboFile.delete();
                } catch (Exception e){
                   KrudWorld.error("Failed to delete the old instance file of Turbo Pregen!");
                   return;
                }
            }
        }

        try {
            if (sender().isPlayer() && access() == null) {
                sender().sendMessage(C.RED + "The engine access for this world is null!");
                sender().sendMessage(C.RED + "Please make sure the world is loaded & the engine is initialized. Generate a new chunk, for example.");
            }

            TurboPregenerator.TurboPregenJob pregenJob = TurboPregenerator.TurboPregenJob.builder()
                    .world(worldName)
                    .radiusBlocks(radius)
                    .position(0)
                    .build();

            File TurboGenFile = new File(worldDirectory, "turbogen.json");
            TurboPregenerator pregenerator = new TurboPregenerator(pregenJob, TurboGenFile);
            pregenerator.start();

            String msg = C.GREEN + "TurboPregen started in " + C.GOLD + worldName + C.GREEN + " of " + C.GOLD + (radius * 2) + C.GREEN + " by " + C.GOLD + (radius * 2) + C.GREEN + " blocks from " + C.GOLD + center.getX() + "," + center.getZ();
            sender().sendMessage(msg);
            KrudWorld.info(msg);
        } catch (Throwable e) {
            sender().sendMessage(C.RED + "Epic fail. See console.");
            KrudWorld.reportError(e);
            e.printStackTrace();
        }
    }

    @Decree(description = "Stop the active pregeneration task", aliases = "x")
    public void stop(@Param(aliases = "world", description = "The world to pause") World world) throws IOException {
        TurboPregenerator turboPregenInstance = TurboPregenerator.getInstance();
        File worldDirectory = new File(Bukkit.getWorldContainer(), world.getName());
        File turboFile = new File(worldDirectory, "turbogen.json");

        if (turboPregenInstance != null) {
            turboPregenInstance.shutdownInstance(world);
            sender().sendMessage(C.LIGHT_PURPLE + "Closed Turbogen instance for " + world.getName());
        } else if (turboFile.exists() && turboFile.delete()) {
            sender().sendMessage(C.LIGHT_PURPLE + "Closed Turbogen instance for " + world.getName());
        } else if (turboFile.exists()) {
            KrudWorld.error("Failed to delete the old instance file of Turbo Pregen!");
        } else {
            sender().sendMessage(C.YELLOW + "No active pregeneration tasks to stop");
        }
    }

    @Decree(description = "Pause / continue the active pregeneration task", aliases = {"t", "resume", "unpause"})
    public void pause(
            @Param(aliases = "world", description = "The world to pause")
            World world
    ) {
        if (TurboPregenerator.getInstance() != null) {
            TurboPregenerator.setPausedTurbo(world);
            sender().sendMessage(C.GREEN + "Paused/unpaused Turbo Pregen, now: " + (TurboPregenerator.isPausedTurbo(world) ? "Paused" : "Running") + ".");
        } else {
            File worldDirectory = new File(Bukkit.getWorldContainer(), world.getName());
            File TurboFile = new File(worldDirectory, "turbogen.json");
            if (TurboFile.exists()){
                TurboPregenerator.loadTurboGenerator(world.getName());
                sender().sendMessage(C.YELLOW + "Started Turbo Pregen back up!");
            } else {
                sender().sendMessage(C.YELLOW + "No active Turbo Pregen tasks to pause/unpause.");
            }

        }
    }
}
