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

package dev.krud.world.core.service;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.commands.CommandKrudWorld;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.decree.DecreeContext;
import dev.krud.world.util.decree.DecreeSystem;
import dev.krud.world.util.decree.virtual.VirtualDecreeCommand;
import dev.krud.world.util.format.C;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CommandSVC implements KrudWorldService, DecreeSystem {
    private final KMap<String, CompletableFuture<String>> futures = new KMap<>();
    private final transient AtomicCache<VirtualDecreeCommand> commandCache = new AtomicCache<>();
    private CompletableFuture<String> consoleFuture = null;

    @Override
    public void onEnable() {
        KrudWorld.instance.getCommand("iris").setExecutor(this);
        J.a(() -> {
            DecreeContext.touch(KrudWorld.getSender());
            try {
                getRoot().cacheAll();
            } finally {
                DecreeContext.remove();
            }
        });
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().startsWith("/") ? e.getMessage().substring(1) : e.getMessage();

        if (msg.startsWith("irisdecree ")) {
            String[] args = msg.split("\\Q \\E");
            CompletableFuture<String> future = futures.get(args[1]);

            if (future != null) {
                future.complete(args[2]);
                e.setCancelled(true);
                return;
            }
        }

        if ((msg.startsWith("locate ") || msg.startsWith("locatebiome ")) && KrudWorldToolbelt.isKrudWorldWorld(e.getPlayer().getWorld())) {
            new VolmitSender(e.getPlayer()).sendMessage(C.RED + "Locating biomes & objects is disabled in KrudWorld Worlds. Use /iris studio goto <biome>");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(ServerCommandEvent e) {
        if (consoleFuture != null && !consoleFuture.isCancelled() && !consoleFuture.isDone()) {
            if (!e.getCommand().contains(" ")) {
                String pick = e.getCommand().trim().toLowerCase(Locale.ROOT);
                consoleFuture.complete(pick);
                e.setCancelled(true);
            }
        }
    }

    @Override
    public VirtualDecreeCommand getRoot() {
        return commandCache.aquireNastyPrint(() -> VirtualDecreeCommand.createRoot(new CommandKrudWorld()));
    }

    public void post(String password, CompletableFuture<String> future) {
        futures.put(password, future);
    }

    public void postConsole(CompletableFuture<String> future) {
        consoleFuture = future;
    }
}
