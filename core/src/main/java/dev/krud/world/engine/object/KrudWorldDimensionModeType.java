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
import dev.krud.world.engine.framework.EngineMode;
import dev.krud.world.engine.mode.ModeEnclosure;
import dev.krud.world.engine.mode.ModeIslands;
import dev.krud.world.engine.mode.ModeOverworld;
import dev.krud.world.engine.mode.ModeSuperFlat;
import dev.krud.world.engine.object.annotations.Desc;

import java.util.function.Function;

@Desc("The type of dimension this is")
public enum KrudWorldDimensionModeType {
    @Desc("Typical dimensions. Has a fluid height, and all features of a biome based world")
    OVERWORLD(ModeOverworld::new),

    @Desc("Ultra fast, but very limited in features. Only supports terrain & biomes. No decorations, mobs, objects, or anything of the sort!")
    SUPERFLAT(ModeSuperFlat::new),

    @Desc("Like the nether, a ceiling & floor carved out")
    ENCLOSURE(ModeEnclosure::new),

    @Desc("Floating islands of terrain")
    ISLANDS(ModeIslands::new),
    ;
    private final Function<Engine, EngineMode> factory;

    KrudWorldDimensionModeType(Function<Engine, EngineMode> factory) {
        this.factory = factory;
    }

    public EngineMode create(Engine e) {
        return factory.apply(e);
    }
}
