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

package dev.krud.world.util.mantle.flag;

import org.jetbrains.annotations.NotNull;

record CustomFlag(String name, int ordinal) implements MantleFlag {

    @Override
    public @NotNull String toString() {
        return name;
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CustomFlag that))
            return false;
        return ordinal == that.ordinal;
    }

    @Override
    public int hashCode() {
        return ordinal;
    }
}
