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
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.*;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.Cuboid;
import dev.krud.world.util.data.KrudWorldCustomData;
import dev.krud.world.util.math.BlockPosition;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.scheduling.J;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TreeSVC implements KrudWorldService {
    private boolean block = false;

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    /**
     * This function does the following
     * <br>1. Is the sapling growing in an KrudWorld world? No -> exit</br>
     * <br>2. Is the KrudWorld world accessible? No -> exit</br>
     * <br>3. Is the sapling overwriting setting on in that dimension? No -> exit</br>
     * <br>4. Check biome, region and dimension for overrides for that sapling type -> Found -> use</br>
     * <br>5. Exit if none are found, cancel event if one or more are.</br>
     *
     * @param event Checks the given event for sapling overrides
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(StructureGrowEvent event) {
        if (block || event.isCancelled()) {
            return;
        }
        KrudWorld.debug(this.getClass().getName() + " received a structure grow event");

        if (!KrudWorldToolbelt.isKrudWorldWorld(event.getWorld())) {
            KrudWorld.debug(this.getClass().getName() + " passed grow event off to vanilla since not an KrudWorld world");
            return;
        }

        PlatformChunkGenerator worldAccess = KrudWorldToolbelt.access(event.getWorld());
        if (worldAccess == null) {
            KrudWorld.debug(this.getClass().getName() + " passed it off to vanilla because could not get KrudWorldAccess for this world");
            KrudWorld.reportError(new NullPointerException(event.getWorld().getName() + " could not be accessed despite being an KrudWorld world"));
            return;
        }

        Engine engine = worldAccess.getEngine();

        if (engine == null) {
            KrudWorld.debug(this.getClass().getName() + " passed it off to vanilla because could not get Engine for this world");
            KrudWorld.reportError(new NullPointerException(event.getWorld().getName() + " could not be accessed despite being an KrudWorld world"));
            return;
        }

        KrudWorldDimension dimension = engine.getDimension();

        if (dimension == null) {
            KrudWorld.debug(this.getClass().getName() + " passed it off to vanilla because could not get Dimension for this world");
            KrudWorld.reportError(new NullPointerException(event.getWorld().getName() + " could not be accessed despite being an KrudWorld world"));
            return;
        }

        if (!dimension.getTreeSettings().isEnabled()) {
            KrudWorld.debug(this.getClass().getName() + " cancelled because tree overrides are disabled");
            return;
        }

        BlockData first = event.getLocation().getBlock().getBlockData().clone();
        Cuboid saplingPlane = getSaplings(event.getLocation(), blockData -> blockData instanceof Sapling && blockData.getMaterial().equals(first.getMaterial()), event.getWorld());

        KrudWorld.debug("Sapling grew @ " + event.getLocation() + " for " + event.getSpecies().name() + " usedBoneMeal is " + event.isFromBonemeal());
        KrudWorld.debug("Sapling plane is: " + saplingPlane.getSizeX() + " by " + saplingPlane.getSizeZ());
        KrudWorldObjectPlacement placement = getObjectPlacement(worldAccess, event.getLocation(), event.getSpecies(), new KrudWorldTreeSize(1, 1));

        if (placement == null) {
            KrudWorld.debug(this.getClass().getName() + " had options but did not manage to find objectPlacements for them");
            return;
        }

        saplingPlane.forEach(block -> block.setType(Material.AIR));
        KrudWorldObject object = worldAccess.getData().getObjectLoader().load(placement.getPlace().getRandom(RNG.r));
        List<BlockState> blockStateList = new KList<>();
        KMap<Location, BlockData> dataCache = new KMap<>();
        // TODO: REAL CLASSES!!!!

        IObjectPlacer placer = new IObjectPlacer() {

            @Override
            public int getHighest(int x, int z, KrudWorldData data) {
                return event.getWorld().getHighestBlockYAt(x, z);
            }

            @Override
            public int getHighest(int x, int z, KrudWorldData data, boolean ignoreFluid) {
                return event.getWorld().getHighestBlockYAt(x, z, ignoreFluid ? HeightMap.OCEAN_FLOOR : HeightMap.WORLD_SURFACE);
            }

            @Override
            public void set(int x, int y, int z, BlockData d) {
                Block b = event.getWorld().getBlockAt(x, y, z);
                BlockState state = b.getState();
                if (d instanceof KrudWorldCustomData data)
                    state.setBlockData(data.getBase());
                else state.setBlockData(d);
                blockStateList.add(b.getState());
                dataCache.put(new Location(event.getWorld(), x, y, z), d);
            }

            @Override
            public BlockData get(int x, int y, int z) {
                return event.getWorld().getBlockAt(x, y, z).getBlockData();
            }

            @Override
            public boolean isPreventingDecay() {
                return true;
            }

            @Override
            public boolean isCarved(int x, int y, int z) {
                return false;
            }

            @Override
            public boolean isSolid(int x, int y, int z) {
                return get(x, y, z).getMaterial().isSolid();
            }

            @Override
            public boolean isUnderwater(int x, int z) {
                return false;
            }

            @Override
            public int getFluidHeight() {
                return worldAccess.getEngine().getDimension().getFluidHeight();
            }

            @Override
            public boolean isDebugSmartBore() {
                return false;
            }

            @Override
            public void setTile(int xx, int yy, int zz, TileData tile) {

            }

            @Override
            public <T> void setData(int xx, int yy, int zz, T data) {

            }

            @Override
            public <T> T getData(int xx, int yy, int zz, Class<T> t) {
                return null;
            }

            @Override
            public Engine getEngine() {
                return engine;
            }
        };

        object.place(
                saplingPlane.getCenter().getBlockX(),
                (saplingPlane.getCenter().getBlockY() + object.getH() / 2),
                saplingPlane.getCenter().getBlockZ(),
                placer,
                placement,
                RNG.r,
                Objects.requireNonNull(worldAccess).getData()
        );

        event.setCancelled(true);

        J.s(() -> {

            StructureGrowEvent iGrow = new StructureGrowEvent(event.getLocation(), event.getSpecies(), event.isFromBonemeal(), event.getPlayer(), blockStateList);
            block = true;
            Bukkit.getServer().getPluginManager().callEvent(iGrow);
            block = false;

            if (!iGrow.isCancelled()) {
                for (BlockState state : iGrow.getBlocks()) {
                    Location l = state.getLocation();

                    BlockData d = dataCache.get(l);
                    if (d == null) continue;
                    Block block = l.getBlock();

                    if (d instanceof KrudWorldCustomData data) {
                        block.setBlockData(data.getBase(), false);
                        KrudWorld.service(ExternalDataSVC.class).processUpdate(engine, block, data.getCustom());
                    } else block.setBlockData(d, false);
                }
            }
        });
    }

    /**
     * Finds a single object placement (which may contain more than one object) for the requirements species, location &
     * size
     *
     * @param worldAccess The world to access (check for biome, region, dimension, etc)
     * @param location    The location of the growth event (For biome/region finding)
     * @param type        The bukkit TreeType to match
     * @param size        The size of the sapling area
     * @return An object placement which contains the matched tree, or null if none were found / it's disabled.
     */
    private KrudWorldObjectPlacement getObjectPlacement(PlatformChunkGenerator worldAccess, Location location, TreeType type, KrudWorldTreeSize size) {

        KList<KrudWorldObjectPlacement> placements = new KList<>();
        boolean isUseAll = worldAccess.getEngine().getDimension().getTreeSettings().getMode().equals(KrudWorldTreeModes.ALL);

        // Retrieve objectPlacements of type `species` from biome
        KrudWorldBiome biome = worldAccess.getEngine().getBiome(location.getBlockX(), location.getBlockY()-worldAccess.getTarget().getWorld().minHeight(), location.getBlockZ());
        placements.addAll(matchObjectPlacements(biome.getObjects(), size, type));

        // Add more or find any in the region
        if (isUseAll || placements.isEmpty()) {
            KrudWorldRegion region = worldAccess.getEngine().getRegion(location.getBlockX(), location.getBlockZ());
            placements.addAll(matchObjectPlacements(region.getObjects(), size, type));
        }

        // Check if no matches were found, return a random one if they are
        return placements.isNotEmpty() ? placements.getRandom() : null;
    }

    /**
     * Filters out mismatches and returns matches
     *
     * @param objects The object placements to check
     * @param size    The size of the sapling area to filter with
     * @param type    The type of the tree to filter with
     * @return A list of objectPlacements that matched. May be empty.
     */
    private KList<KrudWorldObjectPlacement> matchObjectPlacements(KList<KrudWorldObjectPlacement> objects, KrudWorldTreeSize size, TreeType type) {

        KList<KrudWorldObjectPlacement> p = new KList<>();

        for (KrudWorldObjectPlacement i : objects) {
            if (i.matches(size, type)) {
                p.add(i);
            }
        }

        return p;
    }

    /**
     * Get the Cuboid of sapling sizes at a location & blockData predicate
     *
     * @param at    this location
     * @param valid with this blockData predicate
     * @param world the world to check in
     * @return A cuboid containing only saplings
     */
    public Cuboid getSaplings(Location at, Predicate<BlockData> valid, World world) {
        KList<BlockPosition> blockPositions = new KList<>();
        grow(at.getWorld(), new BlockPosition(at.getBlockX(), at.getBlockY(), at.getBlockZ()), valid, blockPositions);
        BlockPosition a = new BlockPosition(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        BlockPosition b = new BlockPosition(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        // Maximise the block position in x and z to get max cuboid bounds
        for (BlockPosition blockPosition : blockPositions) {
            a.max(blockPosition);
            b.min(blockPosition);
        }

        KrudWorld.debug("Blocks: " + blockPositions.size());
        KrudWorld.debug("Min: " + a + " Max: " + b);

        // Create a cuboid with the size calculated before
        Cuboid cuboid = new Cuboid(a.toBlock(world).getLocation(), b.toBlock(world).getLocation());
        boolean cuboidIsValid = true;

        // Loop while the cuboid is larger than 2
        while (Math.min(cuboid.getSizeX(), cuboid.getSizeZ()) > 0) {
            checking:
            for (int i = cuboid.getLowerX(); i < cuboid.getUpperX(); i++) {
                for (int j = cuboid.getLowerY(); j < cuboid.getUpperY(); j++) {
                    for (int k = cuboid.getLowerZ(); k < cuboid.getUpperZ(); k++) {
                        if (!blockPositions.contains(new BlockPosition(i, j, k))) {
                            cuboidIsValid = false;
                            break checking;
                        }
                    }
                }
            }

            // Return this cuboid if it's valid
            if (cuboidIsValid) {
                return cuboid;
            }

            // Inset the cuboid and try again (revalidate)
            cuboid = cuboid.inset(Cuboid.CuboidDirection.Horizontal, 1);
            cuboidIsValid = true;
        }

        return new Cuboid(at, at);
    }

    /**
     * Grows the blockPosition list by means of checking neighbours in
     *
     * @param world  the world to check in
     * @param center the location of this position
     * @param valid  validation on blockData to check block with
     * @param l      list of block positions to add new neighbors too
     */
    private void grow(World world, BlockPosition center, Predicate<BlockData> valid, KList<BlockPosition> l) {
        // Make sure size is less than 50, the block to check isn't already in, and make sure the blockData still matches
        if (l.size() <= 50 && !l.contains(center) && valid.test(center.toBlock(world).getBlockData())) {
            l.add(center);
            grow(world, center.add(1, 0, 0), valid, l);
            grow(world, center.add(-1, 0, 0), valid, l);
            grow(world, center.add(0, 0, 1), valid, l);
            grow(world, center.add(0, 0, -1), valid, l);
        }
    }
}
