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
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Snippet("biome-palette")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A layer of surface / subsurface material in biomes")
@Data
public class KrudWorldBiomePaletteLayer {
    private final transient AtomicCache<KList<BlockData>> blockData = new AtomicCache<>();
    private final transient AtomicCache<CNG> layerGenerator = new AtomicCache<>();
    private final transient AtomicCache<CNG> heightGenerator = new AtomicCache<>();
    @Desc("The style of noise")
    private KrudWorldGeneratorStyle style = NoiseStyle.STATIC.style();
    @DependsOn({"minHeight", "maxHeight"})
    @MinNumber(0)
    @MaxNumber(2032) // TODO: WARNING HEIGHT

    @Desc("The min thickness of this layer")
    private int minHeight = 1;
    @DependsOn({"minHeight", "maxHeight"})
    @MinNumber(1)
    @MaxNumber(2032) // TODO: WARNING HEIGHT

    @Desc("The max thickness of this layer")
    private int maxHeight = 1;
    @Desc("If set, this layer will change size depending on the slope. If in bounds, the layer will get larger (taller) the closer to the center of this slope clip it is. If outside of the slipe's bounds, this layer will not show.")
    private KrudWorldSlopeClip slopeCondition = new KrudWorldSlopeClip();
    @MinNumber(0.0001)
    @Desc("The terrain zoom mostly for zooming in on a wispy palette")
    private double zoom = 5;
    @Required
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("The palette of blocks to be used in this layer")
    private KList<KrudWorldBlockData> palette = new KList<KrudWorldBlockData>().qadd(new KrudWorldBlockData("GRASS_BLOCK"));

    public CNG getHeightGenerator(RNG rng, KrudWorldData data) {
        return heightGenerator.aquire(() -> CNG.signature(rng.nextParallelRNG(minHeight * maxHeight + getBlockData(data).size())));
    }

    public BlockData get(RNG rng, double x, double y, double z, KrudWorldData data) {
        if (getBlockData(data).isEmpty()) {
            return null;
        }

        if (getBlockData(data).size() == 1) {
            return getBlockData(data).get(0);
        }

        return getLayerGenerator(rng, data).fit(getBlockData(data), x / zoom, y / zoom, z / zoom);
    }

    public CNG getLayerGenerator(RNG rng, KrudWorldData data) {
        return layerGenerator.aquire(() ->
        {
            RNG rngx = rng.nextParallelRNG(minHeight + maxHeight + getBlockData(data).size());
            return style.create(rngx, data);
        });
    }

    public KList<KrudWorldBlockData> add(String b) {
        palette.add(new KrudWorldBlockData(b));

        return palette;
    }

    public KList<BlockData> getBlockData(KrudWorldData data) {
        return blockData.aquire(() ->
        {
            KList<BlockData> blockData = new KList<>();
            for (KrudWorldBlockData ix : palette) {
                BlockData bx = ix.getBlockData(data);
                if (bx != null) {
                    for (int i = 0; i < ix.getWeight(); i++) {
                        blockData.add(bx);
                    }
                }
            }

            return blockData;
        });
    }

    public KrudWorldBiomePaletteLayer zero() {
        palette.clear();
        return this;
    }
}
