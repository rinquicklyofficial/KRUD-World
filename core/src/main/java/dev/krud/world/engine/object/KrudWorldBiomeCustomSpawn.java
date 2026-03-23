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

import dev.krud.world.engine.object.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.entity.EntityType;

@Snippet("custom-biome-spawn")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A custom biome spawn")
@Data
public class KrudWorldBiomeCustomSpawn {
    @Required
    @Desc("The biome's entity type")
    private EntityType type = EntityType.COW;

    @MinNumber(1)
    @Desc("The min to spawn")
    private int minCount = 2;

    @MinNumber(1)
    @Desc("The max to spawn")
    private int maxCount = 5;

    @MinNumber(1)
    @MaxNumber(1000)
    @Desc("The weight in this group. Higher weight, the more common this type is spawned")
    private int weight = 1;

    @Desc("The rarity")
    private KrudWorldBiomeCustomSpawnType group = KrudWorldBiomeCustomSpawnType.MISC;
}
