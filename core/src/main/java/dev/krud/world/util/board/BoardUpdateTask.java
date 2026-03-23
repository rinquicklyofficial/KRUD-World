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

package dev.krud.world.util.board;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author Missionary (missionarymc@gmail.com)
 * @since 5/31/2018
 */
@RequiredArgsConstructor
public class BoardUpdateTask extends BukkitRunnable {

    private static final Predicate<UUID> PLAYER_IS_ONLINE = uuid -> Bukkit.getPlayer(uuid) != null;

    private final BoardManager boardManager;

    @Override
    public void run() {
        for (var entry : boardManager.getScoreboards().entrySet()) {
            if (!PLAYER_IS_ONLINE.test(entry.getKey())) {
                continue;
            }

            entry.getValue().update();
        }
    }
}
