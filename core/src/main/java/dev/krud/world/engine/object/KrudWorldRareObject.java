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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("rare-object")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a structure tile")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldRareObject {
    @Required
    @MinNumber(1)
    @Desc("The rarity is 1 in X")
    private int rarity = 1;

    @RegistryListResource(KrudWorldObject.class)
    @Required
    @Desc("The object to place if rarity check passed")
    private String object = "";
}
