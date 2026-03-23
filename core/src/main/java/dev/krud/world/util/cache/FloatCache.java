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

import java.io.*;

public class FloatCache implements ArrayCache<Float> {
    @Getter
    private final int width;
    @Getter
    private final int height;
    private final float[] cache;


    public FloatCache(File file) throws IOException {
        this(new DataInputStream(new FileInputStream(file)));
    }

    public FloatCache(DataInputStream din) throws IOException {
        this(din.readInt(), din.readInt());
        for (int i = 0; i < width * height; i++) {
            cache[i] = din.readFloat();
        }
        din.close();
    }

    public FloatCache(int width, int height) {
        this.width = width;
        this.height = height;
        cache = new float[width * height];
    }

    public void set(int i, Float v) {
        cache[i] = v;
    }

    public Float get(int i) {
        return cache[i];
    }

    @Override
    public void writeCache(DataOutputStream dos) throws IOException {
        dos.writeInt(width);
        dos.writeInt(height);

        for (int i = 0; i < width * height; i++) {
            dos.writeFloat(get(i));
        }
    }

    @Override
    public Float readNodeData(DataInputStream din) throws IOException {
        return din.readFloat();
    }

    @Override
    public void writeNodeData(DataOutputStream dos, Float integer) throws IOException {
        dos.writeFloat(integer);
    }

    @Override
    public void iset(int i, int v) {
        set(i, (float) v);
    }
}
