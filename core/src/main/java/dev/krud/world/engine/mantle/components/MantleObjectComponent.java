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

package dev.krud.world.engine.mantle.components;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.mantle.ComponentFlag;
import dev.krud.world.engine.mantle.EngineMantle;
import dev.krud.world.engine.mantle.KrudWorldMantleComponent;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.data.B;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.mantle.flag.ReservedFlag;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterStructurePOI;
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.noise.NoiseType;
import dev.krud.world.util.parallel.BurstExecutor;
import org.bukkit.util.BlockVector;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@ComponentFlag(ReservedFlag.OBJECT)
public class MantleObjectComponent extends KrudWorldMantleComponent {

    public MantleObjectComponent(EngineMantle engineMantle) {
        super(engineMantle, ReservedFlag.OBJECT, 1);
    }

    @Override
    public void generateLayer(MantleWriter writer, int x, int z, ChunkContext context) {
        RNG rng = applyNoise(x, z, Cache.key(x, z) + seed());
        int xxx = 8 + (x << 4);
        int zzz = 8 + (z << 4);
        KrudWorldRegion region = getComplex().getRegionStream().get(xxx, zzz);
        KrudWorldBiome biome = getComplex().getTrueBiomeStream().get(xxx, zzz);
        placeObjects(writer, rng, x, z, biome, region);
    }

    private RNG applyNoise(int x, int z, long seed) {
        CNG noise = CNG.signatureFast(new RNG(seed), NoiseType.WHITE, NoiseType.GLOB);
        return new RNG((long) (seed * noise.noise(x, z)));
    }

    @ChunkCoordinates
    private void placeObjects(MantleWriter writer, RNG rng, int x, int z, KrudWorldBiome biome, KrudWorldRegion region) {
        for (KrudWorldObjectPlacement i : biome.getSurfaceObjects()) {
            if (rng.chance(i.getChance() + rng.d(-0.005, 0.005))) {
                try {
                    placeObject(writer, rng, x << 4, z << 4, i);
                } catch (Throwable e) {
                    KrudWorld.reportError(e);
                    KrudWorld.error("Failed to place objects in the following biome: " + biome.getName());
                    KrudWorld.error("Object(s) " + i.getPlace().toString(", ") + " (" + e.getClass().getSimpleName() + ").");
                    KrudWorld.error("Are these objects missing?");
                    e.printStackTrace();
                }
            }
        }

        for (KrudWorldObjectPlacement i : region.getSurfaceObjects()) {
            if (rng.chance(i.getChance() + rng.d(-0.005, 0.005))) {
                try {
                    placeObject(writer, rng, x << 4, z << 4, i);
                } catch (Throwable e) {
                    KrudWorld.reportError(e);
                    KrudWorld.error("Failed to place objects in the following region: " + region.getName());
                    KrudWorld.error("Object(s) " + i.getPlace().toString(", ") + " (" + e.getClass().getSimpleName() + ").");
                    KrudWorld.error("Are these objects missing?");
                    e.printStackTrace();
                }
            }
        }
    }

    @BlockCoordinates
    private void placeObject(MantleWriter writer, RNG rng, int x, int z, KrudWorldObjectPlacement objectPlacement) {
        for (int i = 0; i < objectPlacement.getDensity(rng, x, z, getData()); i++) {
            KrudWorldObject v = objectPlacement.getScale().get(rng, objectPlacement.getObject(getComplex(), rng));
            if (v == null) {
                return;
            }
            int xx = rng.i(x, x + 15);
            int zz = rng.i(z, z + 15);
            int id = rng.i(0, Integer.MAX_VALUE);
            v.place(xx, -1, zz, writer, objectPlacement, rng, (b, data) -> {
                writer.setData(b.getX(), b.getY(), b.getZ(), v.getLoadKey() + "@" + id);
                if (objectPlacement.isDolphinTarget() && objectPlacement.isUnderwater() && B.isStorageChest(data)) {
                    writer.setData(b.getX(), b.getY(), b.getZ(), MatterStructurePOI.BURIED_TREASURE);
                }
            }, null, getData());
        }
    }

    @BlockCoordinates
    private Set<String> guessPlacedKeys(RNG rng, int x, int z, KrudWorldObjectPlacement objectPlacement) {
        Set<String> f = new KSet<>();
        for (int i = 0; i < objectPlacement.getDensity(rng, x, z, getData()); i++) {
            KrudWorldObject v = objectPlacement.getScale().get(rng, objectPlacement.getObject(getComplex(), rng));
            if (v == null) {
                continue;
            }

            f.add(v.getLoadKey());
        }

        return f;
    }

