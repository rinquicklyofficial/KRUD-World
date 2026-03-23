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

public class ByteCache implements ArrayCache<Integer> {
    @Getter
    private final int width;
    @Getter
    private final int height;
    private final byte[] cache;

    public ByteCache(int width, int height) {
        this.width = width;
        this.height = height;
        cache = new byte[width * height];
    }

    public void set(int i, Integer v) {
        cache[i] = v.byteValue();
    }

    public Integer get(int i) {
        return (int) cache[i];
    }

    @Override
    public void writeCache(DataOutputStream dos) throws IOException {
        dos.writeInt(width);
        dos.writeInt(height);

        for (int i = 0; i < width * height; i++) {
            dos.writeByte(get(i));
        }
    }

    @Override
    public Integer readNodeData(DataInputStream din) throws IOException {
        return (int) din.readByte();
    }

    @Override
    public void writeNodeData(DataOutputStream dos, Integer integer) throws IOException {
        dos.writeByte(integer);
    }

    @Override
    public void iset(int i, int v) {
        set(i, v);
    }
}
