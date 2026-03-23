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
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("cave-shape")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Cave Shape")
@Data
public class KrudWorldCaveShape {
    private transient final KMap<KrudWorldPosition, KSet<KrudWorldPosition>> cache = new KMap<>();

    @Desc("Noise used for the shape of the cave")
    private KrudWorldGeneratorStyle noise = new KrudWorldGeneratorStyle();
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The threshold for noise mask")
    private double noiseThreshold = -1;

    @RegistryListResource(KrudWorldObject.class)
    @Desc("Object used as mask for the shape of the cave")
    private String object = null;
    @Desc("Rotation to apply to objects before using them as mask")
    private KrudWorldObjectRotation objectRotation = new KrudWorldObjectRotation();

    public CNG getNoise(RNG rng, Engine engine) {
        return noise.create(rng, engine.getData());
    }

    public KSet<KrudWorldPosition> getMasked(RNG rng, Engine engine) {
        if (object == null) return null;
        return cache.computeIfAbsent(randomRotation(rng), pos -> {
            var rotated = new KSet<KrudWorldPosition>();
            engine.getData().getObjectLoader().load(object).getBlocks().forEach((vector, data) -> {
                if (data.getMaterial().isAir()) return;
                rotated.add(new KrudWorldPosition(objectRotation.rotate(vector, pos.getX(), pos.getY(), pos.getZ())));
            });
            return rotated;
        });
    }

    private KrudWorldPosition randomRotation(RNG rng) {
        if (objectRotation == null || !objectRotation.canRotate())
            return new KrudWorldPosition(0,0,0);
        return new KrudWorldPosition(
                randomDegree(rng, objectRotation.getXAxis()),
                randomDegree(rng, objectRotation.getYAxis()),
                randomDegree(rng, objectRotation.getZAxis())
        );
    }

    private int randomDegree(RNG rng, KrudWorldAxisRotationClamp clamp) {
        if (!clamp.isEnabled()) return 0;
        if (clamp.isLocked()) return (int) clamp.getMax();
        double interval = clamp.getInterval();
        if (interval < 1) interval = 1;

        double min = clamp.getMin(), max = clamp.getMax();
        double value = (interval * (Math.ceil(Math.abs(rng.d(0, 360) / interval)))) % 360D;
        if (clamp.isUnlimited()) return (int) value;

        if (min > max) {
            max = clamp.getMin();
            min = clamp.getMax();
        }
        return (int) (double) M.clip(value, min, max);
    }
}
