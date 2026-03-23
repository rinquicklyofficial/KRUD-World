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

package dev.krud.world.core.nms.datapack;

import dev.krud.world.engine.object.KrudWorldBiomeCustom;
import dev.krud.world.engine.object.KrudWorldDimensionTypeOptions;
import dev.krud.world.util.json.JSONObject;
import org.jetbrains.annotations.Nullable;

public interface IDataFixer {
    default JSONObject fixCustomBiome(KrudWorldBiomeCustom biome, JSONObject json) {
        return json;
    }

    JSONObject resolve(Dimension dimension, @Nullable KrudWorldDimensionTypeOptions options);

    void fixDimension(Dimension dimension, JSONObject json);

    default JSONObject createDimension(Dimension base, int minY, int height, int logicalHeight, @Nullable KrudWorldDimensionTypeOptions options) {
        JSONObject obj = resolve(base, options);
        obj.put("min_y", minY);
        obj.put("height", height);
        obj.put("logical_height", logicalHeight);
        fixDimension(base, obj);
        return obj;
    }

    enum Dimension {
        OVERWORLD,
        NETHER,
        END
    }
}
