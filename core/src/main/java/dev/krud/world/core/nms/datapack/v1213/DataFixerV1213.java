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

package dev.krud.world.core.nms.datapack.v1213;

import dev.krud.world.core.nms.datapack.v1206.DataFixerV1206;
import dev.krud.world.engine.object.KrudWorldBiomeCustom;
import dev.krud.world.util.json.JSONArray;
import dev.krud.world.util.json.JSONObject;

public class DataFixerV1213 extends DataFixerV1206 {

    @Override
    public JSONObject fixCustomBiome(KrudWorldBiomeCustom biome, JSONObject json) {
        json = super.fixCustomBiome(biome, json);
        json.put("carvers", new JSONArray());
        return json;
    }
}
