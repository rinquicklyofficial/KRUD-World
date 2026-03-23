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

import dev.krud.world.KrudWorld;
import org.bukkit.event.Listener;

public interface KrudWorldService extends Listener {
    void onEnable();

    void onDisable();

    default void postShutdown(Runnable r) {
        KrudWorld.instance.postShutdown(r);
    }
}
