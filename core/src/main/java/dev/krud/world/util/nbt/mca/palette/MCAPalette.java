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

import dev.krud.world.util.nbt.tag.ListTag;

import java.util.function.Predicate;

public interface MCAPalette<T> {
    int idFor(T paramT);

    boolean maybeHas(Predicate<T> paramPredicate);

    T valueFor(int paramInt);

    int getSize();

    void read(ListTag paramListTag);
}
