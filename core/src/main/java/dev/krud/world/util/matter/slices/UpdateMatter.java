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

import dev.krud.world.util.data.palette.GlobalPalette;
import dev.krud.world.util.data.palette.Palette;
import dev.krud.world.util.matter.MatterUpdate;
import dev.krud.world.util.matter.Sliced;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class UpdateMatter extends RawMatter<MatterUpdate> {
    public static final MatterUpdate ON = new MatterUpdate(true);
    public static final MatterUpdate OFF = new MatterUpdate(false);
    private static final Palette<MatterUpdate> GLOBAL = new GlobalPalette<>(OFF, ON);

    public UpdateMatter() {
        this(1, 1, 1);
    }

    public UpdateMatter(int width, int height, int depth) {
        super(width, height, depth, MatterUpdate.class);
    }

    @Override
    public Palette<MatterUpdate> getGlobalPalette() {
        return GLOBAL;
    }

    @Override
    public void writeNode(MatterUpdate b, DataOutputStream dos) throws IOException {
        dos.writeBoolean(b.isUpdate());
    }

    @Override
    public MatterUpdate readNode(DataInputStream din) throws IOException {
        return din.readBoolean() ? ON : OFF;
    }
}
