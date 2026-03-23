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
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.mantle.MantleWriter;
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("carving")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a carving configuration")
@Data
public class KrudWorldCarving {
    @ArrayType(type = KrudWorldCavePlacer.class, min = 1)
    @Desc("Define cave placers")
    private KList<KrudWorldCavePlacer> caves = new KList<>();

    @ArrayType(type = KrudWorldRavinePlacer.class, min = 1)
    @Desc("Define ravine placers")
    private KList<KrudWorldRavinePlacer> ravines = new KList<>();

    @ArrayType(type = KrudWorldElipsoid.class, min = 1)
    @Desc("Define elipsoids")
    private KList<KrudWorldElipsoid> elipsoids = new KList<>();

    @ArrayType(type = KrudWorldSphere.class, min = 1)
    @Desc("Define spheres")
    private KList<KrudWorldSphere> spheres = new KList<>();

    @ArrayType(type = KrudWorldPyramid.class, min = 1)
    @Desc("Define pyramids")
    private KList<KrudWorldPyramid> pyramids = new KList<>();


    @BlockCoordinates
    public void doCarving(MantleWriter writer, RNG rng, Engine engine, int x, int y, int z, int depth) {
        doCarving(writer, rng, new RNG(engine.getSeedManager().getCarve()), engine, x, y, z, depth, -1);
    }

    @BlockCoordinates
    public void doCarving(MantleWriter writer, RNG rng, RNG base, Engine engine, int x, int y, int z, int recursion, int waterHint) {
        int nextRecursion = recursion + 1;

        if (caves.isNotEmpty()) {
            for (KrudWorldCavePlacer i : caves) {
                if (recursion > i.getMaxRecursion()) continue;
                i.generateCave(writer, rng, base, engine, x, y, z, nextRecursion, waterHint);
            }
        }

        if (ravines.isNotEmpty()) {
            for (KrudWorldRavinePlacer i : ravines) {
                if (recursion > i.getMaxRecursion()) continue;
                i.generateRavine(writer, rng, base, engine, x, y, z, nextRecursion, waterHint);
            }
        }

        if (spheres.isNotEmpty()) {
            for (KrudWorldSphere i : spheres) {
                if (rng.nextInt(i.getRarity()) == 0) {
                    i.generate(base, engine, writer, x, y, z);
                }
            }
        }

        if (elipsoids.isNotEmpty()) {
            for (KrudWorldElipsoid i : elipsoids) {
                if (rng.nextInt(i.getRarity()) == 0) {
                    i.generate(base, engine, writer, x, y, z);
                }
            }
        }

        if (pyramids.isNotEmpty()) {
            for (KrudWorldPyramid i : pyramids) {
                if (rng.nextInt(i.getRarity()) == 0) {
                    i.generate(base, engine, writer, x, y, z);
                }
            }
        }
    }

    public int getMaxRange(KrudWorldData data, int recursion) {
        int max = 0;
        int nextRecursion = recursion + 1;

        for (KrudWorldCavePlacer i : caves) {
            if (recursion > i.getMaxRecursion()) continue;
            max = Math.max(max, i.getSize(data, nextRecursion));
        }

        for (KrudWorldRavinePlacer i : ravines) {
            if (recursion > i.getMaxRecursion()) continue;
            max = Math.max(max, i.getSize(data, nextRecursion));
        }

        if (elipsoids.isNotEmpty()) {
            max = (int) Math.max(elipsoids.stream().mapToDouble(KrudWorldElipsoid::maxSize).max().getAsDouble(), max);
        }

        if (spheres.isNotEmpty()) {
            max = (int) Math.max(spheres.stream().mapToDouble(KrudWorldSphere::maxSize).max().getAsDouble(), max);
        }

        if (pyramids.isNotEmpty()) {
            max = (int) Math.max(pyramids.stream().mapToDouble(KrudWorldPyramid::maxSize).max().getAsDouble(), max);
        }

        return max;
    }
}
