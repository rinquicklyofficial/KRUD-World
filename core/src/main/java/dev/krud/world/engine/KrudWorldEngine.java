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

package dev.krud.world.engine;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.ServerConfigurator;
import dev.krud.world.core.events.KrudWorldEngineHotloadEvent;
import dev.krud.world.core.gui.PregeneratorJob;
import dev.krud.world.core.loader.ResourceLoader;
import dev.krud.world.core.nms.container.BlockPos;
import dev.krud.world.core.nms.container.Pair;
import dev.krud.world.core.project.KrudWorldProject;
import dev.krud.world.core.scripting.environment.EngineEnvironment;
import dev.krud.world.core.service.PreservationSVC;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.framework.*;
import dev.krud.world.engine.mantle.EngineMantle;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.atomics.AtomicRollingSequence;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.context.KrudWorldContext;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterStructurePOI;
import dev.krud.world.util.matter.slices.container.JigsawStructureContainer;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Data
@EqualsAndHashCode(exclude = "context")
@ToString(exclude = "context")
public class KrudWorldEngine implements Engine {
    private final AtomicInteger bud;
    private final AtomicInteger buds;
    private final AtomicInteger generated;
    private final AtomicInteger generatedLast;
    private final AtomicDouble perSecond;
    private final AtomicLong lastGPS;
    private final EngineTarget target;
    private final KrudWorldContext context;
    private final EngineMantle mantle;
    private final ChronoLatch perSecondLatch;
    private final ChronoLatch perSecondBudLatch;
    private final EngineMetrics metrics;
    private final boolean studio;
    private final AtomicRollingSequence wallClock;
    private final int art;
    private final AtomicCache<KrudWorldEngineData> engineData = new AtomicCache<>();
    private final AtomicBoolean cleaning;
    private final ChronoLatch cleanLatch;
    private final SeedManager seedManager;
    private CompletableFuture<Long> hash32;
    private EngineMode mode;
    private EngineEffects effects;
    private EngineEnvironment execution;
    private EngineWorldManager worldManager;
    private volatile int parallelism;
    private boolean failing;
    private boolean closed;
    private int cacheId;
    private double maxBiomeObjectDensity;
    private double maxBiomeLayerDensity;
    private double maxBiomeDecoratorDensity;
    private KrudWorldComplex complex;

    public KrudWorldEngine(EngineTarget target, boolean studio) {
        this.studio = studio;
        this.target = target;
        getEngineData();
        verifySeed();
        this.seedManager = new SeedManager(target.getWorld().getRawWorldSeed());
        bud = new AtomicInteger(0);
        buds = new AtomicInteger(0);
        metrics = new EngineMetrics(32);
        cleanLatch = new ChronoLatch(10000);
        generatedLast = new AtomicInteger(0);
        perSecond = new AtomicDouble(0);
        perSecondLatch = new ChronoLatch(1000, false);
        perSecondBudLatch = new ChronoLatch(1000, false);
        wallClock = new AtomicRollingSequence(32);
        lastGPS = new AtomicLong(M.ms());
        generated = new AtomicInteger(0);
        mantle = new KrudWorldEngineMantle(this);
        context = new KrudWorldContext(this);
        cleaning = new AtomicBoolean(false);
        execution = getData().getEnvironment().with(this);
        if (studio) {
            getData().dump();
            getData().clearLists();
            getTarget().setDimension(getData().getDimensionLoader().load(getDimension().getLoadKey()));
        }
        context.touch();
        getData().setEngine(this);
        getData().loadPrefetch(this);
        KrudWorld.info("Initializing Engine: " + target.getWorld().name() + "/" + target.getDimension().getLoadKey() + " (" + target.getDimension().getDimensionHeight() + " height) Seed: " + getSeedManager().getSeed());
        failing = false;
        closed = false;
        art = J.ar(this::tickRandomPlayer, 0);
        setupEngine();
        KrudWorld.debug("Engine Initialized " + getCacheID());
    }

    private void verifySeed() {
        if (getEngineData().getSeed() != null && getEngineData().getSeed() != target.getWorld().getRawWorldSeed()) {
            target.getWorld().setRawWorldSeed(getEngineData().getSeed());
        }
    }

