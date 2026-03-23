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

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Vector3i extends BlockVector {
    public Vector3i(int x, int y, int z) {
        super(x, y, z);
    }

    public Vector3i(Vector vec) {
        super(vec);
    }

    @NotNull
    @Override
    public Vector3i clone() {
        return (Vector3i) super.clone();
    }

    @Override
    public int hashCode() {
        return (int) x ^ ((int) z << 12) ^ ((int) y << 24);
    }
}
