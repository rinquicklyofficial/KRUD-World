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

package dev.krud.world.util.data.palette;

import dev.krud.world.util.nbt.tag.CompoundTag;

public interface PaletteAccess {
    void setBlock(int x, int y, int z, CompoundTag data);

    CompoundTag getBlock(int x, int y, int z);

    void writeToSection(CompoundTag tag);

    void readFromSection(CompoundTag tag);
}
