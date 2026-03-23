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

package dev.krud.world.util.matter.slices;

import dev.krud.world.util.data.palette.Palette;
import dev.krud.world.util.matter.MatterStructurePOI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StructurePOIMatter extends RawMatter<MatterStructurePOI> {

    public StructurePOIMatter() {
        super(1, 1, 1, MatterStructurePOI.class);
    }

    @Override
    public Palette<MatterStructurePOI> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(MatterStructurePOI b, DataOutputStream dos) throws IOException {
        dos.writeUTF(b.getType());
    }

    @Override
    public MatterStructurePOI readNode(DataInputStream din) throws IOException {
        return MatterStructurePOI.get(din.readUTF());
    }
}
