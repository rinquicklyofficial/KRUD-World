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

import dev.krud.world.util.hunk.Hunk;
import lombok.Data;

@Data
public abstract class StorageHunk<T> implements Hunk<T> {
    private final int width;
    private final int height;
    private final int depth;

    public StorageHunk(int width, int height, int depth) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new RuntimeException("Unsupported size " + width + " " + height + " " + depth);
        }

        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public abstract void setRaw(int x, int y, int z, T t);

    @Override
    public abstract T getRaw(int x, int y, int z);
}
