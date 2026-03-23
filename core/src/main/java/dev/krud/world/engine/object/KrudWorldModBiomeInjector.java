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

@Snippet("biome-injector")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A biome injector")
@Data
public class KrudWorldModBiomeInjector {
    @Required
    @Desc("The region to find")
    @RegistryListResource(KrudWorldRegion.class)
    private String region = "";

    @Required
    @Desc("A biome to inject into the region")
    @RegistryListResource(KrudWorldBiome.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> inject = new KList<>();
}
