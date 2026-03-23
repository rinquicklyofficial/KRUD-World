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

package dev.krud.world.core.link.data;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.link.ExternalDataProvider;
import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.nms.container.BiomeColor;
import dev.krud.world.core.nms.container.BlockProperty;
import dev.krud.world.core.service.ExternalDataSVC;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.B;
import dev.krud.world.util.data.KrudWorldCustomData;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.serialize.Chroma;
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.CrucibleItem;
import io.lumine.mythiccrucible.items.ItemManager;
import io.lumine.mythiccrucible.items.blocks.CustomBlockItemContext;
import io.lumine.mythiccrucible.items.furniture.FurnitureItemContext;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

public class MythicCrucibleDataProvider extends ExternalDataProvider {

    private ItemManager itemManager;

    public MythicCrucibleDataProvider() {
        super("MythicCrucible");
    }

    @Override
    public void init() {
        KrudWorld.info("Setting up MythicCrucible Link...");
        try {
            this.itemManager = MythicCrucible.inst().getItemManager();
        } catch (Exception e) {
            KrudWorld.error("Failed to set up MythicCrucible Link: Unable to fetch MythicCrucible instance!");
        }
    }

    @NotNull
    @Override
    public BlockData getBlockData(@NotNull Identifier blockId, @NotNull KMap<String, String> state) throws MissingResourceException {
        CrucibleItem crucibleItem = this.itemManager.getItem(blockId.key())
                .orElseThrow(() -> new MissingResourceException("Failed to find BlockData!", blockId.namespace(), blockId.key()));
        CustomBlockItemContext blockItemContext = crucibleItem.getBlockData();
        FurnitureItemContext furnitureItemContext = crucibleItem.getFurnitureData();
        if (furnitureItemContext != null) {
            return KrudWorldCustomData.of(B.getAir(), ExternalDataSVC.buildState(blockId, state));
        } else if (blockItemContext != null) {
            return blockItemContext.getBlockData();
        }
        throw new MissingResourceException("Failed to find BlockData!", blockId.namespace(), blockId.key());
    }

    @Override
    public @NotNull List<BlockProperty> getBlockProperties(@NotNull Identifier blockId) throws MissingResourceException {
        CrucibleItem crucibleItem = this.itemManager.getItem(blockId.key())
                .orElseThrow(() -> new MissingResourceException("Failed to find BlockData!", blockId.namespace(), blockId.key()));

        if (crucibleItem.getFurnitureData() != null) {
            return YAW_FACE_BIOME_PROPERTIES;
        } else if (crucibleItem.getBlockData() != null) {
            return List.of();
        }
        throw new MissingResourceException("Failed to find BlockData!", blockId.namespace(), blockId.key());
    }

    @NotNull
    @Override
    public ItemStack getItemStack(@NotNull Identifier itemId, @NotNull KMap<String, Object> customNbt) throws MissingResourceException {
        Optional<CrucibleItem> opt = this.itemManager.getItem(itemId.key());
        return BukkitAdapter.adapt(opt.orElseThrow(() ->
                new MissingResourceException("Failed to find ItemData!", itemId.namespace(), itemId.key()))
                .getMythicItem()
                .generateItemStack(1));
    }

    @Override
    public @NotNull Collection<@NotNull Identifier> getTypes(@NotNull DataType dataType) {
        return itemManager.getItems()
                .stream()
                .map(i -> new Identifier("crucible", i.getInternalName()))
                .filter(dataType.asPredicate(this))
                .toList();
    }

    @Override
    public void processUpdate(@NotNull Engine engine, @NotNull Block block, @NotNull Identifier blockId) {
        var parsedState = ExternalDataSVC.parseState(blockId);
        var state = parsedState.getB();
        blockId = parsedState.getA();

        Optional<CrucibleItem> item = itemManager.getItem(blockId.key());
        if (item.isEmpty()) return;
        FurnitureItemContext furniture = item.get().getFurnitureData();
        if (furniture == null) return;

        var pair = parseYawAndFace(engine, block, state);
        BiomeColor type = null;
        Chroma color = null;
        try {
            type = BiomeColor.valueOf(state.get("matchBiome").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException ignored) {}
        if (type != null) {
            var biomeColor = INMS.get().getBiomeColor(block.getLocation(), type);
            if (biomeColor == null) return;
            color = Chroma.of(biomeColor.getRGB());
        }
        furniture.place(block, pair.getB(), pair.getA(), color);
    }

    @Override
    public boolean isValidProvider(@NotNull Identifier key, DataType dataType) {
        if (dataType == DataType.ENTITY) return false;
        return key.namespace().equalsIgnoreCase("crucible");
    }
}
