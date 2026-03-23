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
import dev.krud.world.engine.object.KrudWorldFluidBodies;
import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.mantle.flag.ReservedFlag;
import dev.krud.world.util.math.RNG;

@ComponentFlag(ReservedFlag.FLUID_BODIES)
public class MantleFluidBodyComponent extends KrudWorldMantleComponent {
    public MantleFluidBodyComponent(EngineMantle engineMantle) {
        super(engineMantle, ReservedFlag.FLUID_BODIES, 0);
    }

    @Override
    public void generateLayer(MantleWriter writer, int x, int z, ChunkContext context) {
        RNG rng = new RNG(Cache.key(x, z) + seed() + 405666);
        int xxx = 8 + (x << 4);
        int zzz = 8 + (z << 4);
        KrudWorldRegion region = getComplex().getRegionStream().get(xxx, zzz);
        KrudWorldBiome biome = getComplex().getTrueBiomeStream().get(xxx, zzz);
        generate(writer, rng, x, z, region, biome);
    }

    @ChunkCoordinates
    private void generate(MantleWriter writer, RNG rng, int cx, int cz, KrudWorldRegion region, KrudWorldBiome biome) {
        generate(getDimension().getFluidBodies(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
        generate(biome.getFluidBodies(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
        generate(region.getFluidBodies(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
    }

    @ChunkCoordinates
    private void generate(KrudWorldFluidBodies bodies, MantleWriter writer, RNG rng, int cx, int cz) {
        bodies.generate(writer, rng, getEngineMantle().getEngine(), cx << 4, -1, cz << 4);
    }

    protected int computeRadius() {
        int max = 0;

        max = Math.max(max, getDimension().getFluidBodies().getMaxRange(getData()));

        for (KrudWorldRegion i : getDimension().getAllRegions(this::getData)) {
            max = Math.max(max, i.getFluidBodies().getMaxRange(getData()));
        }

        for (KrudWorldBiome i : getDimension().getAllBiomes(this::getData)) {
            max = Math.max(max, i.getFluidBodies().getMaxRange(getData()));
        }

        return max;
    }
}
