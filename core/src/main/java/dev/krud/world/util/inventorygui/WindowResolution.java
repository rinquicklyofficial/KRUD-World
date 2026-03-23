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

package dev.krud.world.util.inventorygui;

import org.bukkit.event.inventory.InventoryType;

public enum WindowResolution {
    W9_H6(9, 6, InventoryType.CHEST),
    W5_H1(5, 1, InventoryType.HOPPER),
    W3_H3(3, 3, InventoryType.DROPPER);

    private final int width;
    private final int maxHeight;
    private final InventoryType type;

    WindowResolution(int w, int h, InventoryType type) {
        this.width = w;
        this.maxHeight = h;
        this.type = type;
    }

    public int getMaxWidthOffset() {
        return (getWidth() - 1) / 2;
    }

    public int getWidth() {
        return width;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public InventoryType getType() {
        return type;
    }
}
