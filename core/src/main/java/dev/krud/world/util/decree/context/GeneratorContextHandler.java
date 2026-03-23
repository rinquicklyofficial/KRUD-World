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

package dev.krud.world.util.decree.context;

import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldGenerator;
import dev.krud.world.util.decree.DecreeContextHandler;
import dev.krud.world.util.plugin.VolmitSender;

public class GeneratorContextHandler implements DecreeContextHandler<KrudWorldGenerator> {
    @Override
    public Class<KrudWorldGenerator> getType() {
        return KrudWorldGenerator.class;
    }

    @Override
    public KrudWorldGenerator handle(VolmitSender sender) {
        if (sender.isPlayer()
                && KrudWorldToolbelt.isKrudWorldWorld(sender.player().getWorld())
                && KrudWorldToolbelt.access(sender.player().getWorld()).getEngine() != null) {
            Engine engine = KrudWorldToolbelt.access(sender.player().getWorld()).getEngine();
            return engine.getData().getGeneratorLoader().load(engine.getBiome(sender.player().getLocation()).getGenerators().getRandom().getGenerator());
        }

        return null;
    }
}
