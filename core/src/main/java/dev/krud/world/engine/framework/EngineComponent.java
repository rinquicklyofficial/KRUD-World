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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.KrudWorldComplex;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.math.RollingSequence;
import dev.krud.world.util.parallel.MultiBurst;
import org.bukkit.event.Listener;

public interface EngineComponent {
    Engine getEngine();

    RollingSequence getMetrics();

    String getName();

    default MultiBurst burst() {
        return getEngine().burst();
    }

    default void close() {
        try {
            if (this instanceof Listener) {
                KrudWorld.instance.unregisterListener((Listener) this);
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }
    }

    default KrudWorldData getData() {
        return getEngine().getData();
    }

    default EngineTarget getTarget() {
        return getEngine().getTarget();
    }

    default KrudWorldDimension getDimension() {
        return getEngine().getDimension();
    }

    default long getSeed() {
        return getEngine().getSeedManager().getComponent();
    }

    default int getParallelism() {
        return getEngine().getParallelism();
    }

    default KrudWorldComplex getComplex() {
        return getEngine().getComplex();
    }
}
