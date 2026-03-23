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

package dev.krud.world.util.decree;

import dev.krud.world.util.plugin.VolmitSender;

public enum DecreeOrigin {
    PLAYER,
    CONSOLE,
    /**
     * Both the player and the console
     */
    BOTH;

    /**
     * Check if the origin is valid for a sender
     *
     * @param sender The sender to check
     * @return True if valid for origin
     */
    public boolean validFor(VolmitSender sender) {
        if (sender.isPlayer()) {
            return this.equals(PLAYER) || this.equals(BOTH);
        } else {
            return this.equals(CONSOLE) || this.equals(BOTH);
        }
    }
}
