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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FloatBitCache extends DataBitCache<Float> {
    public FloatBitCache(int width, int height) {
        super(width, height);
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
