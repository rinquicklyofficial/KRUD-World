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
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.util.stream.ProceduralStream;

import java.util.function.Function;

@Desc("Represents a stream from the engine")
public enum KrudWorldEngineStreamType {
    @Desc("Represents the given slope at the x, z coordinates")
    SLOPE((f) -> f.getComplex().getSlopeStream()),

    @Desc("Represents the base generator height at the given position. This includes only the biome generators / interpolation and noise features but does not include carving, caves.")
    HEIGHT((f) -> f.getComplex().getHeightStream()),

    @Desc("Represents the base generator height at the given position. This includes only the biome generators / interpolation and noise features but does not include carving, caves. with Max(height, fluidHeight).")
    HEIGHT_OR_FLUID((f) -> f.getComplex().getHeightFluidStream()),

    @Desc("Represents the overlay noise generators summed (dimension setting)")
    OVERLAY_NOISE((f) -> f.getComplex().getOverlayStream()),

    @Desc("Represents the noise style of regions")
    REGION_STYLE((f) -> f.getComplex().getRegionStyleStream()),

    @Desc("Represents the identity of regions. Each region has a unique number (very large numbers)")
    REGION_IDENTITY((f) -> f.getComplex().getRegionIdentityStream());

    private final Function<Engine, ProceduralStream<Double>> getter;

    KrudWorldEngineStreamType(Function<Engine, ProceduralStream<Double>> getter) {
        this.getter = getter;
    }

    public ProceduralStream<Double> get(Engine engine) {
        return getter.apply(engine);
    }
}
