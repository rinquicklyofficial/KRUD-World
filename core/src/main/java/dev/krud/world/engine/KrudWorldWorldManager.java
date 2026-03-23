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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.service.ExternalDataSVC;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedWorldManager;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.Position2;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterMarker;
import dev.krud.world.util.parallel.MultiBurst;
import dev.krud.world.util.plugin.Chunks;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.Looper;
import dev.krud.world.util.scheduling.jobs.QueueJob;
import io.papermc.lib.PaperLib;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@Data
public class KrudWorldWorldManager extends EngineAssignedWorldManager {
    private final Looper looper;
    private final int id;
    private final KList<Runnable> updateQueue = new KList<>();
    private final ChronoLatch cl;
    private final ChronoLatch clw;
    private final ChronoLatch ecl;
    private final ChronoLatch cln;
    private final ChronoLatch chunkUpdater;
    private final ChronoLatch chunkDiscovery;
    private final KMap<Long, Future<?>> cleanup = new KMap<>();
    private final ScheduledExecutorService cleanupService;
    private double energy = 25;
    private int entityCount = 0;
    private long charge = 0;
    private int actuallySpawned = 0;
    private int cooldown = 0;
    private List<Entity> precount = new KList<>();
    private KSet<Position2> injectBiomes = new KSet<>();

    public KrudWorldWorldManager() {
        super(null);
        cl = null;
        ecl = null;
        cln = null;
        clw = null;
        looper = null;
        chunkUpdater = null;
        chunkDiscovery = null;
        cleanupService = null;
        id = -1;
    }

    public KrudWorldWorldManager(Engine engine) {
        super(engine);
        chunkUpdater = new ChronoLatch(3000);
        chunkDiscovery = new ChronoLatch(5000);
        cln = new ChronoLatch(60000);
        cl = new ChronoLatch(3000);
        ecl = new ChronoLatch(250);
        clw = new ChronoLatch(1000, true);
        cleanupService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            var thread = new Thread(runnable, "KrudWorld Mantle Cleanup " + getTarget().getWorld().name());
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        id = engine.getCacheID();
        energy = 25;
        looper = new Looper() {
            @Override
            protected long loop() {
                if (getEngine().isClosed() || getEngine().getCacheID() != id) {
                    interrupt();
                }

                if (!getEngine().getWorld().hasRealWorld() && clw.flip()) {
                    getEngine().getWorld().tryGetRealWorld();
                }

                if (getEngine().getWorld().hasRealWorld()) {
                    if (getEngine().getWorld().getPlayers().isEmpty()) {
                        return 5000;
                    }

                    if (chunkUpdater.flip()) {
                        updateChunks();
                    }

                    if (chunkDiscovery.flip()) {
                        discoverChunks();
                    }

                    if (cln.flip()) {
                        engine.getEngineData().cleanup(getEngine());
                    }

                    if (!KrudWorldSettings.get().getWorld().isMarkerEntitySpawningSystem() && !KrudWorldSettings.get().getWorld().isAnbientEntitySpawningSystem()) {
                        return 3000;
                    }

                    if (getDimension().isInfiniteEnergy()) {
                        energy += 1000;
                        fixEnergy();
                    }

                    if (M.ms() < charge) {
                        energy += 70;
                        fixEnergy();
                    }

                    if (precount != null) {
                        entityCount = 0;
                        for (Entity i : precount) {
                            if (i instanceof LivingEntity) {
                                if (!i.isDead()) {
                                    entityCount++;
                                }
                            }
                        }

                        precount = null;
                    }

                    if (energy < 650) {
                        if (ecl.flip()) {
                            energy *= 1 + (0.02 * M.clip((1D - getEntitySaturation()), 0D, 1D));
                            fixEnergy();
                        }
                    }

                    onAsyncTick();
                }

                return KrudWorldSettings.get().getWorld().getAsyncTickIntervalMS();
            }
        };
        looper.setPriority(Thread.MIN_PRIORITY);
        looper.setName("KrudWorld World Manager " + getTarget().getWorld().name());
        looper.start();
    }

