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
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("object-replacer")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A biome replacer")
@Data
public class KrudWorldModObjectReplacer {
    @Required
    @Desc("A list of objects to find")
    @RegistryListResource(KrudWorldObject.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> find = new KList<>();

    @Required
    @Desc("An object to replace it with")
    @RegistryListResource(KrudWorldBiome.class)
    private String replace = "";
}
