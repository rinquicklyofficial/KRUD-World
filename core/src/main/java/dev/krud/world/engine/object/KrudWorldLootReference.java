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

import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.DataProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("loot-registry")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a loot entry")
@Data
public class KrudWorldLootReference {
    private final transient AtomicCache<KList<KrudWorldLootTable>> tt = new AtomicCache<>();
    @Desc("Add = add on top of parent tables, Replace = clear first then add these. Clear = Remove all and dont add loot from this or parent.")
    private KrudWorldLootMode mode = KrudWorldLootMode.ADD;
    @RegistryListResource(KrudWorldLootTable.class)
    @ArrayType(min = 1, type = String.class)
    @Desc("Add loot table registries here")
    private KList<String> tables = new KList<>();
    @MinNumber(0)
    @Desc("Increase the chance of loot in this area")
    private double multiplier = 1D;

    public KList<KrudWorldLootTable> getLootTables(DataProvider g) {
        return tt.aquire(() ->
        {
            KList<KrudWorldLootTable> t = new KList<>();

            for (String i : tables) {
                t.add(g.getData().getLootLoader().load(i));
            }

            return t;
        });
    }
}
