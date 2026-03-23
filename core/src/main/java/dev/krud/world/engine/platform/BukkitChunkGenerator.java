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

package dev.krud.world.engine.platform;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.KrudWorldWorlds;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.service.StudioSVC;
import dev.krud.world.engine.KrudWorldEngine;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.data.chunk.TerrainChunk;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineTarget;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.engine.object.KrudWorldWorld;
import dev.krud.world.engine.object.StudioMode;
import dev.krud.world.engine.platform.studio.StudioGenerator;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.KrudWorldBiomeStorage;
import dev.krud.world.util.hunk.view.BiomeGridHunkHolder;
import dev.krud.world.util.hunk.view.ChunkDataHunkHolder;
import dev.krud.world.util.io.ReactiveFolder;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.Looper;
import io.papermc.lib.PaperLib;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@EqualsAndHashCode(callSuper = true)
@Data
public class BukkitChunkGenerator extends ChunkGenerator implements PlatformChunkGenerator, Listener {
    private static final int LOAD_LOCKS = Runtime.getRuntime().availableProcessors() * 4;
    private final Semaphore loadLock;
    private final KrudWorldWorld world;
    private final File dataLocation;
    private final String dimensionKey;
    private final ReactiveFolder folder;
    private final ReentrantLock lock = new ReentrantLock();
    private final KList<BlockPopulator> populators;
    private final ChronoLatch hotloadChecker;
    private final AtomicBoolean setup;
    private final boolean studio;
    private final AtomicInteger a = new AtomicInteger(0);
    private final CompletableFuture<Integer> spawnChunks = new CompletableFuture<>();
    private final AtomicCache<EngineTarget> targetCache = new AtomicCache<>();
    private volatile Engine engine;
    private volatile Looper hotloader;
    private volatile StudioMode lastMode;
    private volatile DummyBiomeProvider dummyBiomeProvider;
    @Setter
    private volatile StudioGenerator studioGenerator;

    public BukkitChunkGenerator(KrudWorldWorld world, boolean studio, File dataLocation, String dimensionKey) {
        setup = new AtomicBoolean(false);
        studioGenerator = null;
        dummyBiomeProvider = new DummyBiomeProvider();
        populators = new KList<>();
        loadLock = new Semaphore(LOAD_LOCKS);
        this.world = world;
        this.hotloadChecker = new ChronoLatch(1000, false);
        this.studio = studio;
        this.dataLocation = dataLocation;
        this.dimensionKey = dimensionKey;
        this.folder = new ReactiveFolder(dataLocation, (_a, _b, _c) -> hotload());
        Bukkit.getServer().getPluginManager().registerEvents(this, KrudWorld.instance);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(WorldInitEvent event) {
        if (!world.name().equals(event.getWorld().getName())) return;
        KrudWorld.instance.unregisterListener(this);
        world.setRawWorldSeed(event.getWorld().getSeed());
        if (initialize(event.getWorld())) return;

        KrudWorld.warn("Failed to get Engine for " + event.getWorld().getName() + " re-trying...");
        J.s(() -> {
            if (!initialize(event.getWorld())) {
                KrudWorld.error("Failed to get Engine for " + event.getWorld().getName() + "!");
            }
        }, 10);
    }

    private boolean initialize(World world) {
        Engine engine = getEngine(world);
        if (engine == null) return false;
        try {
            INMS.get().inject(world.getSeed(), engine, world);
            KrudWorld.info("Injected KrudWorld Biome Source into " + world.getName());
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            KrudWorld.error("Failed to inject biome source into " + world.getName());
            e.printStackTrace();
        }
        spawnChunks.complete(INMS.get().getSpawnChunkCount(world));
        KrudWorld.instance.unregisterListener(this);
        KrudWorldWorlds.get().put(world.getName(), dimensionKey);
        return true;
    }

    @Nullable
    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        Location location = new Location(world, 0, 64, 0);
        PaperLib.getChunkAtAsync(location)
                .thenAccept(c -> {
                    World w = c.getWorld();
                    if (!w.getSpawnLocation().equals(location))
                        return;
                    w.setSpawnLocation(location.add(0, w.getHighestBlockYAt(location) - 64, 0));
                });
        return location;
    }

    private void setupEngine() {
        lastMode = StudioMode.NORMAL;
        engine = new KrudWorldEngine(getTarget(), studio);
        populators.clear();
        targetCache.reset();
    }

    @NotNull
    @Override
    public EngineTarget getTarget() {
        if (engine != null) return engine.getTarget();

        return targetCache.aquire(() -> {
            KrudWorldData data = KrudWorldData.get(dataLocation);
            data.dump();
            data.clearLists();
            KrudWorldDimension dimension = data.getDimensionLoader().load(dimensionKey);

            if (dimension == null) {
                KrudWorld.error("Oh No! There's no pack in " + data.getDataFolder().getPath() + " or... there's no dimension for the key " + dimensionKey);
                KrudWorldDimension test = KrudWorldData.loadAnyDimension(dimensionKey, null);

                if (test != null) {
                    KrudWorld.warn("Looks like " + dimensionKey + " exists in " + test.getLoadFile().getPath() + " ");
                    test = KrudWorld.service(StudioSVC.class).installInto(KrudWorld.getSender(), dimensionKey, dataLocation);
                    KrudWorld.warn("Attempted to install into " + data.getDataFolder().getPath());

                    if (test != null) {
                        KrudWorld.success("Woo! Patched the Engine!");
                        dimension = test;
                    } else {
                        KrudWorld.error("Failed to patch dimension!");
                        throw new RuntimeException("Missing Dimension: " + dimensionKey);
                    }
                } else {
                    KrudWorld.error("Nope, you don't have an installation containing " + dimensionKey + " try downloading it?");
                    throw new RuntimeException("Missing Dimension: " + dimensionKey);
                }
            }

            return new EngineTarget(world, dimension, data);
        });
    }

