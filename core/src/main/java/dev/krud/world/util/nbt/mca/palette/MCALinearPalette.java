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

package dev.krud.world.util.nbt.mca.palette;

import dev.krud.world.util.nbt.tag.CompoundTag;
import dev.krud.world.util.nbt.tag.ListTag;

import java.util.function.Function;
import java.util.function.Predicate;

public class MCALinearPalette<T> implements MCAPalette<T> {
    private final MCAIdMapper<T> registry;

    private final T[] values;

    private final MCAPaletteResize<T> resizeHandler;

    private final Function<CompoundTag, T> reader;

    private final int bits;

    private int size;

    public MCALinearPalette(MCAIdMapper<T> var0, int var1, MCAPaletteResize<T> var2, Function<CompoundTag, T> var3) {
        this.registry = var0;
        this.values = (T[]) new Object[1 << var1];
        this.bits = var1;
        this.resizeHandler = var2;
        this.reader = var3;
    }

    public int idFor(T var0) {
        int var1;
        for (var1 = 0; var1 < this.size; var1++) {
            if (this.values[var1] == var0)
                return var1;
        }
        var1 = this.size;
        if (var1 < this.values.length) {
            this.values[var1] = var0;
            this.size++;
            return var1;
        }
        return this.resizeHandler.onResize(this.bits + 1, var0);
    }

    public boolean maybeHas(Predicate<T> var0) {
        for (int var1 = 0; var1 < this.size; var1++) {
            if (var0.test(this.values[var1]))
                return true;
        }
        return false;
    }

    public T valueFor(int var0) {
        if (var0 >= 0 && var0 < this.size)
            return this.values[var0];
        return null;
    }

    public int getSize() {
        return this.size;
    }

    public void read(ListTag var0) {
        for (int var1 = 0; var1 < var0.size(); var1++) {
            this.values[var1] = this.reader.apply((CompoundTag) var0.get(var1));
        }
        this.size = var0.size();
    }
}
