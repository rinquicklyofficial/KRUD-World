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

package dev.krud.world.core.nms;

@FunctionalInterface
public interface BiomeBaseInjector {
    default void setBiome(int x, int z, Object biomeBase) {
        setBiome(x, 0, z, biomeBase);
    }

    void setBiome(int x, int y, int z, Object biomeBase);
}