    @Override
    public void injectChunkReplacement(World world, int x, int z, Executor syncExecutor) {
        try {
            loadLock.acquire();
            KrudWorldBiomeStorage st = new KrudWorldBiomeStorage();
            TerrainChunk tc = TerrainChunk.createUnsafe(world, st);
            this.world.bind(world);
            getEngine().generate(x << 4, z << 4, tc, KrudWorldSettings.get().getGenerator().useMulticore);

            Chunk c = PaperLib.getChunkAtAsync(world, x, z)
                    .thenApply(d -> {
                        KrudWorld.tickets.addTicket(d);

                        for (Entity ee : d.getEntities()) {
                            if (ee instanceof Player) {
                                continue;
                            }

                            ee.remove();
                        }

                        return d;
                    }).get();


            KList<CompletableFuture<?>> futures = new KList<>(1 + getEngine().getHeight() >> 4);
            for (int i = getEngine().getHeight() >> 4; i >= 0; i--) {
                int finalI = i << 4;
                futures.add(CompletableFuture.runAsync(() -> {
                    for (int xx = 0; xx < 16; xx++) {
                        for (int yy = 0; yy < 16; yy++) {
                            for (int zz = 0; zz < 16; zz++) {
                                if (yy + finalI >= engine.getHeight() || yy + finalI < 0) {
                                    continue;
                                }
                                int y = yy + finalI + world.getMinHeight();
                                c.getBlock(xx, y, zz).setBlockData(tc.getBlockData(xx, y, zz), false);
                            }
                        }
                    }
                }, syncExecutor));
            }
            futures.add(CompletableFuture.runAsync(() -> INMS.get().placeStructures(c), syncExecutor));

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRunAsync(() -> {
                        KrudWorld.tickets.removeTicket(c);
                        engine.getWorldManager().onChunkLoad(c, true);
                    }, syncExecutor)
                    .get();
            KrudWorld.debug("Regenerated " + x + " " + z);

            loadLock.release();
        } catch (Throwable e) {
            loadLock.release();
            KrudWorld.error("======================================");
            e.printStackTrace();
            KrudWorld.reportErrorChunk(x, z, e, "CHUNK");
            KrudWorld.error("======================================");

            ChunkData d = Bukkit.createChunkData(world);

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    d.setBlock(i, 0, j, Material.RED_GLAZED_TERRACOTTA.createBlockData());
                }
            }
        }
    }

    private Engine getEngine(WorldInfo world) {
        if (setup.get()) {
            return getEngine();
        }

        lock.lock();

        try {
            if (setup.get()) {
                return getEngine();
            }


            getWorld().setRawWorldSeed(world.getSeed());
            setupEngine();
            setup.set(true);
            this.hotloader = studio ? new Looper() {
                @Override
                protected long loop() {
                    if (hotloadChecker.flip()) {
                        folder.check();
                    }

                    return 250;
                }
            } : null;

            if (studio) {
                hotloader.setPriority(Thread.MIN_PRIORITY);
                hotloader.start();
                hotloader.setName(getTarget().getWorld().name() + " Hotloader");
            }

            return engine;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        withExclusiveControl(() -> {
            if (isStudio()) {
                hotloader.interrupt();
            }

            final Engine engine = getEngine();
            if (engine != null && !engine.isClosed())
                engine.close();
            folder.clear();
            populators.clear();

        });
    }

    @Override
    public boolean isStudio() {
        return studio;
    }

    @Override
    public void hotload() {
        if (!isStudio()) {
            return;
        }

        withExclusiveControl(() -> getEngine().hotload());
    }

    public void withExclusiveControl(Runnable r) {
        J.a(() -> {
            try {
                loadLock.acquire(LOAD_LOCKS);
                r.run();
                loadLock.release(LOAD_LOCKS);
            } catch (Throwable e) {
                KrudWorld.reportError(e);
            }
        });
    }

    @Override
    public void touch(World world) {
        getEngine(world);
    }

    @Override
    public void generateNoise(@NotNull WorldInfo world, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.ChunkData d) {
        try {
            Engine engine = getEngine(world);
            computeStudioGenerator();
            TerrainChunk tc = TerrainChunk.create(d, new KrudWorldBiomeStorage());
            this.world.bind(world);
            if (studioGenerator != null) {
                studioGenerator.generateChunk(engine, tc, x, z);
            } else {
                ChunkDataHunkHolder blocks = new ChunkDataHunkHolder(tc);
                BiomeGridHunkHolder biomes = new BiomeGridHunkHolder(tc, tc.getMinHeight(), tc.getMaxHeight());
                engine.generate(x << 4, z << 4, blocks, biomes, KrudWorldSettings.get().getGenerator().useMulticore);
                blocks.apply();
                biomes.apply();
            }

            KrudWorld.debug("Generated " + x + " " + z);
        } catch (Throwable e) {
            KrudWorld.error("======================================");
            e.printStackTrace();
            KrudWorld.reportErrorChunk(x, z, e, "CHUNK");
            KrudWorld.error("======================================");

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    d.setBlock(i, 0, j, Material.RED_GLAZED_TERRACOTTA.createBlockData());
                }
            }
        }
    }

    @Override
    public int getBaseHeight(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull HeightMap heightMap) {
        return 4;
    }

    private void computeStudioGenerator() {
        if (!getEngine().getDimension().getStudioMode().equals(lastMode)) {
            lastMode = getEngine().getDimension().getStudioMode();
            getEngine().getDimension().getStudioMode().inject(this);
        }
    }

    @NotNull
    @Override
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return populators;
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return dummyBiomeProvider;
    }
}
