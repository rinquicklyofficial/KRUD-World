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

package dev.krud.world.util.stream.interpolation;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.CaveResult;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.stream.ProceduralStream;
import org.bukkit.block.data.BlockData;

import java.util.UUID;
import java.util.function.Function;

public interface Interpolated<T> {
    Interpolated<BlockData> BLOCK_DATA = of((t) -> 0D, (t) -> null);
    Interpolated<KList<CaveResult>> CAVE_RESULTS = of((t) -> 0D, (t) -> null);
    Interpolated<RNG> RNG = of((t) -> 0D, (t) -> null);
    Interpolated<Double> DOUBLE = of((t) -> t, (t) -> t);
    Interpolated<Double[]> DOUBLE_ARRAY = of((t) -> 0D, (t) -> new Double[2]);
    Interpolated<Boolean> BOOLEAN = of((t) -> 0D, (t) -> false);
    Interpolated<Integer> INT = of(Double::valueOf, Double::intValue);
    Interpolated<Long> LONG = of(Double::valueOf, Double::longValue);
    Interpolated<UUID> UUID = of((i) -> Double.longBitsToDouble(i.getMostSignificantBits()), (i) -> new UUID(Double.doubleToLongBits(i), i.longValue()));

    static <T> Interpolated<T> of(Function<T, Double> a, Function<Double, T> b) {
        return new Interpolated<>() {
            @Override
            public double toDouble(T t) {
                return a.apply(t);
            }

            @Override
            public T fromDouble(double d) {
                return b.apply(d);
            }
        };
    }

    double toDouble(T t);

    T fromDouble(double d);

    default InterpolatorFactory<T> interpolate() {
        if (this instanceof ProceduralStream) {
            return new InterpolatorFactory<>((ProceduralStream<T>) this);
        }

        KrudWorld.warn("Cannot interpolate " + this.getClass().getCanonicalName() + "!");
        return null;
    }
}
