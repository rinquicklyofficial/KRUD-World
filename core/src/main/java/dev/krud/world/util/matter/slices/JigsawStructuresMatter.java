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
import dev.krud.world.util.matter.slices.container.JigsawStructuresContainer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Sliced
public class JigsawStructuresMatter extends RawMatter<JigsawStructuresContainer> {
    public JigsawStructuresMatter() {
        this(1, 1, 1);
    }

    public JigsawStructuresMatter(int width, int height, int depth) {
        super(width, height, depth, JigsawStructuresContainer.class);
    }

    @Override
    public Palette<JigsawStructuresContainer> getGlobalPalette() {
        return null;
    }

    @Override
    public void writeNode(JigsawStructuresContainer b, DataOutputStream dos) throws IOException {
        b.write(dos);
    }

    @Override
    public JigsawStructuresContainer readNode(DataInputStream din) throws IOException {
        return new JigsawStructuresContainer(din);
    }
}
