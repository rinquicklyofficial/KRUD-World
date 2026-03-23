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

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.MaxNumber;
import dev.krud.world.engine.object.annotations.MinNumber;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("object-scale")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Scale objects")
@Data
public class KrudWorldObjectScale {
    private static ConcurrentLinkedHashMap<KrudWorldObject, KList<KrudWorldObject>> cache
            = new ConcurrentLinkedHashMap.Builder<KrudWorldObject, KList<KrudWorldObject>>()
            .initialCapacity(64)
            .maximumWeightedCapacity(1024)
            .concurrencyLevel(32)
            .build();
    @MinNumber(1)
    @MaxNumber(32)
    @Desc("KrudWorld Objects are scaled and cached to speed up placements. Because of this extra memory is used, so we evenly distribute variations across the defined scale range, then pick one randomly. If the differences is small, use a lower number. For more possibilities on the scale spectrum, increase this at the cost of memory.")
    private int variations = 7;
    @MinNumber(0.01)
    @MaxNumber(50)
    @Desc("The minimum scale")
    private double minimumScale = 1;
    @MinNumber(0.01)
    @MaxNumber(50)
    @Desc("The maximum height for placement (top of object)")
    private double maximumScale = 1;
    @Desc("If this object is scaled up beyond its origin size, specify a 3D interpolator")
    private KrudWorldObjectPlacementScaleInterpolator interpolation = KrudWorldObjectPlacementScaleInterpolator.NONE;

    public boolean shouldScale() {
        return ((minimumScale == maximumScale) && maximumScale == 1) || variations <= 0;
    }

    public int getMaxSizeFor(int indim) {
        return (int) (getMaxScale() * indim);
    }

    public double getMaxScale() {
        double mx = 0;

        for (double i = minimumScale; i < maximumScale; i += (maximumScale - minimumScale) / (double) (Math.min(variations, 32))) {
            mx = i;
        }

        return mx;
    }

    public KrudWorldObject get(RNG rng, KrudWorldObject origin) {
        if (shouldScale()) {
            return origin;
        }

        return cache.computeIfAbsent(origin, (k) -> {
            KList<KrudWorldObject> c = new KList<>();
            for (double i = minimumScale; i < maximumScale; i += (maximumScale - minimumScale) / (double) (Math.min(variations, 32))) {
                c.add(origin.scaled(i, getInterpolation()));
            }

            return c;
        }).getRandom(rng);
    }

    public boolean canScaleBeyond() {
        return shouldScale() && maximumScale > 1;
    }
}
