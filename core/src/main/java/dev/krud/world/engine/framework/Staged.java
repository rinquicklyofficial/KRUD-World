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

package dev.krud.world.engine.framework;

import dev.krud.world.util.collection.KList;

public interface Staged {
    KList<EngineStage> getStages();

    void registerStage(EngineStage stage);

    default void dump() {
        getStages().forEach(EngineStage::close);
        getStages().clear();
    }
}
