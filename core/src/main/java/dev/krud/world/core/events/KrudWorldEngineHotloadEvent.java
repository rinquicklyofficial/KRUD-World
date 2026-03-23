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

package dev.krud.world.core.events;

import dev.krud.world.engine.framework.Engine;
import org.bukkit.event.HandlerList;

public class KrudWorldEngineHotloadEvent extends KrudWorldEngineEvent {
    private static final HandlerList handlers = new HandlerList();

    public KrudWorldEngineHotloadEvent(Engine engine) {
        super(engine);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
