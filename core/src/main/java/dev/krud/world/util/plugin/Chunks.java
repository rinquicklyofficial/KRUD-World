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

package dev.krud.world.util.plugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Chunks {
    public static boolean isSafe(World w, int x, int z) {
        return w.isChunkLoaded(x, z)
                && w.isChunkLoaded(x + 1, z)
                && w.isChunkLoaded(x, z + 1)
                && w.isChunkLoaded(x - 1, z)
                && w.isChunkLoaded(x, z - 1)
                && w.isChunkLoaded(x - 1, z - 1)
                && w.isChunkLoaded(x + 1, z + 1)
                && w.isChunkLoaded(x + 1, z - 1)
                && w.isChunkLoaded(x - 1, z + 1);
    }

    public static boolean isSafe(Location l) {
        return isSafe(l.getWorld(), l.getBlockX() >> 4, l.getBlockZ() >> 4);
    }

    public static boolean hasPlayersNearby(Location at) {
        try {
            return !at.getWorld().getNearbyEntities(at, 32, 32, 32, (i) -> i instanceof Player).isEmpty();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
