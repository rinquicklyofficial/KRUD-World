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

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;


@Snippet("enchantment")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents an enchantment & level")
@Data
public class KrudWorldEnchantment {
    @Required
    @RegistryListEnchantment
    @Desc("The enchantment")
    private String enchantment;

    @MinNumber(1)
    @Desc("Minimum amount of this loot")
    private int minLevel = 1;

    @MinNumber(1)
    @Desc("Maximum amount of this loot")
    private int maxLevel = 1;

    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The chance that this enchantment is applied (0 to 1)")
    private double chance = 1;

    public void apply(RNG rng, ItemMeta meta) {
        try {
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(getEnchantment()));
            if (enchant == null) {
                KrudWorld.warn("Unknown Enchantment: " + getEnchantment());
                return;
            }
            if (rng.nextDouble() < chance) {
                if (meta instanceof EnchantmentStorageMeta) {
                    ((EnchantmentStorageMeta) meta).addStoredEnchant(enchant, getLevel(rng), true);
                    return;
                }
                meta.addEnchant(enchant, getLevel(rng), true);
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }
    }

    public int getLevel(RNG rng) {
        return rng.i(getMinLevel(), getMaxLevel());
    }
}
