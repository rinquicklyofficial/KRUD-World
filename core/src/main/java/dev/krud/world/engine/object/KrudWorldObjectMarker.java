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
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Snippet("object-marker")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Find blocks to mark")
@Data
public class KrudWorldObjectMarker {
    private final transient AtomicCache<KList<BlockData>> findData = new AtomicCache<>();
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Required
    @Desc("Find block types to mark")
    private KList<KrudWorldBlockData> mark = new KList<>();
    @MinNumber(1)
    @MaxNumber(16)
    @Desc("The maximum amount of markers to place. Use these sparingly!")
    private int maximumMarkers = 8;
    @Desc("If true, markers will only be placed if the block matches the mark list perfectly.")
    private boolean exact = false;
    @Required
    @RegistryListResource(KrudWorldMarker.class)
    @Desc("The marker to add")
    private String marker;

    public KList<BlockData> getMark(KrudWorldData rdata) {
        return findData.aquire(() ->
        {
            KList<BlockData> b = new KList<>();

            for (KrudWorldBlockData i : mark) {
                BlockData bx = i.getBlockData(rdata);

                if (bx != null) {
                    b.add(bx);
                }
            }

            return b;
        });
    }
}
