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
import dev.krud.world.engine.object.annotations.MaxNumber;
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("stilt-settings")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Defines stilting behaviour.")
@Data
public class KrudWorldStiltSettings {
    @MinNumber(0)
    @MaxNumber(64)
    @Desc("Defines the maximum amount of blocks the object stilts verticially before overstilting and randomRange.")
    private int yMax;
    @MinNumber(0)
    @MaxNumber(64)
    @Desc("Defines the upper boundary for additional blocks after overstilting and/or maxStiltRange.")
    private int yRand;
    @MaxNumber(64)
    @MinNumber(0)
    @Desc("If the place mode is set to stilt, you can over-stilt it even further into the ground. Especially useful when using fast stilt due to inaccuracies.")
    private int overStilt;
    @Desc("If defined, stilting will be done using this block palette rather than the last layer of the object.")
    private KrudWorldMaterialPalette palette;

}
