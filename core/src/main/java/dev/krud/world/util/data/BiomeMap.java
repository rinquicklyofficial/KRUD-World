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

package dev.krud.world.util.data;

import dev.krud.world.engine.object.KrudWorldBiome;

public class BiomeMap {
    private final KrudWorldBiome[] height;

    public BiomeMap() {
        height = new KrudWorldBiome[256];
    }

    public void setBiome(int x, int z, KrudWorldBiome h) {
        height[x * 16 + z] = h;
    }

    public KrudWorldBiome getBiome(int x, int z) {
        return height[x * 16 + z];
    }
}
