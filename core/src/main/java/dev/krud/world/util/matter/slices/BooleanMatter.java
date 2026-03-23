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
import dev.krud.world.util.matter.Sliced;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class BooleanMatter extends RawMatter<Boolean> {
    public BooleanMatter() {
        this(1, 1, 1);
    }

    public BooleanMatter(int width, int height, int depth) {
        super(width, height, depth, Boolean.class);
    }

    @Override
    public Palette<Boolean> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(Boolean b, DataOutputStream dos) throws IOException {
        dos.writeBoolean(b);
    }

    @Override
    public Boolean readNode(DataInputStream din) throws IOException {
        return din.readBoolean();
    }
}
