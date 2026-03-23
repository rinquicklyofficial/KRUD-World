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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Material;

@Accessors(chain = true)
@NoArgsConstructor
@Desc("Find and replace object items for compatability")
@Data
public class KrudWorldCompatabilityItemFilter {
    private final transient AtomicCache<Material> findData = new AtomicCache<>(true);
    private final transient AtomicCache<Material> replaceData = new AtomicCache<>(true);
    @Required
    @Desc("When iris sees this block, and it's not reconized")
    private String when = "";
    @Required
    @Desc("Replace it with this block. Dont worry if this block is also not reconized, iris repeat this compat check.")
    private String supplement = "";

    public KrudWorldCompatabilityItemFilter(String when, String supplement) {
        this.when = when;
        this.supplement = supplement;
    }

    public Material getFind() {
        return findData.aquire(() -> B.getMaterial(when));
    }

    public Material getReplace() {
        return replaceData.aquire(() ->
        {
            Material b = B.getMaterialOrNull(supplement);

            if (b == null) {
                return null;
            }

            KrudWorld.verbose("Compat: Using " + supplement + " in place of " + when + " since this server doesnt support '" + supplement + "'");

            return b;
        });
    }
}
