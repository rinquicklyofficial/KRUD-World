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

package dev.krud.world.engine.object;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.util.collection.KList;
import org.bukkit.block.data.BlockData;

public interface IObjectLoot {
    KList<KrudWorldBlockData> getFilter();
    KList<BlockData> getFilter(KrudWorldData manager);
    boolean isExact();
    String getName();
    int getWeight();
}
