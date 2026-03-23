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
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicBoolean;

@Snippet("ravine-placer")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Translate objects")
@Data
public class KrudWorldRavinePlacer implements IRare {
    private transient final AtomicCache<KrudWorldRavine> ravineCache = new AtomicCache<>();
    private transient final AtomicBoolean fail = new AtomicBoolean(false);
    @Required
    @Desc("Typically a 1 in RARITY on a per chunk/fork basis")
    @MinNumber(1)
    private int rarity = 15;
    @MinNumber(1)
    @Required
    @Desc("The ravine to place")
    @RegistryListResource(KrudWorldRavine.class)
    private String ravine;
    @MinNumber(1)
    @MaxNumber(256)
    @Desc("The maximum recursion depth")
    private int maxRecursion = 100;

    public KrudWorldRavine getRealRavine(KrudWorldData data) {
        return ravineCache.aquire(() -> data.getRavineLoader().load(getRavine()));
    }

    public void generateRavine(MantleWriter mantle, RNG rng, Engine engine, int x, int y, int z) {
        generateRavine(mantle, rng, new RNG(engine.getSeedManager().getCarve()), engine, x, y, z, 0, -1);
    }

    public void generateRavine(MantleWriter mantle, RNG rng, RNG base, Engine engine, int x, int y, int z, int recursion, int waterHint) {
        if (fail.get()) {
            return;
        }

        if (rng.nextInt(rarity) != 0) {
            return;
        }

        KrudWorldData data = engine.getData();
        KrudWorldRavine ravine = getRealRavine(data);

        if (ravine == null) {
            KrudWorld.warn("Unable to locate ravine for generation!");
            fail.set(true);
            return;
        }

        try {
            int xx = x + rng.nextInt(15);
            int zz = z + rng.nextInt(15);
            ravine.generate(mantle, rng, base, engine, xx, y, zz, recursion, waterHint);
        } catch (Throwable e) {
            e.printStackTrace();
            fail.set(true);
        }
    }

    public int getSize(KrudWorldData data, int depth) {
        return getRealRavine(data).getMaxSize(data, depth);
    }
}