    private void discoverChunks() {
        var mantle = getEngine().getMantle().getMantle();
        for (Player i : getEngine().getWorld().realWorld().getPlayers()) {
            int r = 1;

            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    mantle.getChunk(i.getLocation().getChunk()).flag(MantleFlag.DISCOVERED, true);
                }
            }
        }
    }

    private void updateChunks() {
        for (Player i : getEngine().getWorld().realWorld().getPlayers()) {
            int r = 1;

            Chunk c = i.getLocation().getChunk();
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (c.getWorld().isChunkLoaded(c.getX() + x, c.getZ() + z) && Chunks.isSafe(getEngine().getWorld().realWorld(), c.getX() + x, c.getZ() + z)) {

                        if (KrudWorldSettings.get().getWorld().isPostLoadBlockUpdates()) {
                            getEngine().updateChunk(c.getWorld().getChunkAt(c.getX() + x, c.getZ() + z));
                        }

                        if (KrudWorldSettings.get().getWorld().isMarkerEntitySpawningSystem()) {
                            Chunk cx = getEngine().getWorld().realWorld().getChunkAt(c.getX() + x, c.getZ() + z);
                            int finalX = c.getX() + x;
                            int finalZ = c.getZ() + z;
                            J.a(() -> getMantle().raiseFlag(finalX, finalZ, MantleFlag.INITIAL_SPAWNED_MARKER,
                                    () -> {
                                        J.a(() -> spawnIn(cx, true), RNG.r.i(5, 200));
                                        getSpawnersFromMarkers(cx).forEach((blockf, spawners) -> {
                                            if (spawners.isEmpty()) {
                                                return;
                                            }

                                            KrudWorldPosition block = new KrudWorldPosition(blockf.getX(), blockf.getY() + getEngine().getWorld().minHeight(), blockf.getZ());
                                            KrudWorldSpawner s = new KList<>(spawners).getRandom();
                                            spawn(block, s, true);
                                        });
                                    }));
                        }
                    }
                }
            }
        }
    }

    private boolean onAsyncTick() {
        if (getEngine().isClosed()) {
            return false;
        }

        actuallySpawned = 0;

        if (energy < 100) {
            J.sleep(200);
            return false;
        }

        if (!getEngine().getWorld().hasRealWorld()) {
            KrudWorld.debug("Can't spawn. No real world");
            J.sleep(5000);
            return false;
        }

        double epx = getEntitySaturation();
        if (epx > KrudWorldSettings.get().getWorld().getTargetSpawnEntitiesPerChunk()) {
            KrudWorld.debug("Can't spawn. The entity per chunk ratio is at " + Form.pc(epx, 2) + " > 100% (total entities " + entityCount + ")");
            J.sleep(5000);
            return false;
        }

        if (cl.flip()) {
            try {
                J.s(() -> precount = getEngine().getWorld().realWorld().getEntities());
            } catch (Throwable e) {
                close();
            }
        }

        int spawnBuffer = RNG.r.i(2, 12);

        Chunk[] cc = getEngine().getWorld().realWorld().getLoadedChunks();
        while (spawnBuffer-- > 0) {
            if (cc.length == 0) {
                KrudWorld.debug("Can't spawn. No chunks!");
                return false;
            }

            Chunk c = cc[RNG.r.nextInt(cc.length)];

            if (!c.isLoaded() || !Chunks.isSafe(c.getWorld(), c.getX(), c.getZ())) {
                continue;
            }

            spawnIn(c, false);
        }

        energy -= (actuallySpawned / 2D);
        return actuallySpawned > 0;
    }

    private void fixEnergy() {
        energy = M.clip(energy, 1D, getDimension().getMaximumEnergy());
    }

    private void spawnIn(Chunk c, boolean initial) {
        if (getEngine().isClosed()) {
            return;
        }

        if (initial) {
            energy += 1.2;
        }

        if (KrudWorldSettings.get().getWorld().isMarkerEntitySpawningSystem()) {
            getSpawnersFromMarkers(c).forEach((blockf, spawners) -> {
                if (spawners.isEmpty()) {
                    return;
                }

                KrudWorldPosition block = new KrudWorldPosition(blockf.getX(), blockf.getY() + getEngine().getWorld().minHeight(), blockf.getZ());
                KrudWorldSpawner s = new KList<>(spawners).getRandom();
                spawn(block, s, false);
                J.a(() -> getMantle().raiseFlag(c.getX(), c.getZ(), MantleFlag.INITIAL_SPAWNED_MARKER,
                        () -> spawn(block, s, true)));
            });
        }

        if (!KrudWorldSettings.get().getWorld().isAnbientEntitySpawningSystem()) {
            return;
        }

        //@builder
        Predicate<KrudWorldSpawner> filter = i -> i.canSpawn(getEngine(), c.getX(), c.getZ());
        ChunkCounter counter = new ChunkCounter(c.getEntities());

        KrudWorldBiome biome = getEngine().getSurfaceBiome(c);
        KrudWorldEntitySpawn v = spawnRandomly(Stream.concat(getData().getSpawnerLoader()
                                .loadAll(getDimension().getEntitySpawners())
                                .shuffleCopy(RNG.r)
                                .stream()
                                .filter(filter)
                                .filter((i) -> i.isValid(biome)),
                        Stream.concat(getData()
                                        .getSpawnerLoader()
                                        .loadAll(getEngine().getRegion(c.getX() << 4, c.getZ() << 4).getEntitySpawners())
                                        .shuffleCopy(RNG.r)
                                        .stream()
                                        .filter(filter),
                                getData().getSpawnerLoader()
                                        .loadAll(getEngine().getSurfaceBiome(c.getX() << 4, c.getZ() << 4).getEntitySpawners())
                                        .shuffleCopy(RNG.r)
                                        .stream()
                                        .filter(filter)))
                .filter(counter)
                .flatMap((i) -> stream(i, initial))
                .collect(Collectors.toList()))
                .getRandom();
        //@done
        if (v == null || v.getReferenceSpawner() == null)
            return;

        try {
            spawn(c, v);
        } catch (Throwable e) {
            J.s(() -> spawn(c, v));
        }
    }

    private void spawn(Chunk c, KrudWorldEntitySpawn i) {
        KrudWorldSpawner ref = i.getReferenceSpawner();
        int s = i.spawn(getEngine(), c, RNG.r);
        actuallySpawned += s;
        if (s > 0) {
            ref.spawn(getEngine(), c.getX(), c.getZ());
            energy -= s * ((i.getEnergyMultiplier() * ref.getEnergyMultiplier() * 1));
        }
    }

    private void spawn(KrudWorldPosition pos, KrudWorldEntitySpawn i) {
        KrudWorldSpawner ref = i.getReferenceSpawner();
        if (!ref.canSpawn(getEngine(), pos.getX() >> 4, pos.getZ() >> 4))
            return;

        int s = i.spawn(getEngine(), pos, RNG.r);
        actuallySpawned += s;
        if (s > 0) {
            ref.spawn(getEngine(), pos.getX() >> 4, pos.getZ() >> 4);
            energy -= s * ((i.getEnergyMultiplier() * ref.getEnergyMultiplier() * 1));
        }
    }

    private Stream<KrudWorldEntitySpawn> stream(KrudWorldSpawner s, boolean initial) {
        for (KrudWorldEntitySpawn i : initial ? s.getInitialSpawns() : s.getSpawns()) {
            i.setReferenceSpawner(s);
            i.setReferenceMarker(s.getReferenceMarker());
        }

        return (initial ? s.getInitialSpawns() : s.getSpawns()).stream();
    }

    private KList<KrudWorldEntitySpawn> spawnRandomly(List<KrudWorldEntitySpawn> types) {
        KList<KrudWorldEntitySpawn> rarityTypes = new KList<>();
        int totalRarity = 0;

        for (KrudWorldEntitySpawn i : types) {
            totalRarity += IRare.get(i);
        }

        for (KrudWorldEntitySpawn i : types) {
            rarityTypes.addMultiple(i, totalRarity / IRare.get(i));
        }

        return rarityTypes;
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onSave() {
        getEngine().getMantle().save();
    }

    public void requestBiomeInject(Position2 p) {
        injectBiomes.add(p);
    }

    @Override
    public void onChunkLoad(Chunk e, boolean generated) {
        if (getEngine().isClosed()) {
            return;
        }

        int cX = e.getX(), cZ = e.getZ();
        Long key = Cache.key(e);
        cleanup.put(key, cleanupService.schedule(() -> {
            cleanup.remove(key);
            energy += 0.3;
            fixEnergy();
            getEngine().cleanupMantleChunk(cX, cZ);
        }, Math.max(KrudWorldSettings.get().getPerformance().mantleCleanupDelay * 50L, 0), TimeUnit.MILLISECONDS));

        if (generated) {
            //INMS.get().injectBiomesFromMantle(e, getMantle());

            if (!KrudWorldSettings.get().getGenerator().earlyCustomBlocks) return;
            KrudWorld.tickets.addTicket(e);
            J.s(() -> {
                var chunk = getMantle().getChunk(e).use();
                int minY = getTarget().getWorld().minHeight();
                try {
                    chunk.raiseFlagUnchecked(MantleFlag.CUSTOM, () -> {
                        chunk.iterate(Identifier.class, (x, y, z, v) -> {
                            KrudWorld.service(ExternalDataSVC.class).processUpdate(getEngine(), e.getBlock(x & 15, y + minY, z & 15), v);
                        });
                    });
                } finally {
                    chunk.release();
                    KrudWorld.tickets.removeTicket(e);
                }
            }, RNG.r.i(20, 60));
        }
    }

    @Override
    public void onChunkUnload(Chunk e) {
        final var future = cleanup.remove(Cache.key(e));
        if (future != null) {
            future.cancel(false);
        }
    }

    private void spawn(KrudWorldPosition block, KrudWorldSpawner spawner, boolean initial) {
        if (getEngine().isClosed()) {
            return;
        }

        if (spawner == null) {
            return;
        }

        KList<KrudWorldEntitySpawn> s = initial ? spawner.getInitialSpawns() : spawner.getSpawns();
        if (s.isEmpty()) {
            return;
        }

        KrudWorldEntitySpawn ss = spawnRandomly(s).getRandom();
        ss.setReferenceSpawner(spawner);
        ss.setReferenceMarker(spawner.getReferenceMarker());
        spawn(block, ss);
    }

    public Mantle getMantle() {
        return getEngine().getMantle().getMantle();
    }

    @Override
    public void chargeEnergy() {
        charge = M.ms() + 3000;
    }

    @Override
    public void teleportAsync(PlayerTeleportEvent e) {
        if (KrudWorldSettings.get().getWorld().getAsyncTeleport().isEnabled()) {
            e.setCancelled(true);
            warmupAreaAsync(e.getPlayer(), e.getTo(), () -> J.s(() -> {
                ignoreTP.set(true);
                e.getPlayer().teleport(e.getTo(), e.getCause());
                ignoreTP.set(false);
            }));
        }
    }

    private void warmupAreaAsync(Player player, Location to, Runnable r) {
        J.a(() -> {
            int viewDistance = KrudWorldSettings.get().getWorld().getAsyncTeleport().getLoadViewDistance();
            KList<Future<Chunk>> futures = new KList<>();
            for (int i = -viewDistance; i <= viewDistance; i++) {
                for (int j = -viewDistance; j <= viewDistance; j++) {
                    int finalJ = j;
                    int finalI = i;

                    if (to.getWorld().isChunkLoaded((to.getBlockX() >> 4) + i, (to.getBlockZ() >> 4) + j)) {
                        futures.add(CompletableFuture.completedFuture(null));
                        continue;
                    }

                    futures.add(MultiBurst.burst.completeValue(()
                            -> PaperLib.getChunkAtAsync(to.getWorld(),
                            (to.getBlockX() >> 4) + finalI,
                            (to.getBlockZ() >> 4) + finalJ,
                            true, KrudWorldSettings.get().getWorld().getAsyncTeleport().isUrgent()).get()));
                }
            }

            new QueueJob<Future<Chunk>>() {
                @Override
                public void execute(Future<Chunk> chunkFuture) {
                    try {
                        chunkFuture.get();
                    } catch (InterruptedException | ExecutionException ignored) {

                    }
                }

                @Override
                public String getName() {
                    return "Loading Chunks";
                }
            }.queue(futures).execute(new VolmitSender(player), true, r);
        });
    }

    public Map<KrudWorldPosition, KSet<KrudWorldSpawner>> getSpawnersFromMarkers(Chunk c) {
        Map<KrudWorldPosition, KSet<KrudWorldSpawner>> p = new KMap<>();
        Set<KrudWorldPosition> b = new KSet<>();
        getMantle().iterateChunk(c.getX(), c.getZ(), MatterMarker.class, (x, y, z, t) -> {
            if (t.getTag().equals("cave_floor") || t.getTag().equals("cave_ceiling")) {
                return;
            }

            KrudWorldMarker mark = getData().getMarkerLoader().load(t.getTag());
            KrudWorldPosition pos = new KrudWorldPosition((c.getX() << 4) + x, y, (c.getZ() << 4) + z);

            if (mark.isEmptyAbove()) {
                AtomicBoolean remove = new AtomicBoolean(false);

                try {
                    J.sfut(() -> {
                        if (c.getBlock(x, y + 1, z).getBlockData().getMaterial().isSolid() || c.getBlock(x, y + 2, z).getBlockData().getMaterial().isSolid()) {
                            remove.set(true);
                        }
                    }).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (remove.get()) {
                    b.add(pos);
                    return;
                }
            }

            for (String i : mark.getSpawners()) {
                KrudWorldSpawner m = getData().getSpawnerLoader().load(i);
                if (m == null) {
                    KrudWorld.error("Cannot load spawner: " + i + " for marker on " + getName());
                    continue;
                }
                m.setReferenceMarker(mark);

                // This is so fucking incorrect its a joke
                //noinspection ConstantConditions
                if (m != null) {
                    p.computeIfAbsent(pos, (k) -> new KSet<>()).add(m);
                }
            }
        });

        for (KrudWorldPosition i : b) {
            getEngine().getMantle().getMantle().remove(i.getX(), i.getY(), i.getZ(), MatterMarker.class);
        }

        return p;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getWorld().equals(getTarget().getWorld().realWorld())) {
            J.a(() -> {
                MatterMarker marker = getMantle().get(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), MatterMarker.class);

                if (marker != null) {
                    if (marker.getTag().equals("cave_floor") || marker.getTag().equals("cave_ceiling")) {
                        return;
                    }

                    KrudWorldMarker mark = getData().getMarkerLoader().load(marker.getTag());

                    if (mark == null || mark.isRemoveOnChange()) {
                        getMantle().remove(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), MatterMarker.class);
                    }
                }
            });

            KList<ItemStack> d = new KList<>();
            KrudWorldBiome b = getEngine().getBiome(e.getBlock().getLocation().clone().subtract(0, getEngine().getWorld().minHeight(), 0));
            List<KrudWorldBlockDrops> dropProviders = filterDrops(b.getBlockDrops(), e, getData());

            if (dropProviders.stream().noneMatch(KrudWorldBlockDrops::isSkipParents)) {
                KrudWorldRegion r = getEngine().getRegion(e.getBlock().getLocation());
                dropProviders.addAll(filterDrops(r.getBlockDrops(), e, getData()));
                dropProviders.addAll(filterDrops(getEngine().getDimension().getBlockDrops(), e, getData()));
            }

            dropProviders.forEach(provider -> provider.fillDrops(false, d));

            if (dropProviders.stream().anyMatch(KrudWorldBlockDrops::isReplaceVanillaDrops)) {
                e.setDropItems(false);
            }

            if (d.isNotEmpty()) {
                World w = e.getBlock().getWorld();
                J.s(() -> d.forEach(item -> w.dropItemNaturally(e.getBlock().getLocation().clone().add(.5, .5, .5), item)));
            }
        }
    }

    private List<KrudWorldBlockDrops> filterDrops(KList<KrudWorldBlockDrops> drops, BlockBreakEvent e, KrudWorldData data) {
        return new KList<>(drops.stream().filter(d -> d.shouldDropFor(e.getBlock().getBlockData(), data)).toList());
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent e) {

    }

    @Override
    public void close() {
        super.close();
        looper.interrupt();
    }

    @Override
    public int getChunkCount() {
        return getEngine().getWorld().realWorld().getLoadedChunks().length;
    }

    @Override
    public double getEntitySaturation() {
        if (!getEngine().getWorld().hasRealWorld()) {
            return 1;
        }

        return (double) entityCount / (getEngine().getWorld().realWorld().getLoadedChunks().length + 1) * 1.28;
    }

    @Data
    private static class ChunkCounter implements Predicate<KrudWorldSpawner> {
        private final Entity[] entities;
        private transient int index = 0;
        private transient int count = 0;

        @Override
        public boolean test(KrudWorldSpawner spawner) {
            int max = spawner.getMaxEntitiesPerChunk();
            if (max <= count)
                return false;

            while (index < entities.length) {
                if (entities[index++] instanceof LivingEntity) {
                    if (++count >= max)
                        return false;
                }
            }

            return true;
        }
    }
}
