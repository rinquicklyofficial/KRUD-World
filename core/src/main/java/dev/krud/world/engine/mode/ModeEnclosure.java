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

package dev.krud.world.engine.mode;

import dev.krud.world.engine.actuator.KrudWorldBiomeActuator;
import dev.krud.world.engine.actuator.KrudWorldTerrainNormalActuator;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineMode;
import dev.krud.world.engine.framework.KrudWorldEngineMode;

public class ModeEnclosure extends KrudWorldEngineMode implements EngineMode {
    public ModeEnclosure(Engine engine) {
        super(engine);
        var terrain = new KrudWorldTerrainNormalActuator(getEngine());
        var biome = new KrudWorldBiomeActuator(getEngine());

        registerStage(burst(
                (x, z, k, p, m, c) -> terrain.actuate(x, z, k, m, c),
                (x, z, k, p, m, c) -> biome.actuate(x, z, p, m, c)
        ));
    }
}
