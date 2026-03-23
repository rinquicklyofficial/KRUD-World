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
import dev.krud.world.engine.object.annotations.functions.ResourceLoadersFunction;
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Desc("Represents global preprocessors")
public class KrudWorldPreProcessors {
    @Required
    @Desc("The preprocessor type")
    @RegistryListFunction(ResourceLoadersFunction.class)
    private String type = "dimension";

    @Required
    @Desc("The preprocessor scripts\nFile extension: .proc.kts")
    @RegistryListResource(KrudWorldScript.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> scripts = new KList<>();
}
