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

package dev.krud.world.engine.mantle;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.KrudWorldComplex;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.parallel.BurstExecutor;
import org.jetbrains.annotations.NotNull;

public interface MantleComponent extends Comparable<MantleComponent> {
    int getPriority();

    int getRadius();

    default KrudWorldData getData() {
        return getEngineMantle().getData();
    }

    default KrudWorldDimension getDimension() {
        return getEngineMantle().getEngine().getDimension();
    }

    default KrudWorldComplex getComplex() {
        return getEngineMantle().getComplex();
    }

    default long seed() {
        return getEngineMantle().getEngine().getSeedManager().getMantle();
    }

    default BurstExecutor burst() {
        return getEngineMantle().getEngine().burst().burst();
    }

    EngineMantle getEngineMantle();

    default Mantle getMantle() {
        return getEngineMantle().getMantle();
    }

    MantleFlag getFlag();

    boolean isEnabled();

    void setEnabled(boolean b);

    void hotload();

    @ChunkCoordinates
    void generateLayer(MantleWriter writer, int x, int z, ChunkContext context);

    @Override
    default int compareTo(@NotNull MantleComponent o) {
        return Integer.compare(getPriority(), o.getPriority());
    }
}
