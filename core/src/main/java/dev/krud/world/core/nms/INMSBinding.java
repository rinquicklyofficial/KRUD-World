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

package dev.krud.world.core.nms;

import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.nms.container.BiomeColor;
import dev.krud.world.core.nms.container.BlockProperty;
import dev.krud.world.core.nms.container.StructurePlacement;
import dev.krud.world.core.nms.datapack.DataVersion;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.math.Vector3d;
import dev.krud.world.util.nbt.mca.palette.MCABiomeContainer;
import dev.krud.world.util.nbt.mca.palette.MCAPaletteAccess;
import dev.krud.world.util.nbt.tag.CompoundTag;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;

import java.awt.Color;
import java.util.List;

public interface INMSBinding {
    boolean hasTile(Material material);

    boolean hasTile(Location l);

    KMap<String, Object> serializeTile(Location location);

    void deserializeTile(KMap<String, Object> s, Location newPosition);

    CompoundTag serializeEntity(Entity location);

    Entity deserializeEntity(CompoundTag s, Location newPosition);

    boolean supportsCustomHeight();

    Object getBiomeBaseFromId(int id);

    int getMinHeight(World world);

    boolean supportsCustomBiomes();

    int getTrueBiomeBaseId(Object biomeBase);

    Object getTrueBiomeBase(Location location);

    String getTrueBiomeBaseKey(Location location);

    Object getCustomBiomeBaseFor(String mckey);

    Object getCustomBiomeBaseHolderFor(String mckey);

    int getBiomeBaseIdForKey(String key);

    String getKeyForBiomeBase(Object biomeBase);

    Object getBiomeBase(World world, Biome biome);

    Object getBiomeBase(Object registry, Biome biome);

    KList<Biome> getBiomes();

    boolean isBukkit();

    int getBiomeId(Biome biome);

    MCABiomeContainer newBiomeContainer(int min, int max, int[] data);

    MCABiomeContainer newBiomeContainer(int min, int max);

    default World createWorld(WorldCreator c) {
        if (c.generator() instanceof PlatformChunkGenerator gen
                && missingDimensionTypes(gen.getTarget().getDimension().getDimensionTypeKey()))
            throw new IllegalStateException("Missing dimension types to create world");
        return c.createWorld();
    }

    int countCustomBiomes();

    void forceBiomeInto(int x, int y, int z, Object somethingVeryDirty, ChunkGenerator.BiomeGrid chunk);

    default boolean supportsDataPacks() {
        return false;
    }

    MCAPaletteAccess createPalette();

    void injectBiomesFromMantle(Chunk e, Mantle mantle);

    ItemStack applyCustomNbt(ItemStack itemStack, KMap<String, Object> customNbt) throws IllegalArgumentException;

    void inject(long seed, Engine engine, World world) throws NoSuchFieldException, IllegalAccessException;

    Vector3d getBoundingbox(org.bukkit.entity.EntityType entity);
    
    Entity spawnEntity(Location location, EntityType type, CreatureSpawnEvent.SpawnReason reason);

    Color getBiomeColor(Location location, BiomeColor type);

    default DataVersion getDataVersion() {
        return DataVersion.V1_19_2;
    }

    default int getSpawnChunkCount(World world) {
        return 441;
    }

    KList<String> getStructureKeys();

    boolean missingDimensionTypes(String... keys);

    default boolean injectBukkit() {
        return true;
    }

    KMap<Material, List<BlockProperty>> getBlockProperties();

    void placeStructures(Chunk chunk);

    KMap<Identifier, StructurePlacement> collectStructures();
}
