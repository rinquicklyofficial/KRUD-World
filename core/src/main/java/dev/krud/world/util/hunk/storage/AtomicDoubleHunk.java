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

import com.google.common.util.concurrent.AtomicDoubleArray;
import dev.krud.world.util.hunk.Hunk;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings({"Lombok"})
@Data
@EqualsAndHashCode(callSuper = false)
public class AtomicDoubleHunk extends StorageHunk<Double> implements Hunk<Double> {
    private final AtomicDoubleArray data;

    public AtomicDoubleHunk(int w, int h, int d) {
        super(w, h, d);
        data = new AtomicDoubleArray(w * h * d);
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public void setRaw(int x, int y, int z, Double t) {
        data.set(index(x, y, z), t);
    }

    @Override
    public Double getRaw(int x, int y, int z) {
        return data.get(index(x, y, z));
    }

    private int index(int x, int y, int z) {
        return (z * getWidth() * getHeight()) + (y * getWidth()) + x;
    }
}
