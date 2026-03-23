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
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.math.BlockPosition;
import dev.krud.world.util.math.RNG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

@Snippet("deposit")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Creates ore & other block deposits underground")
@Data
public class KrudWorldDepositGenerator {
    private final transient AtomicCache<KList<KrudWorldObject>> objects = new AtomicCache<>();
    private final transient AtomicCache<KList<BlockData>> blockData = new AtomicCache<>();
    @Required
    @MinNumber(0)
    @MaxNumber(8192) // TODO: WARNING HEIGHT
    @Desc("The minimum height this deposit can generate at")
    private int minHeight = 1;
    @Required
    @MinNumber(0)
    @MaxNumber(8192) // TODO: WARNING HEIGHT
    @Desc("The maximum height this deposit can generate at")
    private int maxHeight = 75;
    @Required
    @MinNumber(0)
    @MaxNumber(8192)
    @Desc("The minimum amount of deposit blocks per clump")
    private int minSize = 0;
    @Required
    @MinNumber(0)
    @MaxNumber(8192)
    @Desc("The maximum amount of deposit blocks per clump")
    private int maxSize = 128;
    @Required
    @MinNumber(0)
    @MaxNumber(2048)
    @Desc("The maximum amount of clumps per chunk")
    private int maxPerChunk = 3;
    @Required
    @MinNumber(0)
    @MaxNumber(2048)
    @Desc("The minimum amount of clumps per chunk")
    private int minPerChunk = 0;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The change of the deposit spawning in a chunk")
    private double spawnChance = 1;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The change of the a clump spawning in a chunk")
    private double perClumpSpawnChance = 1;
    @Required
    @ArrayType(min = 1, type = KrudWorldBlockData.class)
    @Desc("The palette of blocks to be used in this deposit generator")
    private KList<KrudWorldBlockData> palette = new KList<>();
    @MinNumber(1)
    @MaxNumber(64)
    @Desc("Ore varience is how many different objects clumps iris will create")
    private int varience = 3;
    @Desc("If set to true, this deposit will replace bedrock")
    private boolean replaceBedrock = false;

    public KrudWorldObject getClump(Engine engine, RNG rng, KrudWorldData rdata) {
        KList<KrudWorldObject> objects = this.objects.aquire(() ->
        {
            RNG rngv = new RNG(engine.getSeedManager().getDeposit() + hashCode());
            KList<KrudWorldObject> objectsf = new KList<>();

            for (int i = 0; i < varience; i++) {
                objectsf.add(generateClumpObject(rngv.nextParallelRNG(2349 * i + 3598), rdata));
            }

            return objectsf;
        });
        return objects.get(rng.i(0, objects.size()));
    }

    public int getMaxDimension() {
        return Math.min(11, (int) Math.ceil(Math.cbrt(maxSize)));
    }

    private KrudWorldObject generateClumpObject(RNG rngv, KrudWorldData rdata) {
        int s = rngv.i(minSize, maxSize + 1);
        if (s == 1) {
            KrudWorldObject o = new KrudWorldObject(1, 1, 1);
            o.getBlocks().put(o.getCenter(), nextBlock(rngv, rdata));
            return o;
        }

        int dim = Math.min(11, (int) Math.ceil(Math.cbrt(s)));
        KrudWorldObject o = new KrudWorldObject(dim, dim, dim);

        int volume = dim * dim * dim;
        if (s >= volume) {
            int x = 0, y = 0, z = 0;

            while (z < dim) {
                o.setUnsigned(x++, y, z, nextBlock(rngv, rdata));

                if (x == dim) {
                    x = 0;
                    y++;
                }

                if (y == dim) {
                    y = 0;
                    z++;
                }
            }
            return o;
        }

        KSet<BlockPosition> set = new KSet<>();
        while (s > 0) {
            BlockPosition ang = new BlockPosition(
                    rngv.i(0, dim),
                    rngv.i(0, dim),
                    rngv.i(0, dim)
            );
            if (!set.add(ang)) continue;

            s--;
            o.setUnsigned(ang.getX(), ang.getY(), ang.getZ(), nextBlock(rngv, rdata));
        }

        return o;
    }

    private BlockData nextBlock(RNG rngv, KrudWorldData rdata) {
        return getBlockData(rdata).get(rngv.i(0, getBlockData(rdata).size()));
    }

    public KList<BlockData> getBlockData(KrudWorldData rdata) {
        return blockData.aquire(() ->
        {
            KList<BlockData> blockData = new KList<>();

            for (KrudWorldBlockData ix : palette) {
                BlockData bx = ix.getBlockData(rdata);

                if (bx != null) {
                    blockData.add(bx);
                }
            }

            return blockData;
        });
    }
}
