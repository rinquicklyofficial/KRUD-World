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
import dev.krud.world.util.math.RNG;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.io.File;

@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldVanillaLootTable extends KrudWorldLootTable {
    private final LootTable lootTable;

    @Override
    public String getName() {
        return "Vanilla " + lootTable.getKey();
    }

    @Override
    public int getRarity() {
        return 0;
    }

    @Override
    public int getMaxPicked() {
        return 0;
    }

    @Override
    public int getMinPicked() {
        return 0;
    }

    @Override
    public int getMaxTries() {
        return 0;
    }

    @Override
    public KList<KrudWorldLoot> getLoot() {
        return new KList<>();
    }

    @Override
    public KList<ItemStack> getLoot(boolean debug, RNG rng, InventorySlotType slot, World world, int x, int y, int z) {
        return new KList<>(lootTable.populateLoot(rng, new LootContext.Builder(new Location(world, x, y, z)).build()));
    }

    @Override
    public String getFolderName() {
        throw new UnsupportedOperationException("VanillaLootTables do not have a folder name");
    }

    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException("VanillaLootTables do not have a type name");
    }

    @Override
    public File getLoadFile() {
        throw new UnsupportedOperationException("VanillaLootTables do not have a load file");
    }

    @Override
    public KrudWorldData getLoader() {
        throw new UnsupportedOperationException("VanillaLootTables do not have a loader");
    }

    @Override
    public KList<String> getPreprocessors() {
        return new KList<>();
    }
}
