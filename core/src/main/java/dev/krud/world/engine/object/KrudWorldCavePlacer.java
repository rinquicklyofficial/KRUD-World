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

@Snippet("cave-placer")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Translate objects")
@Data
public class KrudWorldCavePlacer implements IRare {
    private transient final AtomicCache<KrudWorldCave> caveCache = new AtomicCache<>();
    private transient final AtomicBoolean fail = new AtomicBoolean(false);
    @Required
    @Desc("Typically a 1 in RARITY on a per chunk/fork basis")
    @MinNumber(1)
    private int rarity = 15;
    @MinNumber(1)
    @Required
    @Desc("The cave to place")
    @RegistryListResource(KrudWorldCave.class)
    private String cave;
    @MinNumber(1)
    @MaxNumber(256)
    @Desc("The maximum recursion depth")
    private int maxRecursion = 16;
    @Desc("If set to true, this cave is allowed to break the surface")
    private boolean breakSurface = true;
    @Desc("The height range this cave can spawn at. If breakSurface is false, the output of this range will be clamped by the current world height to prevent surface breaking.")
    private KrudWorldStyledRange caveStartHeight = new KrudWorldStyledRange(13, 120, new KrudWorldGeneratorStyle(NoiseStyle.STATIC));

    public KrudWorldCave getRealCave(KrudWorldData data) {
        return caveCache.aquire(() -> data.getCaveLoader().load(getCave()));
    }

    public void generateCave(MantleWriter mantle, RNG rng, Engine engine, int x, int y, int z) {
        generateCave(mantle, rng, new RNG(engine.getSeedManager().getCarve()), engine, x, y, z, 0, -1);
    }

    public void generateCave(MantleWriter mantle, RNG rng, RNG base, Engine engine, int x, int y, int z, int recursion, int waterHint) {
        if (fail.get()) {
            return;
        }

        if (rng.nextInt(rarity) != 0) {
            return;
        }

        KrudWorldData data = engine.getData();
        KrudWorldCave cave = getRealCave(data);

        if (cave == null) {
            KrudWorld.warn("Unable to locate cave for generation!");
            fail.set(true);
            return;
        }

        if (y == -1) {
            int h = (int) caveStartHeight.get(base, x, z, data);
            int ma = breakSurface ? h : (int) (engine.getComplex().getHeightStream().get(x, z) - 9);
            y = Math.min(h, ma);
        }

        try {
             cave.generate(mantle, rng, base, engine, x + rng.nextInt(15), y, z + rng.nextInt(15), recursion, waterHint, breakSurface);
        } catch (Throwable e) {
            e.printStackTrace();
            fail.set(true);
        }
    }

    public int getSize(KrudWorldData data, int depth) {
        KrudWorldCave cave = getRealCave(data);

        if (cave != null) {
            return cave.getMaxSize(data, depth);
        }

        return 32;
    }
}
