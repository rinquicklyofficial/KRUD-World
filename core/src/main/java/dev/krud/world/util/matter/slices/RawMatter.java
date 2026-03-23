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

package dev.krud.world.util.matter.slices;

import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.hunk.storage.MappedHunk;
import dev.krud.world.util.hunk.storage.PaletteOrHunk;
import dev.krud.world.util.matter.MatterReader;
import dev.krud.world.util.matter.MatterSlice;
import dev.krud.world.util.matter.MatterWriter;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class RawMatter<T> extends PaletteOrHunk<T> implements MatterSlice<T> {
    protected final KMap<Class<?>, MatterWriter<?, T>> writers;
    protected final KMap<Class<?>, MatterReader<?, T>> readers;
    @Getter
    private final Class<T> type;

    public RawMatter(int width, int height, int depth, Class<T> type) {
        super(width, height, depth, true, () -> new MappedHunk<>(width, height, depth));
        writers = new KMap<>();
        readers = new KMap<>();
        this.type = type;
    }

    protected <W> void registerWriter(Class<W> mediumType, MatterWriter<W, T> injector) {
        writers.put(mediumType, injector);
    }

    protected <W> void registerReader(Class<W> mediumType, MatterReader<W, T> injector) {
        readers.put(mediumType, injector);
    }

    @Override
    public <W> MatterWriter<W, T> writeInto(Class<W> mediumType) {
        return (MatterWriter<W, T>) writers.get(mediumType);
    }

    @Override
    public <W> MatterReader<W, T> readFrom(Class<W> mediumType) {
        return (MatterReader<W, T>) readers.get(mediumType);
    }

    @Override
    public abstract void writeNode(T b, DataOutputStream dos) throws IOException;

    @Override
    public abstract T readNode(DataInputStream din) throws IOException;
}
