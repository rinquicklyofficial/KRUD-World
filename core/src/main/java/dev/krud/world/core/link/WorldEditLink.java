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

package dev.krud.world.core.link;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.util.data.Cuboid;
import dev.krud.world.util.data.KCache;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.UUID;

public class WorldEditLink {
    private static final AtomicCache<Boolean> active = new AtomicCache<>();

    public static Cuboid getSelection(Player p) {
        if (!hasWorldEdit())
            return null;

        try {
            Object instance = Class.forName("com.sk89q.worldedit.WorldEdit").getDeclaredMethod("getInstance").invoke(null);
            Object sessionManager = instance.getClass().getDeclaredMethod("getSessionManager").invoke(instance);
            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object world = bukkitAdapter.getDeclaredMethod("adapt", World.class).invoke(null, p.getWorld());
            Object player = bukkitAdapter.getDeclaredMethod("adapt", Player.class).invoke(null, p);
            Object localSession = sessionManager.getClass().getDeclaredMethod("getIfPresent", Class.forName("com.sk89q.worldedit.session.SessionOwner")).invoke(sessionManager, player);
            if (localSession == null) return null;

            Object region = null;
            try {
                region = localSession.getClass().getDeclaredMethod("getSelection", Class.forName("com.sk89q.worldedit.world.World")).invoke(localSession, world);
            } catch (InvocationTargetException ignored) {}
            if (region == null) return null;

            Object min = region.getClass().getDeclaredMethod("getMinimumPoint").invoke(region);
            Object max = region.getClass().getDeclaredMethod("getMaximumPoint").invoke(region);
            return new Cuboid(p.getWorld(),
                    (int) min.getClass().getDeclaredMethod("x").invoke(min),
                    (int) min.getClass().getDeclaredMethod("y").invoke(min),
                    (int) min.getClass().getDeclaredMethod("z").invoke(min),
                    (int) min.getClass().getDeclaredMethod("x").invoke(max),
                    (int) min.getClass().getDeclaredMethod("y").invoke(max),
                    (int) min.getClass().getDeclaredMethod("z").invoke(max)
            );
        } catch (Throwable e) {
            KrudWorld.error("Could not get selection");
            e.printStackTrace();
            KrudWorld.reportError(e);
            active.reset();
            active.aquire(() -> false);
        }
        return null;
    }

    public static boolean hasWorldEdit() {
        return active.aquire(() -> Bukkit.getPluginManager().isPluginEnabled("WorldEdit"));
    }
}
