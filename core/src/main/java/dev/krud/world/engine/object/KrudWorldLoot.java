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
import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.service.ExternalDataSVC;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.B;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Colorable;

import java.awt.*;
import java.util.Optional;

@Snippet("loot")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a loot entry")
@Data
public class KrudWorldLoot {
    private final transient AtomicCache<CNG> chance = new AtomicCache<>();
    @Desc("The target inventory slot types to fill this loot with")
    private InventorySlotType slotTypes = InventorySlotType.STORAGE;
    @MinNumber(1)
    @Desc("The sub rarity of this loot. Calculated after this loot table has been picked.")
    private int rarity = 1;
    @MinNumber(1)
    @Desc("Minimum amount of this loot")
    private int minAmount = 1;
    @MinNumber(1)
    @Desc("Maximum amount of this loot")
    private int maxAmount = 1;
    @MinNumber(1)
    @Desc("The display name of this item")
    private String displayName = null;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("Minimum durability percent")
    private double minDurability = 0;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("Maximum durability percent")
    private double maxDurability = 1;
    @Desc("Define a custom model identifier 1.14+ only")
    private Integer customModel = null;
    @Desc("Set this to true to prevent it from being broken")
    private boolean unbreakable = false;
    @ArrayType(min = 1, type = ItemFlag.class)
    @Desc("The item flags to add")
    private KList<ItemFlag> itemFlags = new KList<>();
    @Desc("Apply enchantments to this item")
    @ArrayType(min = 1, type = KrudWorldEnchantment.class)
    private KList<KrudWorldEnchantment> enchantments = new KList<>();
    @Desc("Apply attribute modifiers to this item")
    @ArrayType(min = 1, type = KrudWorldAttributeModifier.class)
    private KList<KrudWorldAttributeModifier> attributes = new KList<>();
    @ArrayType(min = 1, type = String.class)
    @Desc("Add lore to this item")
    private KList<String> lore = new KList<>();
    @RegistryListItemType
    @Required
    @Desc("This is the item or block type. Does not accept minecraft:*, only materials such as DIAMOND_SWORD or DIRT. The exception are modded materials, as they require a namespace.")
    private String type = "";
    @Desc("The dye color")
    private DyeColor dyeColor = null;
    @Desc("The leather armor color")
    private String leatherColor = null;
    @Desc("Defines a custom NBT Tag for the item.")
    private KMap<String, Object> customNbt;

    public Material getType() {
        return B.getMaterial(type);
    }

    public ItemStack get(boolean debug, RNG rng) {
        try {
            ItemStack is = getItemStack(rng);
            if (is == null)
                return new ItemStack(Material.AIR);
            is.setItemMeta(applyProperties(is, rng, debug, null));
            return INMS.get().applyCustomNbt(is, customNbt);
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            return new ItemStack(Material.AIR);
        }
    }

    public ItemStack get(boolean debug, boolean giveSomething, KrudWorldLootTable table, RNG rng, int x, int y, int z) {
        if (debug) {
            chance.reset();
        }

        if (giveSomething || chance.aquire(() -> NoiseStyle.STATIC.create(rng)).fit(1, rarity * table.getRarity(), x, y, z) == 1) {
            try {
                ItemStack is = getItemStack(rng);
                if (is == null)
                    return null;
                is.setItemMeta(applyProperties(is, rng, debug, table));
                return INMS.get().applyCustomNbt(is, customNbt);
            } catch (Throwable e) {
                //KrudWorld.reportError(e);
                e.printStackTrace();
            }
        }

        return null;
    }

    // TODO Better Third Party Item Acquisition
    private ItemStack getItemStack(RNG rng) {
        if (!type.startsWith("minecraft:") && type.contains(":")) {
            Optional<ItemStack> opt = KrudWorld.service(ExternalDataSVC.class).getItemStack(Identifier.fromString(type), customNbt);
            if (opt.isEmpty()) {
                KrudWorld.warn("Unknown Material: " + type);
                return new ItemStack(Material.AIR);
            }
            ItemStack is = opt.get();
            is.setAmount(Math.max(1, rng.i(getMinAmount(), getMaxAmount())));
            return is;
        }
        return new ItemStack(getType(), Math.max(1, rng.i(getMinAmount(), getMaxAmount())));
    }

    private ItemMeta applyProperties(ItemStack is, RNG rng, boolean debug, KrudWorldLootTable table) {
        ItemMeta m = is.getItemMeta();
        if (m == null) {
            return null;
        }

        for (KrudWorldEnchantment i : getEnchantments()) {
            i.apply(rng, m);
        }

        for (KrudWorldAttributeModifier i : getAttributes()) {
            i.apply(rng, m);
        }

        m.setUnbreakable(isUnbreakable());
        for (ItemFlag i : getItemFlags()) {
            m.addItemFlags(i);
        }

        if (getCustomModel() != null) {
            m.setCustomModelData(getCustomModel());
        }

        if (is.getType().getMaxDurability() > 0 && m instanceof Damageable d) {
            int max = is.getType().getMaxDurability();
            d.setDamage((int) Math.round(Math.max(0, Math.min(max, (1D - rng.d(getMinDurability(), getMaxDurability())) * max))));
        }

        if (getLeatherColor() != null && m instanceof LeatherArmorMeta leather) {
            Color c = Color.decode(getLeatherColor());
            leather.setColor(org.bukkit.Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
        }

        if (getDyeColor() != null && m instanceof Colorable colorable) {
            colorable.setColor(getDyeColor());
        }

        if (displayName != null) {
            m.setLocalizedName(C.translateAlternateColorCodes('&', displayName));
            m.setDisplayName(C.translateAlternateColorCodes('&', displayName));
        }

        KList<String> lore = new KList<>();

        getLore().forEach((i) ->
        {
            String mf = C.translateAlternateColorCodes('&', i);

            if (mf.length() > 24) {
                for (String g : Form.wrapWords(mf, 24).split("\\Q\n\\E")) {
                    lore.add(g.trim());
                }
            } else {
                lore.add(mf);
            }
        });

        if (debug) {
            if (table == null) {
                if (lore.isNotEmpty()) {
                    lore.add(C.GRAY + "--------------------");
                }
                lore.add(C.GRAY + "1 in " + (getRarity()) + " Chance (" + Form.pc(1D / (getRarity()), 5) + ")");
            } else {
                if (lore.isNotEmpty()) {
                    lore.add(C.GRAY + "--------------------");
                }

                lore.add(C.GRAY + "From: " + table.getName() + " (" + Form.pc(1D / table.getRarity(), 5) + ")");
                lore.add(C.GRAY + "1 in " + (table.getRarity() * getRarity()) + " Chance (" + Form.pc(1D / (table.getRarity() * getRarity()), 5) + ")");
            }
        }

        m.setLore(lore);

        return m;
    }
}
