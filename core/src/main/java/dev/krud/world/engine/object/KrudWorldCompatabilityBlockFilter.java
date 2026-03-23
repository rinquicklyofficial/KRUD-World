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

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.util.data.B;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Find and replace object materials for compatability")
@Data
public class KrudWorldCompatabilityBlockFilter {
    private final transient AtomicCache<BlockData> findData = new AtomicCache<>(true);
    private final transient AtomicCache<BlockData> replaceData = new AtomicCache<>(true);
    @Required
    @Desc("When iris sees this block, and it's not reconized")
    private String when = "";
    @Required
    @Desc("Replace it with this block. Dont worry if this block is also not reconized, iris repeat this compat check.")
    private String supplement = "";
    @Desc("If exact is true, it compares block data for example minecraft:some_log[axis=x]")
    private boolean exact = false;

    public KrudWorldCompatabilityBlockFilter(String when, String supplement) {
        this(when, supplement, false);
    }

    public BlockData getFind() {
        return findData.aquire(() -> B.get(when));
    }

    public BlockData getReplace() {
        return replaceData.aquire(() ->
        {
            BlockData b = B.getOrNull(supplement, false);

            if (b == null) {
                return null;
            }

            KrudWorld.warn("Compat: Using '%s' in place of '%s' since this server doesnt support '%s'", supplement, when, when);

            return b;
        });
    }
}
