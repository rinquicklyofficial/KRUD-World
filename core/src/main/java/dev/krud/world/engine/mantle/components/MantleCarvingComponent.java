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

import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.mantle.ComponentFlag;
import dev.krud.world.engine.mantle.EngineMantle;
import dev.krud.world.engine.mantle.KrudWorldMantleComponent;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldCarving;
import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.mantle.flag.ReservedFlag;
import dev.krud.world.util.math.RNG;

@ComponentFlag(ReservedFlag.CARVED)
public class MantleCarvingComponent extends KrudWorldMantleComponent {
    public MantleCarvingComponent(EngineMantle engineMantle) {
        super(engineMantle, ReservedFlag.CARVED, 0);
    }

    @Override
    public void generateLayer(MantleWriter writer, int x, int z, ChunkContext context) {
        RNG rng = new RNG(Cache.key(x, z) + seed());
        int xxx = 8 + (x << 4);
        int zzz = 8 + (z << 4);
        KrudWorldRegion region = getComplex().getRegionStream().get(xxx, zzz);
        KrudWorldBiome biome = getComplex().getTrueBiomeStream().get(xxx, zzz);
        carve(writer, rng, x, z, region, biome);
    }

    @ChunkCoordinates
    private void carve(MantleWriter writer, RNG rng, int cx, int cz, KrudWorldRegion region, KrudWorldBiome biome) {
        carve(getDimension().getCarving(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
        carve(biome.getCarving(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
        carve(region.getCarving(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
    }

    @ChunkCoordinates
    private void carve(KrudWorldCarving carving, MantleWriter writer, RNG rng, int cx, int cz) {
        carving.doCarving(writer, rng, getEngineMantle().getEngine(), cx << 4, -1, cz << 4, 0);
    }

    protected int computeRadius() {
        var dimension = getDimension();
        int max = 0;

        max = Math.max(max, dimension.getCarving().getMaxRange(getData(), 0));

        for (var i : dimension.getAllRegions(this::getData)) {
            max = Math.max(max, i.getCarving().getMaxRange(getData(), 0));
        }

        for (var i : dimension.getAllBiomes(this::getData)) {
            max = Math.max(max, i.getCarving().getMaxRange(getData(), 0));
        }

        return max;
    }
}
