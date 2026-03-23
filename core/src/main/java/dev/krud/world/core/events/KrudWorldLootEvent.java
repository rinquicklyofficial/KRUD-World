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

package dev.krud.world.core.events;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.InventorySlotType;
import dev.krud.world.engine.object.KrudWorldLootTable;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.scheduling.J;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.LootGenerateEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@Getter
public class KrudWorldLootEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private static final LootTable EMPTY = new LootTable() {
        @NotNull
        @Override
        public NamespacedKey getKey() {
            return new NamespacedKey(KrudWorld.instance, "empty");
        }

        @NotNull
        @Override
        public Collection<ItemStack> populateLoot(@Nullable Random random, @NotNull LootContext context) {
            return List.of();
        }

        @Override
        public void fillInventory(@NotNull Inventory inventory, @Nullable Random random, @NotNull LootContext context) {
        }
    };

    private final Engine engine;
    private final Block block;
    private final InventorySlotType slot;
    private final KList<KrudWorldLootTable> tables;

    /**
     * Constructor for KrudWorldLootEvent with mode selection.
     *
     * @param engine The engine instance.
     * @param block The block associated with the event.
     * @param slot The inventory slot type.
     * @param tables The list of KrudWorldLootTables. (mutable*)
     */
    public KrudWorldLootEvent(Engine engine, Block block, InventorySlotType slot, KList<KrudWorldLootTable> tables) {
        this.engine = engine;
        this.block = block;
        this.slot = slot;
        this.tables = tables;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Required method to get the HandlerList for this event.
     *
     * @return The HandlerList.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Triggers the corresponding Bukkit loot event.
     * This method integrates your custom KrudWorldLootTables with Bukkit's LootGenerateEvent,
     * allowing other plugins to modify or cancel the loot generation.
     *
     * @return true when the event was canceled
     */
    public static boolean callLootEvent(KList<ItemStack> loot, Inventory inv, World world, int x, int y, int z) {
        InventoryHolder holder = inv.getHolder();
        Location loc = new Location(world, x, y, z);
        if (holder == null) {
            holder = new InventoryHolder() {
                @NotNull
                @Override
                public Inventory getInventory() {
                    return inv;
                }
            };
        }

        LootContext context = new LootContext.Builder(loc).build();
        LootGenerateEvent event = new LootGenerateEvent(world, null, holder, EMPTY, context, loot, true);
        if (!Bukkit.isPrimaryThread()) {
            KrudWorld.warn("LootGenerateEvent was not called on the main thread, please report this issue.");
            Thread.dumpStack();
            J.sfut(() -> Bukkit.getPluginManager().callEvent(event)).join();
        } else Bukkit.getPluginManager().callEvent(event);

        return event.isCancelled();
    }
}