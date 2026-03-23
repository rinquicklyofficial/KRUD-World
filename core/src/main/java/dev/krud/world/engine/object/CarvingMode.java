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

@Desc("Defines if an object is allowed to place in carvings, surfaces or both.")
public enum CarvingMode {
    @Desc("Only place this object on surfaces (NOT under carvings)")
    SURFACE_ONLY,

    @Desc("Only place this object under carvings (NOT on the surface)")
    CARVING_ONLY,

    @Desc("This object can place anywhere")
    ANYWHERE;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean supportsCarving() {
        return this.equals(ANYWHERE) || this.equals(CARVING_ONLY);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean supportsSurface() {
        return this.equals(ANYWHERE) || this.equals(SURFACE_ONLY);
    }
}
