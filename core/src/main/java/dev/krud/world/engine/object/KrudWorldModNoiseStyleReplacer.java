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

@Snippet("noise-style-replacer")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A noise style replacer")
@Data
public class KrudWorldModNoiseStyleReplacer {
    @Required
    @Desc("A noise style to find")
    @ArrayType(type = String.class, min = 1)
    private NoiseStyle find = NoiseStyle.IRIS;

    @Required
    @Desc("If replaceTypeOnly is set to true, KrudWorld will keep the existing generator style and only replace the type itself. Otherwise it will use the replace tag for every style using the find type.")
    private boolean replaceTypeOnly = false;

    @Required
    @Desc("A noise style to replace it with")
    @RegistryListResource(KrudWorldBiome.class)
    private KrudWorldGeneratorStyle replace = new KrudWorldGeneratorStyle();
}
