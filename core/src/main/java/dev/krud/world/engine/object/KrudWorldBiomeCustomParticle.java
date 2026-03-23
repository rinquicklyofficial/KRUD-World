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
import org.bukkit.Particle;

@Snippet("custom-biome-particle")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A custom biome ambient particle")
@Data
public class KrudWorldBiomeCustomParticle {
    @Required
    @Desc("The biome's particle type")
    private Particle particle = Particle.FLASH;

    @MinNumber(1)
    @MaxNumber(10000)
    @Desc("The rarity")
    private int rarity = 35;
}
