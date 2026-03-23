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
import dev.krud.world.engine.actuator.KrudWorldDecorantActuator;
import dev.krud.world.engine.actuator.KrudWorldTerrainNormalActuator;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineMode;
import dev.krud.world.engine.framework.EngineStage;
import dev.krud.world.engine.framework.KrudWorldEngineMode;
import dev.krud.world.engine.modifier.*;
import org.bukkit.block.data.BlockData;

public class ModeOverworld extends KrudWorldEngineMode implements EngineMode {
    public ModeOverworld(Engine engine) {
        super(engine);
        var terrain = new KrudWorldTerrainNormalActuator(getEngine());
        var biome = new KrudWorldBiomeActuator(getEngine());
        var decorant = new KrudWorldDecorantActuator(getEngine());
        var cave = new KrudWorldCarveModifier(getEngine());
        var post = new KrudWorldPostModifier(getEngine());
        var deposit = new KrudWorldDepositModifier(getEngine());
        var perfection = new KrudWorldPerfectionModifier(getEngine());
        var custom = new KrudWorldCustomModifier(getEngine());
        EngineStage sBiome = (x, z, k, p, m, c) -> biome.actuate(x, z, p, m, c);
        EngineStage sGenMatter = (x, z, k, p, m, c) -> generateMatter(x >> 4, z >> 4, m, c);
        EngineStage sTerrain = (x, z, k, p, m, c) -> terrain.actuate(x, z, k, m, c);
        EngineStage sDecorant = (x, z, k, p, m, c) -> decorant.actuate(x, z, k, m, c);
        EngineStage sCave = (x, z, k, p, m, c) -> cave.modify(x >> 4, z >> 4, k, m, c);
        EngineStage sDeposit = (x, z, k, p, m, c) -> deposit.modify(x, z, k, m, c);
        EngineStage sPost = (x, z, k, p, m, c) -> post.modify(x, z, k, m, c);
        EngineStage sInsertMatter = (x, z, K, p, m, c) -> getMantle().insertMatter(x >> 4, z >> 4, BlockData.class, K, m);
        EngineStage sPerfection = (x, z, k, p, m, c) -> perfection.modify(x, z, k, m, c);
        EngineStage sCustom = (x, z, k, p, m, c) -> custom.modify(x, z, k, m, c);

        registerStage(burst(
                sGenMatter,
                sTerrain
        ));
        registerStage(burst(
                sCave,
                sPost
        ));
        registerStage(burst(
                sDeposit,
                sInsertMatter,
                sDecorant
        ));
        registerStage(sPerfection);
        registerStage(sCustom);
    }
}
