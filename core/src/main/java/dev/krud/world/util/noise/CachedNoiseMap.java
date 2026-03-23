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

package dev.krud.world.util.noise;

import dev.krud.world.util.hunk.bits.Writable;
import dev.krud.world.util.matter.KrudWorldMatter;
import dev.krud.world.util.matter.Matter;
import dev.krud.world.util.matter.MatterSlice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class CachedNoiseMap implements Writable<Integer> {
    private final Matter noise;
    private final MatterSlice<Integer> slice;

    public CachedNoiseMap(int size, NoiseGenerator cng) {
        noise = new KrudWorldMatter(size, size, 1);
        slice = noise.slice(Integer.class);

        for (int i = 0; i < slice.getWidth(); i++) {
            for (int j = 0; j < slice.getHeight(); j++) {
                set(i, j, cng.noise(i, j));
            }
        }
    }

    public CachedNoiseMap(File file) throws IOException, ClassNotFoundException {
        noise = Matter.read(file);
        slice = noise.slice(Integer.class);
    }

    void write(File file) throws IOException {
        noise.write(file);
    }

    void set(int x, int y, double value) {
        slice.set(x % slice.getWidth(), y % slice.getHeight(), 0, Float.floatToIntBits((float) value));
    }

    double get(int x, int y) {
        Integer i = slice.get(x % slice.getWidth(), y % slice.getHeight(), 0);

        if (i == null) {
            return 0;
        }

        return Float.intBitsToFloat(i);
    }

    @Override
    public Integer readNodeData(DataInputStream din) throws IOException {
        return din.readInt();
    }

    @Override
    public void writeNodeData(DataOutputStream dos, Integer integer) throws IOException {
        dos.writeInt(integer);
    }
}
