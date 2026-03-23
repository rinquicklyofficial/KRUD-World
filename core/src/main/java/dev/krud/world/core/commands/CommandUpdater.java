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

import lombok.Synchronized;
import org.bukkit.World;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.pregenerator.ChunkUpdater;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.DecreeOrigin;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;

@Decree(name = "updater", origin = DecreeOrigin.BOTH, description = "KrudWorld World Updater")
public class CommandUpdater implements DecreeExecutor {
    private final Object lock = new Object();
    private transient ChunkUpdater chunkUpdater;

    @Decree(description = "Updates all chunk in the specified world")
    public void start(
            @Param(description = "World to update chunks at", contextual = true)
            World world
    ) {
        if (!KrudWorldToolbelt.isKrudWorldWorld(world)) {
            sender().sendMessage(C.GOLD + "This is not an KrudWorld world");
            return;
        }
        synchronized (lock) {
            if (chunkUpdater != null) {
                chunkUpdater.stop();
            }

            chunkUpdater = new ChunkUpdater(world);
            if (sender().isPlayer()) {
                sender().sendMessage(C.GREEN + "Updating " + world.getName()  + C.GRAY + " Total chunks: " + Form.f(chunkUpdater.getChunks()));
            } else {
                KrudWorld.info(C.GREEN + "Updating " + world.getName() + C.GRAY + " Total chunks: " + Form.f(chunkUpdater.getChunks()));
            }
            chunkUpdater.start();
        }
    }

    @Synchronized("lock")
    @Decree(description = "Pause the updater")
    public void pause( ) {
        if (chunkUpdater == null) {
            sender().sendMessage(C.GOLD + "You cant pause something that doesnt exist?");
            return;
        }
        boolean status = chunkUpdater.pause();
        if (sender().isPlayer()) {
            if (status) {
                sender().sendMessage(C.IRIS + "Paused task for: " + C.GRAY + chunkUpdater.getName());
            } else {
                sender().sendMessage(C.IRIS + "Unpause task for: " + C.GRAY + chunkUpdater.getName());
            }
        } else {
            if (status) {
                KrudWorld.info(C.IRIS + "Paused task for: " + C.GRAY + chunkUpdater.getName());
            } else {
                KrudWorld.info(C.IRIS + "Unpause task for: " + C.GRAY + chunkUpdater.getName());
            }
        }
    }

    @Synchronized("lock")
    @Decree(description = "Stops the updater")
    public void stop() {
        if (chunkUpdater == null) {
            sender().sendMessage(C.GOLD + "You cant stop something that doesnt exist?");
            return;
        }
        if (sender().isPlayer()) {
            sender().sendMessage("Stopping Updater for: " + C.GRAY + chunkUpdater.getName());
        } else {
            KrudWorld.info("Stopping Updater for: " + C.GRAY + chunkUpdater.getName());
        }
        chunkUpdater.stop();
    }
}


