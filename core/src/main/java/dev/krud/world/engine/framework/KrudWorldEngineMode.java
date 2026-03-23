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

public abstract class KrudWorldEngineMode implements EngineMode {
    private final Engine engine;
    private final KList<EngineStage> stages;
    private boolean closed;

    public KrudWorldEngineMode(Engine engine) {
        this.engine = engine;
        this.stages = new KList<>();
        this.closed = false;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        dump();
    }

    @Override
    public Engine getEngine() {
        return engine;
    }

    @Override
    public KList<EngineStage> getStages() {
        return stages;
    }

    @Override
    public void registerStage(EngineStage stage) {
        stages.add(stage);
    }
}
