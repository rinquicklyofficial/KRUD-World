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

package dev.krud.world.util.data.palette;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KMap;

import java.util.List;

public class HashMapPalette<T> implements Palette<T> {
    private final KMap<T, Integer> values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;
    private int id;

    public HashMapPalette(int var1, PaletteResize<T> var2) {
        this.bits = var1;
        this.resizeHandler = var2;
        this.values = new KMap<>();
        id = 1;
    }

    public int idFor(T var0) {
        if (var0 == null) {
            return 0;
        }

        return this.values.computeIfAbsent(var0, (k) -> {
            int newId = id++;

            if (newId >= 1 << this.bits) {
                KrudWorld.info(newId + " to...");
                newId = this.resizeHandler.onResize(this.bits + 1, var0);
                KrudWorld.info(newId + "..");
            }

            return newId;
        });
    }

    public T valueFor(int var0) {
        return this.values.getKey(var0);
    }

    public int getSize() {
        return this.values.size();
    }

    @Override
    public void read(List<T> data) {
        data.forEach(this::idFor);
    }

    @Override
    public void write(List<T> toList) {
        toList.addAll(values.keySet());
    }
}