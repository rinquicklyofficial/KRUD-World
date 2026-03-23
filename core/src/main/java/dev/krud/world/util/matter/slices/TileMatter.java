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

import dev.krud.world.engine.object.TileData;
import dev.krud.world.util.data.palette.Palette;
import dev.krud.world.util.matter.Sliced;
import dev.krud.world.util.matter.TileWrapper;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("rawtypes")
@Sliced
public class TileMatter extends RawMatter<TileWrapper> {

    public TileMatter() {
        this(1, 1, 1);
    }

    public TileMatter(int width, int height, int depth) {
        super(width, height, depth, TileWrapper.class);
        registerWriter(World.class, (w, d, x, y, z) -> TileData.setTileState(w.getBlockAt(new Location(w, x, y, z)), d.getData()));
        registerReader(World.class, (w, x, y, z) -> new TileWrapper(TileData.getTileState(w.getBlockAt(new Location(w, x, y, z)), false)));
    }

    @Override
    public Palette<TileWrapper> getGlobalPalette() {
        return null;
    }

    public void writeNode(TileWrapper b, DataOutputStream dos) throws IOException {
        b.getData().toBinary(dos);
    }

    public TileWrapper readNode(DataInputStream din) throws IOException {
        return new TileWrapper(TileData.read(din));
    }
}
