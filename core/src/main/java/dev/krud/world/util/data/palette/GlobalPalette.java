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

import java.util.List;
import java.util.function.Predicate;

public class GlobalPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;

    private final T defaultValue;

    public GlobalPalette(T... f) {
        IdMapper<T> mapper = new IdMapper<>();
        for (T i : f) {
            mapper.add(i);
        }
        registry = mapper;
        defaultValue = f[0];
    }

    public GlobalPalette(IdMapper<T> var0, T var1) {
        this.registry = var0;
        this.defaultValue = var1;
    }

    public int idFor(T var0) {
        int var1 = this.registry.getId(var0);
        return (var1 == -1) ? 0 : var1;
    }

    public boolean maybeHas(Predicate<T> var0) {
        return true;
    }

    public T valueFor(int var0) {
        T var1 = this.registry.byId(var0);
        return (var1 == null) ? this.defaultValue : var1;
    }

    public int getSize() {
        return this.registry.size();
    }

    @Override
    public void read(List<T> fromList) {

    }

    @Override
    public void write(List<T> toList) {

    }
}
