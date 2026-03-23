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
import dev.krud.world.util.nbt.io.NBTUtil;
import dev.krud.world.util.nbt.tag.Tag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NBTMatter<T extends Tag<?>> extends RawMatter<T> {
    public NBTMatter(int width, int height, int depth, Class<T> c, T e) {
        super(width, height, depth, c);
    }

    @Override
    public Palette<T> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(T b, DataOutputStream dos) throws IOException {
        NBTUtil.write(b, dos, false);
    }

    @Override
    public T readNode(DataInputStream din) throws IOException {
        return (T) NBTUtil.read(din, false).getTag();
    }
}
