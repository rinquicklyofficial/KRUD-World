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

import dev.krud.world.util.math.M;
import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class MatterHeader {
    private String author = "";
    private long createdAt = M.ms();
    private int version = Matter.VERSION;

    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(author);
        out.writeLong(createdAt);
        out.writeShort(version);
    }

    public void read(DataInputStream din) throws IOException {
        setAuthor(din.readUTF());
        setCreatedAt(din.readLong());
        setVersion(din.readShort());
    }
}
