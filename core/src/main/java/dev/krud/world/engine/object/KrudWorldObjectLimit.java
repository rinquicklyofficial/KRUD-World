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

@Snippet("object-limit")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Translate objects")
@Data
public class KrudWorldObjectLimit {
    @MinNumber(0)
    @MaxNumber(1024)
    @Desc("The minimum height for placement (bottom of object)")
    private int minimumHeight = -2048; //TODO: WARNING HEIGHT

    @MinNumber(0)
    @MaxNumber(1024)
    @Desc("The maximum height for placement (top of object)")
    private int maximumHeight = 2048; //TODO: WARNING HEIGHT

    public boolean canPlace(int h, int l) {
        return h <= maximumHeight && l >= minimumHeight;
    }
}
