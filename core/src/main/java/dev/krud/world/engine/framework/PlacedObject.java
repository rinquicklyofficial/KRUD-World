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

package dev.krud.world.engine.framework;

import dev.krud.world.engine.object.KrudWorldObject;
import dev.krud.world.engine.object.KrudWorldObjectPlacement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class PlacedObject {
    @Nullable
    private KrudWorldObjectPlacement placement;
    @Nullable
    private KrudWorldObject object;
    private int id;
    private int xx;
    private int zz;
}
