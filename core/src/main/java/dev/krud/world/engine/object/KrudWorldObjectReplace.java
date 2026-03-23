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
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Snippet("object-block-replacer")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Find and replace object materials")
@Data
public class KrudWorldObjectReplace {
    private final transient AtomicCache<CNG> replaceGen = new AtomicCache<>();
    private final transient AtomicCache<KList<BlockData>> findData = new AtomicCache<>();
    private final transient AtomicCache<KList<BlockData>> replaceData = new AtomicCache<>();
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Required
    @Desc("Find this block")
    private KList<KrudWorldBlockData> find = new KList<>();
    @Required
    @Desc("Replace it with this block palette")
    private KrudWorldMaterialPalette replace = new KrudWorldMaterialPalette();
    @Desc("Exactly match the block data or not")
    private boolean exact = false;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("Modifies the chance the block is replaced")
    private float chance = 1;

    public KList<BlockData> getFind(KrudWorldData rdata) {
        return findData.aquire(() ->
        {
            KList<BlockData> b = new KList<>();

            for (KrudWorldBlockData i : find) {
                BlockData bx = i.getBlockData(rdata);

                if (bx != null) {
                    b.add(bx);
                }
            }

            return b;
        });
    }

    public BlockData getReplace(RNG seed, double x, double y, double z, KrudWorldData rdata) {
        return getReplace().get(seed, x, y, z, rdata);
    }
}
