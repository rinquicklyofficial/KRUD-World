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

package dev.krud.world.core.nms.datapack.v1206;

import dev.krud.world.core.nms.datapack.v1192.DataFixerV1192;
import dev.krud.world.engine.object.KrudWorldBiomeCustom;
import dev.krud.world.engine.object.KrudWorldBiomeCustomSpawn;
import dev.krud.world.engine.object.KrudWorldBiomeCustomSpawnType;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.json.JSONArray;
import dev.krud.world.util.json.JSONObject;

import java.util.Locale;

public class DataFixerV1206 extends DataFixerV1192 {
    @Override
    public JSONObject fixCustomBiome(KrudWorldBiomeCustom biome, JSONObject json) {
        int spawnRarity = biome.getSpawnRarity();
        if (spawnRarity > 0) {
            json.put("creature_spawn_probability", Math.min(spawnRarity/20d, 0.9999999));
        } else {
            json.remove("creature_spawn_probability");
        }

        var spawns = biome.getSpawns();
        if (spawns != null && spawns.isNotEmpty()) {
            JSONObject spawners = new JSONObject();
            KMap<KrudWorldBiomeCustomSpawnType, JSONArray> groups = new KMap<>();

            for (KrudWorldBiomeCustomSpawn i : spawns) {
                JSONArray g = groups.computeIfAbsent(i.getGroup(), (k) -> new JSONArray());
                JSONObject o = new JSONObject();
                o.put("type", i.getType().getKey());
                o.put("weight", i.getWeight());
                o.put("minCount", i.getMinCount());
                o.put("maxCount", i.getMaxCount());
                g.put(o);
            }

            for (KrudWorldBiomeCustomSpawnType i : groups.k()) {
                spawners.put(i.name().toLowerCase(Locale.ROOT), groups.get(i));
            }

            json.put("spawners", spawners);
        }
        return json;
    }

    @Override
    public void fixDimension(Dimension dimension, JSONObject json) {
        super.fixDimension(dimension, json);
        if (!(json.get("monster_spawn_light_level") instanceof JSONObject lightLevel))
            return;
        var value = (JSONObject) lightLevel.remove("value");
        lightLevel.put("max_inclusive", value.get("max_inclusive"));
        lightLevel.put("min_inclusive", value.get("min_inclusive"));
    }
}
