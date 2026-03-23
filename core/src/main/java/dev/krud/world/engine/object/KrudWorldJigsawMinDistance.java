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
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.RegistryListResource;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("jigsaw-structure-min-distance")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents the min distance between jigsaw structure placements")
@Data
public class KrudWorldJigsawMinDistance {
    @Required
    @RegistryListResource(KrudWorldJigsawStructure.class)
    @Desc("The structure to check against")
    private String structure;

    @Required
    @MinNumber(0)
    @Desc("The min distance in blocks to a placed structure\nWARNING: The performance impact scales exponentially!")
    private int distance;
}
