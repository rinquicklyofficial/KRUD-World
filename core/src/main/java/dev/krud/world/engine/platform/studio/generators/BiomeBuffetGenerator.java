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

package dev.krud.world.engine.platform.studio.generators;

import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.data.chunk.TerrainChunk;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.WrongEngineBroException;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.platform.studio.EnginedStudioGenerator;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

public class BiomeBuffetGenerator extends EnginedStudioGenerator {
    private static final BlockData FLOOR = Material.BARRIER.createBlockData();
    private final KrudWorldBiome[] biomes;
    private final int width;
    private final int biomeSize;

    public BiomeBuffetGenerator(Engine engine, int biomeSize) {
        super(engine);
        this.biomeSize = biomeSize;
        biomes = engine.getDimension().getAllBiomes(engine).toArray(new KrudWorldBiome[0]);
        width = Math.max((int) Math.sqrt(biomes.length), 1);
    }

    @Override
    public void generateChunk(Engine engine, TerrainChunk tc, int x, int z) throws WrongEngineBroException {
        int id = Cache.to1D(x / biomeSize, 0, z / biomeSize, width, 1);

        if (id >= 0 && id < biomes.length) {
            KrudWorldBiome biome = biomes[id];
            String foc = engine.getDimension().getFocus();

            if (!Objects.equals(foc, biome.getLoadKey())) {
                engine.getDimension().setFocus(biome.getLoadKey());
                engine.hotloadComplex();
            }

            engine.generate(x << 4, z << 4, tc, true);
        } else {
            tc.setRegion(0, 0, 0, 16, 1, 16, FLOOR);
        }
    }
}
