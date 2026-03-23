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

@SuppressWarnings("EmptyMethod")
public class Spiraler {
    private final Spiraled spiraled;
    int x, z, dx, dz, sizeX, sizeZ, t, maxI, i;
    int ox, oz;

    public Spiraler(int sizeX, int sizeZ, Spiraled spiraled) {
        ox = 0;
        oz = 0;
        this.spiraled = spiraled;
        retarget(sizeX, sizeZ);
    }

    static void Spiral(int X, int Y) {

    }

    public void drain() {
        while (hasNext()) {
            next();
        }
    }

    public Spiraler setOffset(int ox, int oz) {
        this.ox = ox;
        this.oz = oz;
        return this;
    }

    public void retarget(int sizeX, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        x = z = dx = 0;
        dz = -1;
        i = 0;
        t = Math.max(sizeX, sizeZ);
        maxI = t * t;
    }

    public boolean hasNext() {
        return i < maxI;
    }

    public void next() {
        if ((-sizeX / 2 <= x) && (x <= sizeX / 2) && (-sizeZ / 2 <= z) && (z <= sizeZ / 2)) {
            spiraled.on(x + ox, z + oz);
        }

        if ((x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1 - z))) {
            t = dx;
            dx = -dz;
            dz = t;
        }
        x += dx;
        z += dz;
        i++;
    }

    public int count() {
        int c = 0;
        while (hasNext()) {
            next();
            c++;
        }

        return c;
    }
}
