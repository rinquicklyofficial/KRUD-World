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

package dev.krud.world.engine.object.annotations.functions;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.ListFunction;
import dev.krud.world.util.collection.KList;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LootTableKeyFunction implements ListFunction<KList<String>> {
    @Override
    public String key() {
        return "loot-table-key";
    }

    @Override
    public String fancyName() {
        return "LootTable Key";
    }

    @Override
    public KList<String> apply(KrudWorldData data) {
        return StreamSupport.stream(Registry.LOOT_TABLES.spliterator(), false)
                .map(LootTables::getLootTable)
                .map(LootTable::getKey)
                .map(NamespacedKey::toString)
                .collect(Collectors.toCollection(KList::new));
    }
}
