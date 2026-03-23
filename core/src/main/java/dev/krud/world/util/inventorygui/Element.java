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

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.MaterialBlock;
import dev.krud.world.util.scheduling.Callback;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("ALL")
public interface Element {
    MaterialBlock getMaterial();

    Element setMaterial(MaterialBlock b);

    boolean isEnchanted();

    Element setEnchanted(boolean enchanted);

    String getId();

    String getName();

    Element setName(String name);

    double getProgress();

    Element setProgress(double progress);

    short getEffectiveDurability();

    int getCount();

    Element setCount(int c);

    ItemStack computeItemStack();

    Element setBackground(boolean bg);

    boolean isBackgrond();

    Element addLore(String loreLine);

    KList<String> getLore();

    Element call(ElementEvent event, Element context);

    Element onLeftClick(Callback<Element> clicked);

    Element onRightClick(Callback<Element> clicked);

    Element onShiftLeftClick(Callback<Element> clicked);

    Element onShiftRightClick(Callback<Element> clicked);

    Element onDraggedInto(Callback<Element> into);

    Element onOtherDraggedInto(Callback<Element> other);
}
