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

package dev.krud.world.engine.object;

import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

@Snippet("position-3d")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a position")
@Data
public class KrudWorldPosition {
    @Desc("The x position")
    private int x = 0;

    @Desc("The y position")
    private int y = 0;

    @Desc("The z position")
    private int z = 0;

    public KrudWorldPosition(BlockVector bv) {
        this(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
    }

    public KrudWorldPosition(Location l) {
        this(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public KrudWorldPosition(Vector v) {
        this(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public KrudWorldPosition(double x, double y, double z) {
        this((int) x, (int) y, (int) z);
    }


    public KrudWorldPosition add(KrudWorldPosition relativePosition) {
        return new KrudWorldPosition(relativePosition.x + x, relativePosition.y + y, relativePosition.z + z);
    }

    public KrudWorldPosition sub(KrudWorldPosition relativePosition) {
        return new KrudWorldPosition(x - relativePosition.x, y - relativePosition.y, z - relativePosition.z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public KrudWorldPosition copy() {
        return new KrudWorldPosition(x, y, z);
    }

    @Override
    public String toString() {
        return "[" + getX() + "," + getY() + "," + getZ() + "]";
    }

    public boolean isLongerThan(KrudWorldPosition s, int maxLength) {
        return Math.abs(Math.pow(s.x - x, 2) + Math.pow(s.y - y, 2) + Math.pow(s.z - z, 2)) > maxLength * maxLength;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
}
