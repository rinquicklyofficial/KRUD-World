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

package dev.krud.world.util.nbt.mca.palette;

import dev.krud.world.util.nbt.tag.CompoundTag;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class MCAWrappedPalettedContainer<T> implements MCAPaletteAccess {
    private final MCAPalettedContainer<T> container;
    private final Function<T, CompoundTag> reader;
    private final Function<CompoundTag, T> writer;

    public void setBlock(int x, int y, int z, CompoundTag data) {
        container.set(x, y, z, writer.apply(data));
    }

    public CompoundTag getBlock(int x, int y, int z) {
        return reader.apply(container.get(x, y, z));
    }

    public void writeToSection(CompoundTag tag) {
        container.write(tag, "Palette", "BlockStates");
    }

    public void readFromSection(CompoundTag tag) {
        container.read(tag.getListTag("Palette"), tag.getLongArrayTag("BlockStates").getValue());
    }
}
