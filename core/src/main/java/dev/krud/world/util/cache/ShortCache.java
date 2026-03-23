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

import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShortCache implements ArrayCache<Short> {
    @Getter
    private final int width;
    @Getter
    private final int height;
    private final short[] cache;

    public ShortCache(int width, int height) {
        this.width = width;
        this.height = height;
        cache = new short[width * height];
    }

    public void set(int i, Short v) {
        cache[i] = v;
    }

    public Short get(int i) {
        return cache[i];
    }

    @Override
    public void writeCache(DataOutputStream dos) throws IOException {
        dos.writeInt(width);
        dos.writeInt(height);

        for (int i = 0; i < width * height; i++) {
            dos.writeShort(get(i));
        }
    }

    @Override
    public Short readNodeData(DataInputStream din) throws IOException {
        return din.readShort();
    }

    @Override
    public void writeNodeData(DataOutputStream dos, Short integer) throws IOException {
        dos.writeShort(integer);
    }

    @Override
    public void iset(int i, int v) {
        set(i, (short) v);
    }
}
