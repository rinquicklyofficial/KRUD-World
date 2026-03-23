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

package dev.krud.world.engine.framework.placer;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.core.events.KrudWorldLootEvent;
import dev.krud.world.engine.mantle.EngineMantle;
import dev.krud.world.engine.object.IObjectPlacer;
import dev.krud.world.engine.object.InventorySlotType;
import dev.krud.world.engine.object.KrudWorldLootTable;
import dev.krud.world.engine.object.TileData;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.B;
import dev.krud.world.util.data.KrudWorldCustomData;
import dev.krud.world.util.math.RNG;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.InventoryHolder;

@Getter
@EqualsAndHashCode(exclude = {"engine", "mantle"})
public class WorldObjectPlacer implements IObjectPlacer {
    private final World world;
    private final Engine engine;
    private final EngineMantle mantle;

    public WorldObjectPlacer(World world) {
        var a = KrudWorldToolbelt.access(world);
        if (a == null || a.getEngine() == null) throw new IllegalStateException(world.getName() + " is not an KrudWorld World!");
        this.world = world;
        this.engine = a.getEngine();
        this.mantle = engine.getMantle();
    }

    @Override
    public int getHighest(int x, int z, KrudWorldData data) {
        return mantle.getHighest(x, z, data);
    }

    @Override
    public int getHighest(int x, int z, KrudWorldData data, boolean ignoreFluid) {
        return mantle.getHighest(x, z, data, ignoreFluid);
    }

    @Override
    public void set(int x, int y, int z, BlockData d) {
        Block block = world.getBlockAt(x, y + world.getMinHeight(), z);

        if (y <= world.getMinHeight() || block.getType() == Material.BEDROCK) return;
        InventorySlotType slot = null;
        if (B.isStorageChest(d)) {
            slot = InventorySlotType.STORAGE;
        }

        if (d instanceof KrudWorldCustomData data) {
            block.setBlockData(data.getBase(), false);
            KrudWorld.warn("Tried to place custom block at " + x + ", " + y + ", " + z + " which is not supported!");
        } else block.setBlockData(d, false);

        if (slot != null) {
            RNG rx = new RNG(Cache.key(x, z));
            KList<KrudWorldLootTable> tables = engine.getLootTables(rx, block);

            try {
                Bukkit.getPluginManager().callEvent(new KrudWorldLootEvent(engine, block, slot, tables));

                if (!tables.isEmpty()){
                    KrudWorld.debug("KrudWorldLootEvent has been accessed");
                }

                if (tables.isEmpty())
                    return;
                InventoryHolder m = (InventoryHolder) block.getState();
                engine.addItems(false, m.getInventory(), rx, tables, slot, world, x, y, z, 15);
            } catch (Throwable e) {
                KrudWorld.reportError(e);
            }
        }
    }

    @Override
    public BlockData get(int x, int y, int z) {
        return world.getBlockAt(x, y + world.getMinHeight(), z).getBlockData();
    }

    @Override
    public boolean isPreventingDecay() {
        return mantle.isPreventingDecay();
    }

    @Override
    public boolean isCarved(int x, int y, int z) {
        return mantle.isCarved(x, y, z);
    }

    @Override
    public boolean isSolid(int x, int y, int z) {
        return world.getBlockAt(x, y + world.getMinHeight(), z).getType().isSolid();
    }

    @Override
    public boolean isUnderwater(int x, int z) {
        return mantle.isUnderwater(x, z);
    }

    @Override
    public int getFluidHeight() {
        return mantle.getFluidHeight();
    }

    @Override
    public boolean isDebugSmartBore() {
        return mantle.isDebugSmartBore();
    }

    @Override
    public void setTile(int xx, int yy, int zz, TileData tile) {
        tile.toBukkitTry(world.getBlockAt(xx, yy + world.getMinHeight(), zz));
    }

    @Override
    public <T> void setData(int xx, int yy, int zz, T data) {
    }

    @Override
    public <T> T getData(int xx, int yy, int zz, Class<T> t) {
        return null;
    }
}
