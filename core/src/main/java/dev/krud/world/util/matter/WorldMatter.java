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

package dev.krud.world.util.matter;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.TileData;
import dev.krud.world.util.data.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class WorldMatter {
    public static void placeMatter(Matter matter, Location at) {
        if (matter.hasSlice(BlockData.class)) {
            matter.slice(BlockData.class).writeInto(at);
        }

        if (matter.hasSlice(MatterEntityGroup.class)) {
            matter.slice(MatterEntityGroup.class).writeInto(at);
        }

        if (matter.hasSlice(TileWrapper.class)) {
            matter.slice(TileWrapper.class).writeInto(at);
        }
    }

    public static Matter createMatter(String author, Location a, Location b) {
        Cuboid c = new Cuboid(a, b);
        Matter s = new KrudWorldMatter(c.getSizeX(), c.getSizeY(), c.getSizeZ());
        KrudWorld.info(s.getWidth() + " " + s.getHeight() + " " + s.getDepth());
        s.getHeader().setAuthor(author);
        s.slice(BlockData.class).readFrom(c.getLowerNE());
        s.slice(MatterEntityGroup.class).readFrom(c.getLowerNE());
        s.slice(TileWrapper.class).readFrom(c.getLowerNE());
        s.trimSlices();

        return s;
    }
}
