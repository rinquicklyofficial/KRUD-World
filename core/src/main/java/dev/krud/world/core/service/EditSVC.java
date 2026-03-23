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

package dev.krud.world.core.service;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.edit.BlockEditor;
import dev.krud.world.core.edit.BukkitBlockEditor;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.math.M;
import dev.krud.world.util.plugin.KrudWorldService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldUnloadEvent;

public class EditSVC implements KrudWorldService {
    private KMap<World, BlockEditor> editors;
    public static boolean deletingWorld = false;

    @Override
    public void onEnable() {
        this.editors = new KMap<>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KrudWorld.instance, this::update, 1000, 1000);
    }

    @Override
    public void onDisable() {
        flushNow();
    }

    public BlockData get(World world, int x, int y, int z) {
        return open(world).get(x, y, z);
    }

    public void set(World world, int x, int y, int z, BlockData d) {
        open(world).set(x, y, z, d);
    }

    public void setBiome(World world, int x, int y, int z, Biome d) {
        open(world).setBiome(x, y, z, d);
    }

    public void setBiome(World world, int x, int z, Biome d) {
        open(world).setBiome(x, z, d);
    }

    public Biome getBiome(World world, int x, int y, int z) {
        return open(world).getBiome(x, y, z);
    }

    public Biome getBiome(World world, int x, int z) {
        return open(world).getBiome(x, z);
    }

    @EventHandler
    public void on(WorldUnloadEvent e) {
        if (editors.containsKey(e.getWorld()) && !deletingWorld) {
            editors.remove(e.getWorld()).close();
        }
    }


    public void update() {
        for (World i : editors.k()) {
            if (M.ms() - editors.get(i).last() > 1000) {
                editors.remove(i).close();
            }
        }
    }

    public void flushNow() {
        for (World i : editors.k()) {
            editors.remove(i).close();
        }
    }

    public BlockEditor open(World world) {
        if (editors.containsKey(world)) {
            return editors.get(world);
        }

        BlockEditor e = new BukkitBlockEditor(world);
        editors.put(world, e);

        return e;
    }

}
