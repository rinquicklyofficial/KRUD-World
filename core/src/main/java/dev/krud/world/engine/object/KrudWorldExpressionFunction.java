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

import com.dfsek.paralithic.functions.dynamic.Context;
import com.dfsek.paralithic.functions.dynamic.DynamicFunction;
import com.dfsek.paralithic.node.Statefulness;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.math.RNG;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Snippet("expression-function")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a function to use in your expression. Do not set the name to x, y, or z, also don't duplicate names.")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldExpressionFunction implements DynamicFunction {
    @Required
    @Desc("The function to assign this value to. Do not set the name to x, y, or z")
    private String name;

    @Desc("If defined, this variable will use a generator style as it's value")
    private KrudWorldGeneratorStyle styleValue = null;

    @Desc("If defined, iris will use an internal stream from the engine as it's value")
    private KrudWorldEngineStreamType engineStreamValue = null;

    @MinNumber(2)
    @Desc("Number of arguments for the function")
    private int args = 2;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient final KMap<FunctionContext, Provider> cache = new KMap<>();
    private transient KrudWorldData data;

    public boolean isValid() {
        return styleValue != null || engineStreamValue != null;
    }

    @Override
    public int getArgNumber() {
        if (engineStreamValue != null) return 2;
        return Math.max(args, 2);
    }

    @NotNull
    @Override
    public Statefulness statefulness() {
        return Statefulness.STATEFUL;
    }

    @Override
    public double eval(double... doubles) {
        return 0;
    }

    @Override
    public double eval(@Nullable Context raw, double... args) {
        return cache.computeIfAbsent((FunctionContext) raw, context -> {
            assert context != null;
            if (engineStreamValue != null) {
                var stream = engineStreamValue.get(data.getEngine());
                return d -> stream.get(d[0], d[1]);
            }

            if (styleValue != null) {
                return styleValue.createNoCache(context.rng, data)::noise;
            }

            return d -> Double.NaN;
        }).eval(args);
    }

    public record FunctionContext(@NonNull RNG rng) implements Context {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            FunctionContext that = (FunctionContext) o;
            return rng.getSeed() == that.rng.getSeed();
        }

        @Override
        public int hashCode() {
            return Long.hashCode(rng.getSeed());
        }
    }

    @FunctionalInterface
    private interface Provider {
        double eval(double... args);
    }
}