    private void tickRandomPlayer() {
        recycle();
        if (perSecondBudLatch.flip()) {
            buds.set(bud.get());
            bud.set(0);
        }

        if (effects != null) {
            effects.tickRandomPlayer();
        }
    }

    private void prehotload() {
        worldManager.close();
        complex.close();
        effects.close();
        mode.close();
        execution = getData().getEnvironment().with(this);

        J.a(() -> new KrudWorldProject(getData().getDataFolder()).updateWorkspace());
    }

    private void setupEngine() {
        try {
            KrudWorld.debug("Setup Engine " + getCacheID());
            cacheId = RNG.r.nextInt();
            worldManager = new KrudWorldWorldManager(this);
            complex = new KrudWorldComplex(this);
            effects = new KrudWorldEngineEffects(this);
            hash32 = new CompletableFuture<>();
            mantle.hotload();
            setupMode();
            getDimension().getEngineScripts().forEach(execution::execute);
            J.a(this::computeBiomeMaxes);
            J.a(() -> {
                File[] roots = getData().getLoaders()
                        .values()
                        .stream()
                        .map(ResourceLoader::getFolderName)
                        .map(n -> new File(getData().getDataFolder(), n))
                        .filter(File::exists)
                        .filter(File::isDirectory)
                        .toArray(File[]::new);
                hash32.complete(IO.hashRecursive(roots));
            });
        } catch (Throwable e) {
            KrudWorld.error("FAILED TO SETUP ENGINE!");
            e.printStackTrace();
        }

        KrudWorld.debug("Engine Setup Complete " + getCacheID());
    }

    private void setupMode() {
        if (mode != null) {
            mode.close();
        }

        mode = getDimension().getMode().create(this);
    }

    @Override
    public void generateMatter(int x, int z, boolean multicore, ChunkContext context) {
        getMantle().generateMatter(x, z, multicore, context);
    }

    @Override
    public Set<String> getObjectsAt(int x, int z) {
        return getMantle().getObjectComponent().guess(x, z);
    }

    @Override
    public Set<Pair<String, BlockPos>> getPOIsAt(int chunkX, int chunkY) {
        Set<Pair<String, BlockPos>> pois = new HashSet<>();
        getMantle().getMantle().iterateChunk(chunkX, chunkY, MatterStructurePOI.class, (x, y, z, d) -> pois.add(new Pair<>(d.getType(), new BlockPos(x, y, z))));
        return pois;
    }

    @Override
    public KrudWorldJigsawStructure getStructureAt(int x, int z) {
        return getMantle().getJigsawComponent().guess(x, z);
    }

    @Override
    public KrudWorldJigsawStructure getStructureAt(int x, int y, int z) {
        var container = getMantle().getMantle().get(x, y, z, JigsawStructureContainer.class);
        return container == null ? null : container.load(getData());
    }

