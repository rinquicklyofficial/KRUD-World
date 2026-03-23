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
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.util.board.BoardManager;
import dev.krud.world.util.board.BoardProvider;
import dev.krud.world.util.board.BoardSettings;
import dev.krud.world.util.board.ScoreDirection;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.scheduling.J;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BoardSVC implements KrudWorldService, BoardProvider {
    private final KMap<Player, PlayerBoard> boards = new KMap<>();
    private ScheduledExecutorService executor;
    private BoardManager manager;

    @Override
    public void onEnable() {
        executor = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
        manager = new BoardManager(KrudWorld.instance, BoardSettings.builder()
                .boardProvider(this)
                .scoreDirection(ScoreDirection.DOWN)
                .build());
    }

    @Override
    public void onDisable() {
        executor.shutdownNow();
        manager.onDisable();
        boards.clear();
    }

    @EventHandler
    public void on(PlayerChangedWorldEvent e) {
        J.s(() -> updatePlayer(e.getPlayer()));
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        J.s(() -> updatePlayer(e.getPlayer()));
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    public void updatePlayer(Player p) {
        if (KrudWorldToolbelt.isKrudWorldStudioWorld(p.getWorld())) {
            manager.remove(p);
            manager.setup(p);
        } else remove(p);
    }

    private void remove(Player player) {
        manager.remove(player);
        var board = boards.remove(player);
        if (board != null) board.task.cancel(true);
    }

    @Override
    public String getTitle(Player player) {
        return C.GREEN + "KrudWorld";
    }

    @Override
    public List<String> getLines(Player player) {
        return boards.computeIfAbsent(player, PlayerBoard::new).lines;
    }

    @Data
    public class PlayerBoard {
        private final Player player;
        private final ScheduledFuture<?> task;
        private volatile List<String> lines;

        public PlayerBoard(Player player) {
            this.player = player;
            this.lines = new ArrayList<>();
            this.task = executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
        }

        private void tick() {
            if (!KrudWorld.service(StudioSVC.class).isProjectOpen()) {
                return;
            }

            update();
        }

        public void update() {
            final World world = player.getWorld();
            final Location loc = player.getLocation();

            final var access = KrudWorldToolbelt.access(world);
            if (access == null) return;

            final var engine = access.getEngine();
            if (engine == null) return;

            int x = loc.getBlockX();
            int y = loc.getBlockY() - world.getMinHeight();
            int z = loc.getBlockZ();

            List<String> lines = new ArrayList<>(this.lines.size());
            lines.add("&7&m                   ");
            lines.add(C.GREEN + "Speed" + C.GRAY + ":  " + Form.f(engine.getGeneratedPerSecond(), 0) + "/s " + Form.duration(1000D / engine.getGeneratedPerSecond(), 0));
            lines.add(C.AQUA + "Cache" + C.GRAY + ": " + Form.f(KrudWorldData.cacheSize()));
            lines.add(C.AQUA + "Mantle" + C.GRAY + ": " + engine.getMantle().getLoadedRegionCount());

            if (KrudWorldSettings.get().getGeneral().debug) {
                lines.add(C.LIGHT_PURPLE + "Carving" + C.GRAY + ": " + engine.getMantle().isCarved(x,y,z));
            }

            lines.add("&7&m                   ");
            lines.add(C.AQUA + "Region" + C.GRAY + ": " + engine.getRegion(x, z).getName());
            lines.add(C.AQUA + "Biome" + C.GRAY + ":  " + engine.getBiomeOrMantle(x, y, z).getName());
            lines.add(C.AQUA + "Height" + C.GRAY + ": " + Math.round(engine.getHeight(x, z)));
            lines.add(C.AQUA + "Slope" + C.GRAY + ":  " + Form.f(engine.getComplex().getSlopeStream().get(x, z), 2));
            lines.add(C.AQUA + "BUD/s" + C.GRAY + ": " + Form.f(engine.getBlockUpdatesPerSecond()));
            lines.add("&7&m                   ");
            this.lines = lines;
        }
    }
}
