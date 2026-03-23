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

import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a loot table. Biomes, Regions & Objects can add or replace the virtual table with these loot tables")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldLootTable extends KrudWorldRegistrant {
    @Required
    @Desc("The name of this loot table")
    @MinNumber(2)
    private String name = "";

    @MinNumber(1)
    @Desc("The rarity as in 1 in X chance")
    private int rarity = 1;

    @MinNumber(1)
    @Desc("The maximum amount of loot that can be picked in this table at a time.")
    private int maxPicked = 5;

    @MinNumber(0)
    @Desc("The minimum amount of loot that can be picked in this table at a time.")
    private int minPicked = 1;

    @MinNumber(1)
    @Desc("The maximum amount of tries to generate loot")
    private int maxTries = 10;

    @Desc("The loot in this table")
    @ArrayType(min = 1, type = KrudWorldLoot.class)
    private KList<KrudWorldLoot> loot = new KList<>();

    public KList<ItemStack> getLoot(boolean debug, RNG rng, InventorySlotType slot, World world, int x, int y, int z) {
        KList<ItemStack> lootf = new KList<>();

        int m = 0;
        int c = 0;
        int mx = rng.i(getMinPicked(), getMaxPicked());

        while (m < mx && c++ < getMaxTries()) {
            int num = rng.i(loot.size());

            KrudWorldLoot l = loot.get(num);

            if (l.getSlotTypes() == slot) {
                ItemStack item = l.get(debug, false, this, rng, x, y, z);

                if (item != null && item.getType() != Material.AIR) {
                    lootf.add(item);
                    m++;
                }
            }
        }

        return lootf;
    }

    @Override
    public String getFolderName() {
        return "loot";
    }

    @Override
    public String getTypeName() {
        return "Loot";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
