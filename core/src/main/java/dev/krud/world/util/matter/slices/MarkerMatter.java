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

import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.palette.Palette;
import dev.krud.world.util.matter.MatterMarker;
import dev.krud.world.util.matter.Sliced;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class MarkerMatter extends RawMatter<MatterMarker> {
    public static final MatterMarker NONE = new MatterMarker("none");
    public static final MatterMarker CAVE_FLOOR = new MatterMarker("cave_floor");
    public static final MatterMarker CAVE_CEILING = new MatterMarker("cave_ceiling");
    private static final KMap<String, MatterMarker> markers = new KMap<>();

    public MarkerMatter() {
        this(1, 1, 1);
    }

    public MarkerMatter(int width, int height, int depth) {
        super(width, height, depth, MatterMarker.class);
    }

    @Override
    public Palette<MatterMarker> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(MatterMarker b, DataOutputStream dos) throws IOException {
        dos.writeUTF(b.getTag());
    }

    @Override
    public MatterMarker readNode(DataInputStream din) throws IOException {
        return markers.computeIfAbsent(din.readUTF(), MatterMarker::new);
    }
}
