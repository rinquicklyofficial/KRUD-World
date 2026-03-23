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

package dev.krud.world.util.hunk.storage;

import dev.krud.world.util.function.Consumer4;
import dev.krud.world.util.function.Consumer4IO;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.hunk.bits.DataContainer;
import dev.krud.world.util.hunk.bits.Writable;

import java.io.IOException;
import java.util.function.Supplier;

public abstract class PaletteOrHunk<T> extends StorageHunk<T> implements Hunk<T>, Writable<T> {
    private final Hunk<T> hunk;

    public PaletteOrHunk(int width, int height, int depth, boolean allow, Supplier<Hunk<T>> factory) {
        super(width, height, depth);
        hunk = (allow && (width * height * depth <= 4096)) ? new PaletteHunk<>(width, height, depth, this) : factory.get();
    }

    public DataContainer<T> palette() {
        return isPalette() ? ((PaletteHunk<T>) hunk).getData() : null;
    }

    public boolean isPalette() {
        return hunk instanceof PaletteHunk;
    }

    public void setPalette(DataContainer<T> c) {
        if (isPalette()) {
            ((PaletteHunk<T>) hunk).setPalette(c);
        }
    }

    @Override
    public void setRaw(int x, int y, int z, T t) {
        hunk.setRaw(x, y, z, t);
    }

    @Override
    public T getRaw(int x, int y, int z) {
        return hunk.getRaw(x, y, z);
    }

    public int getEntryCount() {
        return hunk.getEntryCount();
    }

    public boolean isMapped() {
        return hunk.isMapped();
    }

    public boolean isEmpty() {
        return hunk.isMapped();
    }

    @Override
    public synchronized Hunk<T> iterateSync(Consumer4<Integer, Integer, Integer, T> c) {
        hunk.iterateSync(c);
        return this;
    }

    @Override
    public synchronized Hunk<T> iterateSyncIO(Consumer4IO<Integer, Integer, Integer, T> c) throws IOException {
        hunk.iterateSyncIO(c);
        return this;
    }

    @Override
    public void empty(T b) {
        hunk.empty(b);
    }
}
