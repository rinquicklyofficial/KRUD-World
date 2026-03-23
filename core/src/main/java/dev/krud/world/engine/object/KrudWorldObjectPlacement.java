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
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.B;
import dev.krud.world.util.data.DataProvider;
import dev.krud.world.util.data.WeightedRandom;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.TreeType;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@Snippet("object-placer")
@EqualsAndHashCode()
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents an iris object placer. It places objects.")
@Data
public class KrudWorldObjectPlacement {
    private final transient AtomicCache<CNG> surfaceWarp = new AtomicCache<>();
    @RegistryListResource(KrudWorldObject.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("List of objects to place")
    private KList<String> place = new KList<>();
    @Desc("Rotate this objects placement")
    private KrudWorldObjectRotation rotation = new KrudWorldObjectRotation();
    @Desc("Limit the max height or min height of placement.")
    private KrudWorldObjectLimit clamp = new KrudWorldObjectLimit();
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The maximum layer level of a snow filter overtop of this placement. Set to 0 to disable. Max of 1.")
    private double snow = 0;
    @Desc("Whether or not this object can be targeted by a dolphin.")
    private boolean isDolphinTarget = false;
    @Desc("The slope at which this object can be placed. Range from 0 to 10 by default. Calculated from a 3-block radius from the center of the object placement.")
    private KrudWorldSlopeClip slopeCondition = new KrudWorldSlopeClip();
    @Desc("Set to true to add the rotation of the direction of the slope of the terrain (wherever the slope is going down) to the y-axis rotation of the object." +
            "Rounded to 90 degrees. Adds the *min* rotation of the y axis as well (to still allow you to rotate objects nicely). Discards *max* and *interval* on *yaxis*")
    private boolean rotateTowardsSlope = false;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The chance for this to place in a chunk. If you need multiple per chunk, set this to 1 and use density.")
    private double chance = 1;
    @MinNumber(1)
    @Desc("If the chance check passes, place this many in a single chunk")
    private int density = 1;
    @Desc("If the chance check passes, and you specify this, it picks a number in the range based on noise, and 'density' is ignored.")
    private KrudWorldStyledRange densityStyle = null;
    @Desc("When stilting is enabled, this object will define various properties related to it.")
    private KrudWorldStiltSettings stiltSettings;
    @MaxNumber(64)
    @MinNumber(0)
    @Desc("When bore is enabled, expand max-y of the cuboid it removes")
    private int boreExtendMaxY = 0;
    @ArrayType(min = 1, type = KrudWorldObjectMarker.class)
    @Desc("Add markers to blocks in this object")
    private KList<KrudWorldObjectMarker> markers = new KList<>();
    @MaxNumber(64)
    @MinNumber(-1)
    @Desc("When bore is enabled, lower min-y of the cuboid it removes")
    private int boreExtendMinY = 0;
    @Desc("If set to true, objects will place on the terrain height, ignoring the water surface.")
    private boolean underwater = false;
    @Desc("If set to true, objects will place in carvings (such as underground) or under an overhang.")
    private CarvingMode carvingSupport = CarvingMode.SURFACE_ONLY;
    @Desc("If this is defined, this object wont place on the terrain heightmap, but instead on this virtual heightmap")
    private KrudWorldNoiseGenerator heightmap;
    @Desc("If set to true, KrudWorld will try to fill the insides of 'rooms' and 'pockets' where air should fit based off of raytrace checks. This prevents a village house placing in an area where a tree already exists, and instead replaces the parts of the tree where the interior of the structure is. \n\nThis operation does not affect warmed-up generation speed however it does slow down loading objects.")
    private boolean smartBore = false;
    @Desc("If set to true, Blocks placed underwater that could be waterlogged are waterlogged.")
    private boolean waterloggable = false;
    @Desc("If set to true, objects will place on the fluid height level Such as boats.")
    private boolean onwater = false;
    @Desc("If set to true, this object will only place parts of itself where blocks already exist. Warning: Melding is very performance intensive!")
    private boolean meld = false;
    @Desc("If set to true, this object will get placed from the bottom of the world up")
    private boolean fromBottom;
    @Desc("If set to true, this object will place from the ground up instead of height checks when not y locked to the surface. This is not compatable with X and Z axis rotations (it may look off)")
    private boolean bottom = false;
    @Desc("If set to true, air will be placed before the schematic places.")
    private boolean bore = false;
    @Desc("Use a generator to warp the field of coordinates. Using simplex for example would make a square placement warp like a flag")
    private KrudWorldGeneratorStyle warp = new KrudWorldGeneratorStyle(NoiseStyle.FLAT);
    @Desc("If the place mode is set to CENTER_HEIGHT_RIGID and you have an X/Z translation, Turning on translate center will also translate the center height check.")
    private boolean translateCenter = false;
    @Desc("The placement mode")
    private ObjectPlaceMode mode = ObjectPlaceMode.CENTER_HEIGHT;
    @ArrayType(min = 1, type = KrudWorldObjectReplace.class)
    @Desc("Find and replace blocks")
    private KList<KrudWorldObjectReplace> edit = new KList<>();
    @Desc("Translate this object's placement")
    private KrudWorldObjectTranslate translate = new KrudWorldObjectTranslate();
    @Desc("Scale Objects")
    private KrudWorldObjectScale scale = new KrudWorldObjectScale();
    @ArrayType(min = 1, type = KrudWorldObjectLoot.class)
    @Desc("The loot tables to apply to these objects")
    private KList<KrudWorldObjectLoot> loot = new KList<>();
    @ArrayType(min = 1, type = KrudWorldObjectVanillaLoot.class)
    @Desc("The vanilla loot tables to apply to these objects")
    private KList<KrudWorldObjectVanillaLoot> vanillaLoot = new KList<>();
    @Desc("Whether the given loot tables override any and all other loot tables available in the dimension, region or biome.")
    private boolean overrideGlobalLoot = false;
    @Desc("This object / these objects override the following trees when they grow...")
    @ArrayType(min = 1, type = KrudWorldTree.class)
    private KList<KrudWorldTree> trees = new KList<>();
    @RegistryListResource(KrudWorldObject.class)
    @ArrayType(type = String.class)
    @Desc("List of objects to this object is allowed to collied with")
    private KList<String> allowedCollisions = new KList<>();
    @RegistryListResource(KrudWorldObject.class)
    @ArrayType(type = String.class)
    @Desc("List of objects to this object is forbidden to collied with")
    private KList<String> forbiddenCollisions = new KList<>();
    @Desc("Ignore any placement restrictions for this object")
    private boolean forcePlace = false;
    private transient AtomicCache<TableCache> cache = new AtomicCache<>();

    public KrudWorldObjectPlacement toPlacement(String... place) {
        KrudWorldObjectPlacement p = new KrudWorldObjectPlacement();
        p.setPlace(new KList<>(place));
        p.setTranslateCenter(translateCenter);
        p.setMode(mode);
        p.setEdit(edit);
        p.setTranslate(translate);
        p.setWarp(warp);
        p.setBore(bore);
        p.setMeld(meld);
        p.setWaterloggable(waterloggable);
        p.setOnwater(onwater);
        p.setSmartBore(smartBore);
        p.setCarvingSupport(carvingSupport);
        p.setUnderwater(underwater);
        p.setBoreExtendMaxY(boreExtendMaxY);
        p.setBoreExtendMinY(boreExtendMinY);
        p.setStiltSettings(stiltSettings);
        p.setDensity(density);
        p.setChance(chance);
        p.setSnow(snow);
        p.setClamp(clamp);
        p.setRotation(rotation);
        p.setLoot(loot);
        return p;
    }

    public CNG getSurfaceWarp(RNG rng, KrudWorldData data) {
        return surfaceWarp.aquire(() ->
                getWarp().create(rng, data));
    }

    public double warp(RNG rng, double x, double y, double z, KrudWorldData data) {
        return getSurfaceWarp(rng, data).fitDouble(-(getWarp().getMultiplier() / 2D), (getWarp().getMultiplier() / 2D), x, y, z);
    }

    public KrudWorldObject getObject(DataProvider g, RNG random) {
        if (place.isEmpty()) {
            return null;
        }

        return g.getData().getObjectLoader().load(place.get(random.nextInt(place.size())));
    }

    public boolean matches(KrudWorldTreeSize size, TreeType type) {
        for (KrudWorldTree i : getTrees()) {
            if (i.matches(size, type)) {
                return true;
            }
        }

        return false;
    }

    public int getDensity() {
        if (densityStyle == null) {
            return density;
        }
        return densityStyle.getMid();
    }

    public int getDensity(RNG rng, double x, double z, KrudWorldData data) {
        if (densityStyle == null) {
            return density;
        }

        return (int) Math.round(densityStyle.get(rng, x, z, data));
    }

    private TableCache getCache(KrudWorldData manager) {
        return cache.aquire(() -> {
            TableCache cache = new TableCache();

            cache.merge(getCache(manager, getVanillaLoot(), KrudWorldObjectPlacement::getVanillaTable));
            cache.merge(getCache(manager, getLoot(), manager.getLootLoader()::load));

            return cache;
        });
    }

    private TableCache getCache(KrudWorldData manager, KList<? extends IObjectLoot> list, Function<String, KrudWorldLootTable> loader) {
        TableCache tc = new TableCache();

        for (IObjectLoot loot : list) {
            if (loot == null)
                continue;
            KrudWorldLootTable table = loader.apply(loot.getName());
            if (table == null) {
                KrudWorld.warn("Couldn't find loot table " + loot.getName());
                continue;
            }

            if (loot.getFilter().isEmpty()) //Table applies to all containers
            {
                tc.global.put(table, loot.getWeight());
            } else if (!loot.isExact()) //Table is meant to be by type
            {
                for (BlockData filterData : loot.getFilter(manager)) {
                    if (!tc.basic.containsKey(filterData.getMaterial())) {
                        tc.basic.put(filterData.getMaterial(), new WeightedRandom<>());
                    }

                    tc.basic.get(filterData.getMaterial()).put(table, loot.getWeight());
                }
            } else //Filter is exact
            {
                for (BlockData filterData : loot.getFilter(manager)) {
                    if (!tc.exact.containsKey(filterData.getMaterial())) {
                        tc.exact.put(filterData.getMaterial(), new KMap<>());
                    }

                    if (!tc.exact.get(filterData.getMaterial()).containsKey(filterData)) {
                        tc.exact.get(filterData.getMaterial()).put(filterData, new WeightedRandom<>());
                    }

                    tc.exact.get(filterData.getMaterial()).get(filterData).put(table, loot.getWeight());
                }
            }
        }
        return tc;
    }

    @Nullable
    private static KrudWorldVanillaLootTable getVanillaTable(String name) {
        return Optional.ofNullable(NamespacedKey.fromString(name))
                .map(Bukkit::getLootTable)
                .map(KrudWorldVanillaLootTable::new)
                .orElse(null);
    }

    /**
     * Gets the loot table that should be used for the block
     *
     * @param data        The block data of the block
     * @param dataManager KrudWorld Data Manager
     * @return The loot table it should use.
     */
    public KrudWorldLootTable getTable(BlockData data, KrudWorldData dataManager) {
        TableCache cache = getCache(dataManager);
        if (B.isStorageChest(data)) {
            KrudWorldLootTable picked = null;
            if (cache.exact.containsKey(data.getMaterial()) && cache.exact.get(data.getMaterial()).containsKey(data)) {
                picked = cache.exact.get(data.getMaterial()).get(data).pullRandom();
            } else if (cache.basic.containsKey(data.getMaterial())) {
                picked = cache.basic.get(data.getMaterial()).pullRandom();
            } else if (cache.global.getSize() > 0) {
                picked = cache.global.pullRandom();
            }

            return picked;
        }

        return null;
    }

    private static class TableCache {
        final transient WeightedRandom<KrudWorldLootTable> global = new WeightedRandom<>();
        final transient KMap<Material, WeightedRandom<KrudWorldLootTable>> basic = new KMap<>();
        final transient KMap<Material, KMap<BlockData, WeightedRandom<KrudWorldLootTable>>> exact = new KMap<>();

        private void merge(TableCache other) {
            global.merge(other.global);
            basic.merge(other.basic, WeightedRandom::merge);
            exact.merge(other.exact, (a, b) -> a.merge(b, WeightedRandom::merge));
        }
    }
}
