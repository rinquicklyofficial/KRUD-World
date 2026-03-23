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

import dev.krud.world.util.data.B;
import dev.krud.world.util.data.KrudWorldCustomData;
import dev.krud.world.util.data.palette.Palette;
import dev.krud.world.util.matter.Sliced;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class BlockMatter extends RawMatter<BlockData> {
    public static final BlockData AIR = Material.AIR.createBlockData();

    public BlockMatter() {
        this(1, 1, 1);
    }

    public BlockMatter(int width, int height, int depth) {
        super(width, height, depth, BlockData.class);
        registerWriter(World.class, ((w, d, x, y, z) -> {
            if (d instanceof KrudWorldCustomData c)
                w.getBlockAt(x, y, z).setBlockData(c.getBase());
            else w.getBlockAt(x, y, z).setBlockData(d);
        }));
        registerReader(World.class, (w, x, y, z) -> {
            BlockData d = w.getBlockAt(x, y, z).getBlockData();
            return d.getMaterial().isAir() ? null : d;
        });
    }

    @Override
    public Palette<BlockData> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(BlockData b, DataOutputStream dos) throws IOException {
        dos.writeUTF(b.getAsString(true));
    }

    @Override
    public BlockData readNode(DataInputStream din) throws IOException {
        return B.get(din.readUTF());
    }
}
