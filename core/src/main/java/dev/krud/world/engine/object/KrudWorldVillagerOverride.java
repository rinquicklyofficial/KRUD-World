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

import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Snippet("villager-override")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Override cartographer map trades with others or disable the trade altogether")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldVillagerOverride {
    @Desc("""
            Disable the trade altogether.
            If a cartographer villager gets a new explorer map trade:
            If this is enabled -> the trade is removed
            If this is disabled -> the trade is replaced with the "override" setting below
            Default is true, so if you omit this, trades will be removed.""")
    private boolean disableTrade = true;

    @DependsOn("disableTrade")
    @Required
    @Desc("""
            The items to override the cartographer trade with.
            By default, this is:
                3 emeralds + 3 glass blocks -> 1 spyglass.
                Can trade 3 to 5 times""")
    @ArrayType(min = 1, type = KrudWorldVillagerTrade.class)
    private KList<KrudWorldVillagerTrade> items = new KList<>(new KrudWorldVillagerTrade()
            .setIngredient1(new ItemStack(Material.EMERALD, 3))
            .setIngredient2(new ItemStack(Material.GLASS, 3))
            .setResult(new ItemStack(Material.SPYGLASS))
            .setMinTrades(3)
            .setMaxTrades(5));

    public KList<KrudWorldVillagerTrade> getValidItems() {
        KList<KrudWorldVillagerTrade> valid = new KList<>();
        getItems().stream().filter(KrudWorldVillagerTrade::isValidItems).forEach(valid::add);
        return valid.size() == 0 ? null : valid;
    }
}
