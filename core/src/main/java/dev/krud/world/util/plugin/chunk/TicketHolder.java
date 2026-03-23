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

package dev.krud.world.util.plugin.chunk;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.util.collection.KMap;
import lombok.NonNull;
import org.bukkit.Chunk;
import org.bukkit.World;

public class TicketHolder {
    private final World world;
    private final KMap<Long, Long> tickets = new KMap<>();

    public TicketHolder(@NonNull World world) {
        this.world = world;
    }

    public void addTicket(@NonNull Chunk chunk) {
        if (chunk.getWorld() != world) return;
        addTicket(chunk.getX(), chunk.getZ());
    }

    public void addTicket(int x, int z) {
        tickets.compute(Cache.key(x, z), ($, ref) -> {
            if (ref == null) {
                world.addPluginChunkTicket(x, z, KrudWorld.instance);
                return 1L;
            }
            return ++ref;
        });
    }

    public boolean removeTicket(@NonNull Chunk chunk) {
        if (chunk.getWorld() != world) return false;
        return removeTicket(chunk.getX(), chunk.getZ());
    }

    public boolean removeTicket(int x, int z) {
        return tickets.compute(Cache.key(x, z), ($, ref) -> {
            if (ref == null) return null;
            if (--ref <= 0) {
                world.removePluginChunkTicket(x, z, KrudWorld.instance);
                return null;
            }
            return ref;
        }) == null;
    }
}
