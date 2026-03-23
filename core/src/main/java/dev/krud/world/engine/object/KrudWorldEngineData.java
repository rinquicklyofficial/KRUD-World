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

import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.util.collection.KMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KrudWorldEngineData extends KrudWorldSpawnerCooldowns {
    private KrudWorldEngineStatistics statistics = new KrudWorldEngineStatistics();
    private KMap<Long, KrudWorldSpawnerCooldowns> chunks = new KMap<>();
    private Long seed = null;

    public void removeChunk(int x, int z) {
        chunks.remove(Cache.key(x, z));
    }

    public KrudWorldSpawnerCooldowns getChunk(int x, int z) {
        return chunks.computeIfAbsent(Cache.key(x, z), k -> new KrudWorldSpawnerCooldowns());
    }

    public void cleanup(Engine engine) {
        super.cleanup(engine);

        chunks.values().removeIf(chunk -> {
            chunk.cleanup(engine);
            return chunk.isEmpty();
        });
    }
}
