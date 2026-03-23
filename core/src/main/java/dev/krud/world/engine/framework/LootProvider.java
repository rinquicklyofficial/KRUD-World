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

import dev.krud.world.engine.object.InventorySlotType;
import dev.krud.world.engine.object.KrudWorldLootReference;
import dev.krud.world.engine.object.KrudWorldLootTable;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.math.RNG;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

public interface LootProvider {
    void scramble(Inventory inventory, RNG rng);

    void injectTables(KList<KrudWorldLootTable> list, KrudWorldLootReference r, boolean fallback);

    KList<KrudWorldLootTable> getLootTables(RNG rng, Block b);

    void addItems(boolean debug, Inventory inv, RNG rng, KList<KrudWorldLootTable> tables, InventorySlotType slot, World world, int x, int y, int z, int mgf);
}
