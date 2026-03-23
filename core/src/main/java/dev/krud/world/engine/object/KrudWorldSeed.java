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
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.math.RNG;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("color")
@Accessors(chain = true)
@NoArgsConstructor
@Desc("Represents a color")
@Data
public class KrudWorldSeed {
    @Desc("The seed to use")
    private long seed = 1337;

    @Desc("To calculate a seed KrudWorld passes in it's natural seed for the current feature, then mixes it with your seed. Setting this to true ignores the parent seed and always uses your exact seed ignoring the input of KrudWorld feature seeds. You can use this to match seeds on other generators.")
    private boolean ignoreNaturalSeedInput = false;

    public long getSeed(long seed) {
        return (seed * 47) + getSeed() + 29334667L;
    }

    public RNG rng(long inseed) {
        return new RNG(getSeed(inseed));
    }
}
