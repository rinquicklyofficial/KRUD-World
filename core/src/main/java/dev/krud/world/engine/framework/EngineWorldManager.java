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

package dev.krud.world.engine.framework;

import org.bukkit.Chunk;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@SuppressWarnings("EmptyMethod")
public interface EngineWorldManager {
    void close();

    double getEnergy();

    int getEntityCount();

    int getChunkCount();

    double getEntitySaturation();

    void onTick();

    void onSave();

    void onBlockBreak(BlockBreakEvent e);

    void onBlockPlace(BlockPlaceEvent e);

    void onChunkLoad(Chunk e, boolean generated);

    void onChunkUnload(Chunk e);

    void chargeEnergy();

    void teleportAsync(PlayerTeleportEvent e);
}
