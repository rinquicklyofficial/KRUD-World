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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("jigsaw-structure-placement")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a jigsaw structure placer")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldJigsawStructurePlacement implements IRare {
    @RegistryListResource(KrudWorldJigsawStructure.class)
    @Required
    @Desc("The structure to place")
    private String structure;

    @Required
    @Desc("The 1 in X chance rarity applies when generating multiple structures at once")
    private int rarity = 100;

    @Required
    @DependsOn({"spacing", "separation"})
    @Desc("The salt to use when generating the structure (to differentiate structures)")
    @MinNumber(Long.MIN_VALUE)
    @MaxNumber(Long.MAX_VALUE)
    private long salt = 0;

    @Required
    @MinNumber(0)
    @DependsOn({"salt", "separation"})
    @Desc("Average distance in chunks between two neighboring generation attempts")
    private int spacing = -1;

    @Required
    @MinNumber(0)
    @DependsOn({"salt", "spacing"})
    @Desc("Minimum distance in chunks between two neighboring generation attempts\nThe maximum distance of two neighboring generation attempts is 2*spacing - separation")
    private int separation = -1;

    @Desc("The method used to spread the structure")
    private SpreadType spreadType = SpreadType.LINEAR;

    @DependsOn({"spreadType"})
    @Desc("The noise style to use when spreadType is set to 'NOISE'\nThis ignores the spacing and separation parameters")
    private KrudWorldGeneratorStyle style = new KrudWorldGeneratorStyle();

    @DependsOn({"spreadType", "style"})
    @Desc("Threshold for noise style")
    private double threshold = 0.5;

    @ArrayType(type = KrudWorldJigsawMinDistance.class)
    @Desc("List of minimum distances to check for")
    private KList<KrudWorldJigsawMinDistance> minDistances = new KList<>();

    public KMap<String, Integer> collectMinDistances() {
        KMap<String, Integer> map = new KMap<>();
        for (KrudWorldJigsawMinDistance d : minDistances) {
            map.compute(d.getStructure(), (k, v) -> v != null ? Math.min(toChunks(d.getDistance()), v) : toChunks(d.getDistance()));
        }
        return map;
    }

    private int toChunks(int blocks) {
        return (int) Math.ceil(blocks / 16d);
    }

    private void calculateMissing(double divisor, long seed) {
        if (salt != 0 && separation > 0 && spacing > 0)
            return;
        seed *= (long) structure.hashCode() * rarity;
        if (salt == 0) {
            salt = new RNG(seed).l(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        if (separation == -1 || spacing == -1) {
            separation = (int) Math.round(rarity / divisor);
            spacing = new RNG(seed).i(separation, separation * 2);
        }
    }

    @ChunkCoordinates
    public boolean shouldPlace(KrudWorldData data, double divisor, long seed, int x, int z) {
        calculateMissing(divisor, seed);
        if (spreadType != SpreadType.NOISE)
            return shouldPlaceSpread(seed, x, z);

        return style.create(new RNG(seed + salt), data).noise(x, z) > threshold;
    }

    private boolean shouldPlaceSpread(long seed, int x, int z) {
        if (separation > spacing) {
            separation = spacing;
            KrudWorld.warn("JigsawStructurePlacement: separation must be less than or equal to spacing");
        }

        int i = Math.floorDiv(x, spacing);
        int j = Math.floorDiv(z, spacing);
        RNG rng = new RNG(i * 341873128712L + j * 132897987541L + seed + salt);

        int k = spacing - separation;
        int l = spreadType.apply(rng, k);
        int m = spreadType.apply(rng, k);
        return i * spacing + l == x && j * spacing + m == z;
    }

    @Desc("Spread type")
    public enum SpreadType {
        @Desc("Linear spread")
        LINEAR(RNG::i),
        @Desc("Triangular spread")
        TRIANGULAR((rng, bound) -> (rng.i(bound) + rng.i(bound)) / 2),
        @Desc("Noise based spread\nThis ignores the spacing and separation parameters")
        NOISE((rng, bound) -> 0);
        private final SpreadMethod method;

        SpreadType(SpreadMethod method) {
            this.method = method;
        }

        public int apply(RNG rng, int bound) {
            return method.apply(rng, bound);
        }
    }

    private interface SpreadMethod {
        int apply(RNG rng, int bound);
    }
}
