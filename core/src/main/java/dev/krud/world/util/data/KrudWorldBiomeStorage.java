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

import dev.krud.world.util.math.KrudWorldMathHelper;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.jetbrains.annotations.NotNull;

public class KrudWorldBiomeStorage implements BiomeGrid {
    public static final int a;
    public static final int b;
    public static final int c;
    private static final int e;
    private static final int f;

    static {
        e = (int) Math.round(Math.log(16.0) / Math.log(2.0)) - 2;
        f = (int) Math.round(Math.log(256.0) / Math.log(2.0)) - 2; // TODO: WARNING HEIGHT
        a = 1 << KrudWorldBiomeStorage.e + KrudWorldBiomeStorage.e + KrudWorldBiomeStorage.f;
        b = (1 << KrudWorldBiomeStorage.e) - 1;
        c = (1 << KrudWorldBiomeStorage.f) - 1;
    }

    private final Biome[] g;

    public KrudWorldBiomeStorage(final Biome[] aBiome) {
        this.g = aBiome;
    }

    public KrudWorldBiomeStorage() {
        this(new Biome[KrudWorldBiomeStorage.a]);
    }

    public KrudWorldBiomeStorage b() {
        return new KrudWorldBiomeStorage(this.g.clone());
    }

    public void inject(BiomeGrid grid) {
        // TODO: WARNING HEIGHT
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    Biome b = getBiome(j, i, k);

                    if (b == null || b.equals(Biome.THE_VOID)) {
                        continue;
                    }

                    grid.setBiome(j, i, k, b);
                }
            }
        }
    }

    @NotNull
    @Override
    public Biome getBiome(int x, int z) {
        return null;
    }

    public Biome getBiome(final int x, final int y, final int z) {
        final int l = x & KrudWorldBiomeStorage.b;
        final int i2 = KrudWorldMathHelper.clamp(y, 0, KrudWorldBiomeStorage.c);
        final int j2 = z & KrudWorldBiomeStorage.b;
        return this.g[i2 << KrudWorldBiomeStorage.e + KrudWorldBiomeStorage.e | j2 << KrudWorldBiomeStorage.e | l];
    }

    @Override
    public void setBiome(int x, int z, @NotNull Biome bio) {

    }

    public void setBiome(final int x, final int y, final int z, final Biome biome) {
        final int l = x & KrudWorldBiomeStorage.b;
        final int i2 = KrudWorldMathHelper.clamp(y, 0, KrudWorldBiomeStorage.c);
        final int j2 = z & KrudWorldBiomeStorage.b;
        this.g[i2 << KrudWorldBiomeStorage.e + KrudWorldBiomeStorage.e | j2 << KrudWorldBiomeStorage.e | l] = biome;
    }
}