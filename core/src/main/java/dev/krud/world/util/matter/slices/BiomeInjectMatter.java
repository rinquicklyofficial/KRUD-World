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
import dev.krud.world.util.matter.MatterBiomeInject;
import dev.krud.world.util.matter.Sliced;
import org.bukkit.block.Biome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class BiomeInjectMatter extends RawMatter<MatterBiomeInject> {
    public BiomeInjectMatter() {
        this(1, 1, 1);
    }

    public BiomeInjectMatter(int width, int height, int depth) {
        super(width, height, depth, MatterBiomeInject.class);
    }

    public static MatterBiomeInject get(Biome biome) {
        return get(false, 0, biome);
    }

    public static MatterBiomeInject get(int customBiome) {
        return get(true, customBiome, null);
    }

    public static MatterBiomeInject get(boolean custom, int customBiome, Biome biome) {
        return new MatterBiomeInject(custom, customBiome, biome);
    }

    @Override
    public Palette<MatterBiomeInject> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(MatterBiomeInject b, DataOutputStream dos) throws IOException {
        dos.writeBoolean(b.isCustom());

        if (b.isCustom()) {
            dos.writeShort(b.getBiomeId());
        } else {
            dos.writeByte(b.getBiome().ordinal());
        }
    }

    @Override
    public MatterBiomeInject readNode(DataInputStream din) throws IOException {
        boolean b = din.readBoolean();
        int id = b ? din.readShort() : 0;
        Biome biome = !b ? Biome.values()[din.readByte()] : Biome.PLAINS;

        return new MatterBiomeInject(b, id, biome);
    }
}
