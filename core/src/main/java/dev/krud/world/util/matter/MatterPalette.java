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

package dev.krud.world.util.matter;

import dev.krud.world.util.data.DataPalette;
import dev.krud.world.util.data.IOAdapter;
import dev.krud.world.util.data.Varint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MatterPalette<T> implements IOAdapter<T> {
    private final MatterSlice<T> slice;
    private final DataPalette<T> palette;

    public MatterPalette(MatterSlice<T> slice) {
        this.slice = slice;
        palette = new DataPalette<T>();
    }

    public MatterPalette(MatterSlice<T> slice, DataInputStream din) throws IOException {
        this.slice = slice;
        palette = DataPalette.getPalette(this, din);
    }

    public void writeNode(T t, DataOutputStream dos) throws IOException {
        Varint.writeUnsignedVarInt(palette.getIndex(t), dos);
    }

    public T readNode(DataInputStream din) throws IOException {
        return palette.get(Varint.readUnsignedVarInt(din));
    }

    public void writePalette(DataOutputStream dos) throws IOException {
        palette.write(this, dos);
    }

    @Override
    public void write(T t, DataOutputStream dos) throws IOException {
        slice.writeNode(t, dos);
    }

    @Override
    public T read(DataInputStream din) throws IOException {
        return slice.readNode(din);
    }

    public void assign(T b) {
        palette.getIndex(b);
    }
}
