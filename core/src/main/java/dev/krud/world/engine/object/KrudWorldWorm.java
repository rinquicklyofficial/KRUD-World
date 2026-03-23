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
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Snippet("worm")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Generate worms")
@Data
public class KrudWorldWorm {
    @Desc("The style used to determine the curvature of this worm's x")
    private KrudWorldShapedGeneratorStyle xStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN, -2, 2);

    @Desc("The style used to determine the curvature of this worm's y")
    private KrudWorldShapedGeneratorStyle yStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN, -2, 2);

    @Desc("The style used to determine the curvature of this worm's z")
    private KrudWorldShapedGeneratorStyle zStyle = new KrudWorldShapedGeneratorStyle(NoiseStyle.PERLIN, -2, 2);

    @Desc("The max block distance this worm can travel from its start. This can have performance implications at ranges over 1,000 blocks but it's not too serious, test.")
    private int maxDistance = 128;

    @Desc("The iterations this worm can make")
    private int maxIterations = 512;

    @Desc("By default if a worm loops back into itself, it stops at that point and does not continue. This is an optimization, to prevent this turn this option on.")
    private boolean allowLoops = false;

    @Desc("The thickness of the worms. Each individual worm has the same thickness while traveling however, each spawned worm will vary in thickness.")
    private KrudWorldStyledRange girth = new KrudWorldStyledRange().setMin(3).setMax(5)
            .setStyle(new KrudWorldGeneratorStyle(NoiseStyle.PERLIN));

    public KList<KrudWorldPosition> generate(RNG rng, KrudWorldData data, MantleWriter writer, KrudWorldRange verticalRange, int x, int y, int z, boolean breakSurface, double distance) {
        int itr = maxIterations;
        double jx, jy, jz;
        double cx = x;
        double cy = y;
        double cz = z;
        KrudWorldPosition start = new KrudWorldPosition(x, y, z);
        KList<KrudWorldPosition> pos = new KList<>();
        KSet<KrudWorldPosition> check = allowLoops ? null : new KSet<>();
        CNG gx = xStyle.getGenerator().create(rng.nextParallelRNG(14567), data);
        CNG gy = yStyle.getGenerator().create(rng.nextParallelRNG(64789), data);
        CNG gz = zStyle.getGenerator().create(rng.nextParallelRNG(34790), data);

        while (itr-- > 0) {
            KrudWorldPosition current = new KrudWorldPosition(Math.round(cx), Math.round(cy), Math.round(cz));
            pos.add(current);

            if (check != null) {
                check.add(current);
            }

            jx = gx.fitDouble(xStyle.getMin(), xStyle.getMax(), cx, cy, cz);
            jy = gy.fitDouble(yStyle.getMin(), yStyle.getMax(), cx, cy, cz);
            jz = gz.fitDouble(zStyle.getMin(), zStyle.getMax(), cx, cy, cz);
            cx += jx;
            cy += jy;
            cz += jz;
            KrudWorldPosition next = new KrudWorldPosition(Math.round(cx), Math.round(cy), Math.round(cz));

            if (!breakSurface && writer.getEngineMantle().getHighest(next.getX(), next.getZ(), true) <= next.getY() + distance) {
                break;
            }

            if (verticalRange != null && !verticalRange.contains(next.getY())) {
                break;
            }

            if (!writer.isWithin((int) Math.round(cx), verticalRange != null ? (int) Math.round(cy) : 5, (int) Math.round(cz))) {
                break;
            }

            if (next.isLongerThan(start, maxDistance)) {
                break;
            }

            if (check != null && check.contains(next)) {
                break;
            }
        }

        return pos;
    }
}
