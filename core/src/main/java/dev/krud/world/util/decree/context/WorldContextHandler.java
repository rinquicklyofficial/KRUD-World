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

package dev.krud.world.util.decree.context;

import dev.krud.world.util.decree.DecreeContextHandler;
import dev.krud.world.util.plugin.VolmitSender;
import org.bukkit.World;

public class WorldContextHandler implements DecreeContextHandler<World> {
    public Class<World> getType() {
        return World.class;
    }

    public World handle(VolmitSender sender) {
        return sender.isPlayer() ? sender.player().getWorld() : null;
    }
}
