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

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.util.collection.KMap;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public class KrudWorldSpawnerCooldowns {
    private final KMap<String, KrudWorldEngineSpawnerCooldown> cooldowns = new KMap<>();

    public KrudWorldEngineSpawnerCooldown getCooldown(@NonNull KrudWorldSpawner spawner) {
        return getCooldown(spawner.getLoadKey());
    }

    public KrudWorldEngineSpawnerCooldown getCooldown(@NonNull String loadKey) {
        return cooldowns.computeIfAbsent(loadKey, k -> {
            KrudWorldEngineSpawnerCooldown cd = new KrudWorldEngineSpawnerCooldown();
            cd.setSpawner(loadKey);
            return cd;
        });
    }

    public void cleanup(Engine engine) {
        cooldowns.values().removeIf(cd -> {
            KrudWorldSpawner sp = engine.getData().getSpawnerLoader().load(cd.getSpawner());
            return sp == null || cd.canSpawn(sp.getMaximumRate());
        });
    }

    public boolean isEmpty() {
        return cooldowns.isEmpty();
    }
}
