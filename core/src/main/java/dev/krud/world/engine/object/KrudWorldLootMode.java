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

@Desc("A loot mode is used to describe what to do with the existing loot layers before adding this loot. Using ADD will simply add this table to the building list of tables (i.e. add dimension tables, region tables then biome tables). By using clear or replace, you remove the parent tables before and add just your tables.")
public enum KrudWorldLootMode {
    @Desc("Add to the existing parent loot tables")
    ADD,
    @Desc("Clear all loot tables then add this table")
    CLEAR,
    @Desc("Replace all loot tables with this table (same as clear)")
    REPLACE,
    @Desc("Only use when there was no loot table defined by an object")
    FALLBACK
}
