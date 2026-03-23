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

package dev.krud.world.engine.object.annotations.functions;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.engine.framework.ListFunction;
import dev.krud.world.util.collection.KList;

public class StructureKeyFunction implements ListFunction<KList<String>> {
    @Override
    public String key() {
        return "structure-key";
    }

    @Override
    public String fancyName() {
        return "Structure Key";
    }

    @Override
    public KList<String> apply(KrudWorldData irisData) {
        return INMS.get().getStructureKeys().removeWhere(t -> t.startsWith("#"));
    }
}
