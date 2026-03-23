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
import dev.krud.world.util.math.M;
import lombok.Data;

@Data
public class KrudWorldEngineSpawnerCooldown {
    private long lastSpawn = 0;
    private String spawner;

    public void spawn(Engine engine) {
        lastSpawn = M.ms();
    }

    public boolean canSpawn(KrudWorldRate s) {
        return M.ms() - lastSpawn > s.getInterval();
    }
}
