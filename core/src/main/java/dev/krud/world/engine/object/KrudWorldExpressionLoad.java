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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.stream.ProceduralStream;
import lombok.*;
import lombok.experimental.Accessors;

@Snippet("expression-load")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a variable to use in your expression. Do not set the name to x, y, or z, also don't duplicate names.")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldExpressionLoad {
    @Required
    @Desc("The variable to assign this value to. Do not set the name to x, y, or z")
    private String name = "";

    @Desc("If the style value is not defined, this value will be used")
    private double staticValue = -1;

    @Desc("If defined, this variable will use a generator style as it's value")
    private KrudWorldGeneratorStyle styleValue = null;

    @Desc("If defined, iris will use an internal stream from the engine as it's value")
    private KrudWorldEngineStreamType engineStreamValue = null;

    @Desc("If defined, iris will use an internal value from the engine as it's value")
    private KrudWorldEngineValueType engineValue = null;

    private transient AtomicCache<ProceduralStream<Double>> streamCache = new AtomicCache<>();
    private transient AtomicCache<Double> valueCache = new AtomicCache<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient final KMap<Long, CNG> styleCache = new KMap<>();

    public double getValue(RNG rng, KrudWorldData data, double x, double z) {
        if (engineValue != null) {
            return valueCache.aquire(() -> engineValue.get(data.getEngine()));
        }

        if (engineStreamValue != null) {
            return streamCache.aquire(() -> engineStreamValue.get(data.getEngine())).get(x, z);
        }

        if (styleValue != null) {
            return styleCache.computeIfAbsent(rng.getSeed(), k -> styleValue.createNoCache(new RNG(k), data))
                    .noise(x, z);
        }

        return staticValue;
    }

    public double getValue(RNG rng, KrudWorldData data, double x, double y, double z) {
        if (engineValue != null) {
            return valueCache.aquire(() -> engineValue.get(data.getEngine()));
        }

        if (engineStreamValue != null) {
            return streamCache.aquire(() -> engineStreamValue.get(data.getEngine())).get(x, z);
        }

        if (styleValue != null) {
            return styleCache.computeIfAbsent(rng.getSeed(), k -> styleValue.createNoCache(new RNG(k), data))
                    .noise(x, y, z);
        }

        return staticValue;
    }
}
