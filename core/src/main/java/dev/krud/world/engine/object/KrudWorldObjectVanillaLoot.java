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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.engine.object.annotations.functions.LootTableKeyFunction;
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Snippet("object-vanilla-loot")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents vanilla loot within this object or jigsaw piece")
@Data
public class KrudWorldObjectVanillaLoot implements IObjectLoot {
    private final transient AtomicCache<KList<BlockData>> filterCache = new AtomicCache<>();
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("The list of blocks this loot table should apply to")
    private KList<KrudWorldBlockData> filter = new KList<>();
    @Desc("Exactly match the block data or not")
    private boolean exact = false;
    @Desc("The vanilla loot table key")
    @Required
    @RegistryListFunction(LootTableKeyFunction.class)
    private String name;
    @Desc("The weight of this loot table being chosen")
    private int weight = 1;

    public KList<BlockData> getFilter(KrudWorldData rdata) {
        return filterCache.aquire(() ->
        {
            KList<BlockData> b = new KList<>();

            for (KrudWorldBlockData i : filter) {
                BlockData bx = i.getBlockData(rdata);

                if (bx != null) {
                    b.add(bx);
                }
            }

            return b;
        });
    }
}
