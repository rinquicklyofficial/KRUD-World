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

package dev.krud.world.util.math;

import dev.krud.world.engine.object.KrudWorldPosition;
import dev.krud.world.util.data.Cuboid;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

public class AxisAlignedBB {
    private final double xa;
    private final double xb;
    private final double ya;
    private final double yb;
    private final double za;
    private final double zb;

    public AxisAlignedBB(double xa, double xb, double ya, double yb, double za, double zb) {
        this.xa = xa;
        this.xb = xb;
        this.ya = ya;
        this.yb = yb;
        this.za = za;
        this.zb = zb;
    }

    public AxisAlignedBB(AlignedPoint a, AlignedPoint b) {
        this(a.getX(), b.getX(), a.getY(), b.getY(), a.getZ(), b.getZ());
    }

    public AxisAlignedBB(KrudWorldPosition a, KrudWorldPosition b) {
        this(a.getX(), b.getX(), a.getY(), b.getY(), a.getZ(), b.getZ());
    }

    public AxisAlignedBB shifted(KrudWorldPosition p) {
        return shifted(p.getX(), p.getY(), p.getZ());
    }

    public AxisAlignedBB shifted(double x, double y, double z) {
        return new AxisAlignedBB(min().add(new KrudWorldPosition((int) x, (int) y, (int) z)), max().add(new KrudWorldPosition((int) x, (int) y, (int) z)));
    }

    public boolean contains(AlignedPoint p) {
        return p.getX() >= xa && p.getX() <= xb && p.getY() >= ya && p.getZ() <= yb && p.getZ() >= za && p.getZ() <= zb;
    }

    public boolean contains(KrudWorldPosition p) {
        return p.getX() >= xa && p.getX() <= xb && p.getY() >= ya && p.getZ() <= yb && p.getZ() >= za && p.getZ() <= zb;
    }

    public boolean intersects(AxisAlignedBB s) {
        return this.xb >= s.xa && this.yb >= s.ya && this.zb >= s.za && s.xb >= this.xa && s.yb >= this.ya && s.zb >= this.za;
    }

    public KrudWorldPosition max() {
        return new KrudWorldPosition((int) xb, (int) yb, (int) zb);
    }


    public BlockVector maxbv() {
        return new BlockVector((int) xb, (int) yb, (int) zb);
    }

    public KrudWorldPosition min() {
        return new KrudWorldPosition((int) xa, (int) ya, (int) za);
    }

    public BlockVector minbv() {
        return new BlockVector((int) xa, (int) ya, (int) za);
    }

    public Cuboid toCuboid(World world) {
        return new Cuboid(min().toLocation(world), max().toLocation(world));
    }
}