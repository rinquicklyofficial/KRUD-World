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

import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.data.DataProvider;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import lombok.*;
import lombok.experimental.Accessors;

@Snippet("generator-layer")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("This represents a link to a generator for a biome")
@Data
public class KrudWorldBiomeGeneratorLink {

    private final transient AtomicCache<KrudWorldGenerator> gen = new AtomicCache<>();
    @RegistryListResource(KrudWorldGenerator.class)
    @Desc("The generator id")
    private String generator = "default";
    @DependsOn({"min", "max"})
    @Required
    @MinNumber(-2032) // TODO: WARNING HEIGHT
    @MaxNumber(2032) // TODO: WARNING HEIGHT
    @Desc("The min block value (value + fluidHeight)")
    private int min = 0;
    @DependsOn({"min", "max"})
    @Required
    @MinNumber(-2032) // TODO: WARNING HEIGHT
    @MaxNumber(2032) // TODO: WARNING HEIGHT
    @Desc("The max block value (value + fluidHeight)")
    private int max = 0;

    public KrudWorldGenerator getCachedGenerator(DataProvider g) {
        return gen.aquire(() -> {
            KrudWorldGenerator gen = g.getData().getGeneratorLoader().load(getGenerator());

            if (gen == null) {
                gen = new KrudWorldGenerator();
            }

            return gen;
        });
    }

    public double getHeight(DataProvider xg, double x, double z, long seed) {
        double g = getCachedGenerator(xg).getHeight(x, z, seed);
        g = g < 0 ? 0 : g;
        g = g > 1 ? 1 : g;

        return KrudWorldInterpolation.lerp(min, max, g);
    }
}
