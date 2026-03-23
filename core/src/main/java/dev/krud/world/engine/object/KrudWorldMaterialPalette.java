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

import java.util.Optional;

@Snippet("palette")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A palette of materials")
@Data
public class KrudWorldMaterialPalette {
    private final transient AtomicCache<KList<BlockData>> blockData = new AtomicCache<>();
    private final transient AtomicCache<CNG> layerGenerator = new AtomicCache<>();
    private final transient AtomicCache<CNG> heightGenerator = new AtomicCache<>();
    @Desc("The style of noise")
    private KrudWorldGeneratorStyle style = NoiseStyle.STATIC.style();
    @MinNumber(0.0001)
    @Desc("The terrain zoom mostly for zooming in on a wispy palette")
    private double zoom = 5;
    @Required
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("The palette of blocks to be used in this layer")
    private KList<KrudWorldBlockData> palette = new KList<KrudWorldBlockData>().qadd(new KrudWorldBlockData("STONE"));

    public BlockData get(RNG rng, double x, double y, double z, KrudWorldData rdata) {
        if (getBlockData(rdata).isEmpty()) {
            return null;
        }

        if (getBlockData(rdata).size() == 1) {
            return getBlockData(rdata).get(0);
        }

        return getLayerGenerator(rng, rdata).fit(getBlockData(rdata), x / zoom, y / zoom, z / zoom);
    }

    public Optional<TileData> getTile(RNG rng, double x, double y, double z, KrudWorldData rdata) {
        if (getBlockData(rdata).isEmpty())
            return Optional.empty();

        TileData tile = getBlockData(rdata).size() == 1 ? palette.get(0).tryGetTile(rdata) : palette.getRandom(rng).tryGetTile(rdata);
        return tile != null ? Optional.of(tile) : Optional.empty();
    }

    public CNG getLayerGenerator(RNG rng, KrudWorldData rdata) {
        return layerGenerator.aquire(() ->
        {
            RNG rngx = rng.nextParallelRNG(-23498896 + getBlockData(rdata).size());
            return style.create(rngx, rdata);
        });
    }

    public KrudWorldMaterialPalette qclear() {
        palette.clear();
        return this;
    }

    public KList<KrudWorldBlockData> add(String b) {
        palette.add(new KrudWorldBlockData(b));

        return palette;
    }

    public KrudWorldMaterialPalette qadd(String b) {
        palette.add(new KrudWorldBlockData(b));

        return this;
    }

    public KList<BlockData> getBlockData(KrudWorldData rdata) {
        return blockData.aquire(() ->
        {
            KList<BlockData> blockData = new KList<>();
            for (KrudWorldBlockData ix : palette) {
                BlockData bx = ix.getBlockData(rdata);
                if (bx != null) {
                    for (int i = 0; i < ix.getWeight(); i++) {
                        blockData.add(bx);
                    }
                }
            }

            return blockData;
        });
    }

    public KrudWorldMaterialPalette zero() {
        palette.clear();
        return this;
    }
}
