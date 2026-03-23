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

package dev.krud.world.util.data.palette;

import dev.krud.world.util.data.Varint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface PaletteType<T> {
    void writePaletteNode(DataOutputStream dos, T t) throws IOException;

    T readPaletteNode(DataInputStream din) throws IOException;

    default void writeList(DataOutputStream dos, List<T> list) throws IOException {
        Varint.writeUnsignedVarInt(list.size(), dos);
        for (T i : list) {
            writePaletteNode(dos, i);
        }
    }

    default List<T> readList(DataInputStream din) throws IOException {
        int v = Varint.readUnsignedVarInt(din);
        List<T> t = new ArrayList<>();

        for (int i = 0; i < v; i++) {
            t.add(readPaletteNode(din));
        }

        return t;
    }
}