    public Set<String> guess(int x, int z) {
        // todo The guess doesnt bring into account that the placer may return -1
        RNG rng = applyNoise(x, z, Cache.key(x, z) + seed());
        KrudWorldBiome biome = getEngineMantle().getEngine().getSurfaceBiome((x << 4) + 8, (z << 4) + 8);
        KrudWorldRegion region = getEngineMantle().getEngine().getRegion((x << 4) + 8, (z << 4) + 8);
        Set<String> v = new KSet<>();
        for (KrudWorldObjectPlacement i : biome.getSurfaceObjects()) {
            if (rng.chance(i.getChance() + rng.d(-0.005, 0.005))) {
                v.addAll(guessPlacedKeys(rng, x, z, i));
            }
        }

        for (KrudWorldObjectPlacement i : region.getSurfaceObjects()) {
            if (rng.chance(i.getChance() + rng.d(-0.005, 0.005))) {
                v.addAll(guessPlacedKeys(rng, x, z, i));
            }
        }

        return v;
    }

    protected int computeRadius() {
        var dimension = getDimension();

        AtomicInteger xg = new AtomicInteger();
        AtomicInteger zg = new AtomicInteger();

        KSet<String> objects = new KSet<>();
        KMap<KrudWorldObjectScale, KList<String>> scalars = new KMap<>();
        for (var region : dimension.getAllRegions(this::getData)) {
            for (var j : region.getObjects()) {
                if (j.getScale().canScaleBeyond()) {
                    scalars.put(j.getScale(), j.getPlace());
                } else {
                    objects.addAll(j.getPlace());
                }
            }
        }
        for (var biome : dimension.getAllBiomes(this::getData)) {
            for (var j : biome.getObjects()) {
                if (j.getScale().canScaleBeyond()) {
                    scalars.put(j.getScale(), j.getPlace());
                } else {
                    objects.addAll(j.getPlace());
                }
            }
        }

        BurstExecutor e = getEngineMantle().getTarget().getBurster().burst(objects.size());
        KMap<String, BlockVector> sizeCache = new KMap<>();
        for (String i : objects) {
            e.queue(() -> {
                try {
                    BlockVector bv = sizeCache.computeIfAbsent(i, (k) -> {
                        try {
                            return KrudWorldObject.sampleSize(getData().getObjectLoader().findFile(i));
                        } catch (IOException ex) {
                            KrudWorld.reportError(ex);
                            ex.printStackTrace();
                        }

                        return null;
                    });

                    if (bv == null) {
                        throw new RuntimeException();
                    }

                    if (Math.max(bv.getBlockX(), bv.getBlockZ()) > 128) {
                        KrudWorld.warn("Object " + i + " has a large size (" + bv + ") and may increase memory usage!");
                    }

                    synchronized (xg) {
                        xg.getAndSet(Math.max(bv.getBlockX(), xg.get()));
                    }

                    synchronized (zg) {
                        zg.getAndSet(Math.max(bv.getBlockZ(), zg.get()));
                    }
                } catch (Throwable ed) {
                    KrudWorld.reportError(ed);

                }
            });
        }

        for (Map.Entry<KrudWorldObjectScale, KList<String>> entry : scalars.entrySet()) {
            double ms = entry.getKey().getMaximumScale();
            for (String j : entry.getValue()) {
                e.queue(() -> {
                    try {
                        BlockVector bv = sizeCache.computeIfAbsent(j, (k) -> {
                            try {
                                return KrudWorldObject.sampleSize(getData().getObjectLoader().findFile(j));
                            } catch (IOException ioException) {
                                KrudWorld.reportError(ioException);
                                ioException.printStackTrace();
                            }

                            return null;
                        });

                        if (bv == null) {
                            throw new RuntimeException();
                        }

                        if (Math.max(bv.getBlockX(), bv.getBlockZ()) > 128) {
                            KrudWorld.warn("Object " + j + " has a large size (" + bv + ") and may increase memory usage! (Object scaled up to " + Form.pc(ms, 2) + ")");
                        }

                        synchronized (xg) {
                            xg.getAndSet((int) Math.max(Math.ceil(bv.getBlockX() * ms), xg.get()));
                        }

                        synchronized (zg) {
                            zg.getAndSet((int) Math.max(Math.ceil(bv.getBlockZ() * ms), zg.get()));
                        }
                    } catch (Throwable ee) {
                        KrudWorld.reportError(ee);

                    }
                });
            }
        }

        e.complete();
        return Math.max(xg.get(), zg.get());
    }
}
