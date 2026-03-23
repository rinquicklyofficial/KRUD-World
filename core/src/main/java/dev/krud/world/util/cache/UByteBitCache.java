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

package dev.krud.world.util.cache;

public class UByteBitCache extends ByteBitCache {
    public UByteBitCache(int width, int height) {
        super(width, height);
    }

    @Override
    public void set(int i, Integer v) {
        super.set(i, v + Byte.MIN_VALUE);
    }

    @Override
    public Integer get(int i) {
        return super.get(i) - Byte.MIN_VALUE;
    }

    @Override
    public void iset(int i, int v) {
        set(i, v);
    }
}
