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
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Ore Layer")
@Data
public class KrudWorldOreGenerator {
    @Desc("The palette of 'ore' generated")
    private KrudWorldMaterialPalette palette = new KrudWorldMaterialPalette().qclear();
    @Desc("The generator style for the 'ore'")
    private KrudWorldGeneratorStyle chanceStyle = new KrudWorldGeneratorStyle(NoiseStyle.STATIC);
    @Desc("Will ores generate on the surface of the terrain layer")
    private boolean generateSurface = false;
    @Desc("Threshold for rate of generation")
    private double threshold = 0.5;
    @Desc("Height limit (min, max)")
    private KrudWorldRange range = new KrudWorldRange(30, 80);

    private transient AtomicCache<CNG> chanceCache = new AtomicCache<>();

    public BlockData generate(int x, int y, int z, RNG rng, KrudWorldData data) {
        if (palette.getPalette().isEmpty()) {
            return null;
        }

        if (!range.contains(y)) {
            return null;
        }

        CNG chance = chanceCache.aquire(() -> chanceStyle.create(rng, data));

        if (chance.noise(x, y, z) > threshold) {
            return null;
        }

        return palette.get(rng, x, y, z, data);
    }
}
