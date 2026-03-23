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

import dev.krud.world.util.data.MaterialBlock;
import org.bukkit.Material;

@SuppressWarnings("ClassCanBeRecord")
public class UIStaticDecorator implements WindowDecorator {
    private final Element element;

    public UIStaticDecorator(Element element) {
        this.element = element == null ? new UIElement("bg").setMaterial(new MaterialBlock(Material.AIR)) : element;
    }

    @Override
    public Element onDecorateBackground(Window window, int position, int row) {
        return element;
    }
}