    private void warmupChunk(int x, int z) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int xx = x + (i << 4);
                int zz = z + (z << 4);
                getComplex().getTrueBiomeStream().get(xx, zz);
                getComplex().getHeightStream().get(xx, zz);
            }
        }
    }

    @Override
    public void hotload() {
        hotloadSilently();
        KrudWorld.callEvent(new KrudWorldEngineHotloadEvent(this));
    }

    public void hotloadComplex() {
        complex.close();
        complex = new KrudWorldComplex(this);
    }

    public void hotloadSilently() {
        getData().dump();
        getData().clearLists();
        getTarget().setDimension(getData().getDimensionLoader().load(getDimension().getLoadKey()));
        prehotload();
        setupEngine();
        J.a(() -> {
            synchronized (ServerConfigurator.class) {
                ServerConfigurator.installDataPacks(false);
            }
        });
    }

    @Override
    public KrudWorldEngineData getEngineData() {
        return engineData.aquire(() -> {
            //TODO: Method this file
            File f = new File(getWorld().worldFolder(), "iris/engine-data/" + getDimension().getLoadKey() + ".json");
            KrudWorldEngineData data = null;

            if (f.exists()) {
                try {
                    data = new Gson().fromJson(IO.readAll(f), KrudWorldEngineData.class);
                    if (data == null) {
                        KrudWorld.error("Failed to read Engine Data! Corrupted File? recreating...");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (data == null) {
                data = new KrudWorldEngineData();
                data.getStatistics().setVersion(KrudWorld.instance.getKrudWorldVersion());
                data.getStatistics().setMCVersion(KrudWorld.instance.getMCVersion());
                data.getStatistics().setUpgradedVersion(KrudWorld.instance.getKrudWorldVersion());
                if (data.getStatistics().getVersion() == -1 || data.getStatistics().getMCVersion() == -1 ) {
                    KrudWorld.error("Failed to setup Engine Data!");
                }

                if (f.getParentFile().exists() || f.getParentFile().mkdirs()) {
                    try {
                        IO.writeAll(f, new Gson().toJson(data));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    KrudWorld.error("Failed to setup Engine Data!");
                }
            }

            return data;
        });
    }

    @Override
    public int getGenerated() {
        return generated.get();
    }

    @Override
    public double getGeneratedPerSecond() {
        if (perSecondLatch.flip()) {
            double g = generated.get() - generatedLast.get();
            generatedLast.set(generated.get());

            if (g == 0) {
                return 0;
            }

            long dur = M.ms() - lastGPS.get();
            lastGPS.set(M.ms());
            perSecond.set(g / ((double) (dur) / 1000D));
        }

        return perSecond.get();
    }

    @Override
    public boolean isStudio() {
        return studio;
    }

    private void computeBiomeMaxes() {
        for (KrudWorldBiome i : getDimension().getAllBiomes(this)) {
            double density = 0;

            for (KrudWorldObjectPlacement j : i.getObjects()) {
                density += j.getDensity() * j.getChance();
            }

            maxBiomeObjectDensity = Math.max(maxBiomeObjectDensity, density);
            density = 0;

            for (KrudWorldDecorator j : i.getDecorators()) {
                density += Math.max(j.getStackMax(), 1) * j.getChance();
            }

            maxBiomeDecoratorDensity = Math.max(maxBiomeDecoratorDensity, density);
            density = 0;

            for (KrudWorldBiomePaletteLayer j : i.getLayers()) {
                density++;
            }

            maxBiomeLayerDensity = Math.max(maxBiomeLayerDensity, density);
        }
    }

    @Override
    public int getBlockUpdatesPerSecond() {
        return buds.get();
    }

    public void printMetrics(CommandSender sender) {
        KMap<String, Double> totals = new KMap<>();
        KMap<String, Double> weights = new KMap<>();
        double masterWallClock = wallClock.getAverage();
        KMap<String, Double> timings = getMetrics().pull();
        double totalWeight = 0;
        double wallClock = getMetrics().getTotal().getAverage();

        for (double j : timings.values()) {
            totalWeight += j;
        }

        for (String j : timings.k()) {
            weights.put(getName() + "." + j, (wallClock / totalWeight) * timings.get(j));
        }

        totals.put(getName(), wallClock);

        double mtotals = 0;

        for (double i : totals.values()) {
            mtotals += i;
        }

        for (String i : totals.k()) {
            totals.put(i, (masterWallClock / mtotals) * totals.get(i));
        }

        double v = 0;

        for (double i : weights.values()) {
            v += i;
        }

        for (String i : weights.k()) {
            weights.put(i, weights.get(i) / v);
        }

        sender.sendMessage("Total: " + C.BOLD + C.WHITE + Form.duration(masterWallClock, 0));

        for (String i : totals.k()) {
            sender.sendMessage("  Engine " + C.UNDERLINE + C.GREEN + i + C.RESET + ": " + C.BOLD + C.WHITE + Form.duration(totals.get(i), 0));
        }

        sender.sendMessage("Details: ");

        for (String i : weights.sortKNumber().reverse()) {
            String befb = C.UNDERLINE + "" + C.GREEN + "" + i.split("\\Q[\\E")[0] + C.RESET + C.GRAY + "[";
            String num = C.GOLD + i.split("\\Q[\\E")[1].split("]")[0] + C.RESET + C.GRAY + "].";
            String afb = C.ITALIC + "" + C.AQUA + i.split("\\Q]\\E")[1].substring(1) + C.RESET + C.GRAY;

            sender.sendMessage("  " + befb + num + afb + ": " + C.BOLD + C.WHITE + Form.pc(weights.get(i), 0));
        }
    }

    @Override
    public void close() {
        PregeneratorJob.shutdownInstance();
        closed = true;
        J.car(art);
        getWorldManager().close();
        getTarget().close();
        saveEngineData();
        getMantle().close();
        getComplex().close();
        mode.close();
        getData().dump();
        getData().clearLists();
        KrudWorld.service(PreservationSVC.class).dereference();
        KrudWorld.debug("Engine Fully Shutdown!");
        complex = null;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void recycle() {
        if (!cleanLatch.flip()) {
            return;
        }

        if (cleaning.get()) {
            cleanLatch.flipDown();
            return;
        }

        cleaning.set(true);

        J.a(() -> {
            try {
                getData().getObjectLoader().clean();
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                KrudWorld.error("Cleanup failed! Enable debug to see stacktrace.");
            }

            cleaning.lazySet(false);
        });
    }

    @BlockCoordinates
    @Override
    public void generate(int x, int z, Hunk<BlockData> vblocks, Hunk<Biome> vbiomes, boolean multicore) throws WrongEngineBroException {
        if (closed) {
            throw new WrongEngineBroException();
        }

        context.touch();
        getEngineData().getStatistics().generatedChunk();
        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            Hunk<BlockData> blocks = vblocks.listen((xx, y, zz, t) -> catchBlockUpdates(x + xx, y, z + zz, t));

            if (getDimension().isDebugChunkCrossSections() && ((x >> 4) % getDimension().getDebugCrossSectionsMod() == 0 || (z >> 4) % getDimension().getDebugCrossSectionsMod() == 0)) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        blocks.set(i, 0, j, Material.CRYING_OBSIDIAN.createBlockData());
                    }
                }
            } else {
                mode.generate(x, z, blocks, vbiomes, multicore);
            }

            getMantle().getMantle().flag(x >> 4, z >> 4, MantleFlag.REAL, true);
            getMetrics().getTotal().put(p.getMilliseconds());
            generated.incrementAndGet();

            if (generated.get() == 661) {
                J.a(() -> getData().savePrefetch(this));
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            fail("Failed to generate " + x + ", " + z, e);
        }
    }

    @Override
    public void saveEngineData() {
        //TODO: Method this file
        File f = new File(getWorld().worldFolder(), "iris/engine-data/" + getDimension().getLoadKey() + ".json");
        f.getParentFile().mkdirs();
        try {
            IO.writeAll(f, new Gson().toJson(getEngineData()));
            KrudWorld.debug("Saved Engine Data");
        } catch (IOException e) {
            KrudWorld.error("Failed to save Engine Data");
            e.printStackTrace();
        }
    }

    @Override
    public void blockUpdatedMetric() {
        bud.incrementAndGet();
    }

    @Override
    public KrudWorldBiome getFocus() {
        if (getDimension().getFocus() == null || getDimension().getFocus().trim().isEmpty()) {
            return null;
        }

        return getData().getBiomeLoader().load(getDimension().getFocus());
    }

    @Override
    public KrudWorldRegion getFocusRegion() {
        if (getDimension().getFocusRegion() == null || getDimension().getFocusRegion().trim().isEmpty()) {
            return null;
        }

        return getData().getRegionLoader().load(getDimension().getFocusRegion());
    }

    @Override
    public void fail(String error, Throwable e) {
        failing = true;
        KrudWorld.error(error);
        e.printStackTrace();
    }

    @Override
    public boolean hasFailed() {
        return failing;
    }

    @Override
    public int getCacheID() {
        return cacheId;
    }

    private boolean EngineSafe() {
        // Todo: this has potential if done right
        int EngineMCVersion = getEngineData().getStatistics().getMCVersion();
        int EngineKrudWorldVersion = getEngineData().getStatistics().getVersion();
        int MinecraftVersion = KrudWorld.instance.getMCVersion();
        int KrudWorldVersion = KrudWorld.instance.getKrudWorldVersion();
        if (EngineKrudWorldVersion != KrudWorldVersion) {
            return false;
        }
        if (EngineMCVersion != MinecraftVersion) {
            return false;
        }
        return true;
    }
}
