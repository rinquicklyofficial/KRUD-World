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
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.matter.Sliced;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class CavernMatter extends RawMatter<MatterCavern> {
    public static final MatterCavern EMPTY = new MatterCavern(false, "", (byte) 0);
    public static final MatterCavern BASIC = new MatterCavern(true, "", (byte) 0);

    public CavernMatter() {
        this(1, 1, 1);
    }

    public CavernMatter(int width, int height, int depth) {
        super(width, height, depth, MatterCavern.class);
    }

    public static MatterCavern get(String customBiome, int liquid) {
        return new MatterCavern(true, customBiome, (byte) liquid);
    }

    @Override
    public Palette<MatterCavern> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(MatterCavern b, DataOutputStream dos) throws IOException {
        dos.writeBoolean(b.isCavern());
        dos.writeUTF(b.getCustomBiome());
        dos.writeByte(b.getLiquid());
    }

    @Override
    public MatterCavern readNode(DataInputStream din) throws IOException {
        boolean b = din.readBoolean();
        String v = din.readUTF();
        byte l = din.readByte();

        return new MatterCavern(b, v, l);
    }
}
