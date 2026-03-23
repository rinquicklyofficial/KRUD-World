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

package dev.krud.world.engine.decorator;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedComponent;
import dev.krud.world.engine.framework.EngineDecorator;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldDecorationPart;
import dev.krud.world.engine.object.KrudWorldDecorator;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.B;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.math.RNG;
import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

public abstract class KrudWorldEngineDecorator extends EngineAssignedComponent implements EngineDecorator {
    @Getter
    private final KrudWorldDecorationPart part;
    private final long seed;
    private final long modX, modZ;

    public KrudWorldEngineDecorator(Engine engine, String name, KrudWorldDecorationPart part) {
        super(engine, name + " Decorator");
        this.part = part;
        this.seed = getSeed() + 29356788 - (part.ordinal() * 10439677L);
        this.modX = 29356788 ^ (part.ordinal() + 6);
        this.modZ = 10439677 ^ (part.ordinal() + 1);
    }

    @BlockCoordinates
    protected RNG getRNG(int x, int z) {
        return new RNG(x * modX + z * modZ + seed);
    }

    protected KrudWorldDecorator getDecorator(RNG rng, KrudWorldBiome biome, double realX, double realZ) {
        KList<KrudWorldDecorator> v = new KList<>();

        RNG gRNG = new RNG(seed);
        for (KrudWorldDecorator i : biome.getDecorators()) {
            try {
                if (i.getPartOf().equals(part) && i.getBlockData(biome, gRNG, realX, realZ, getData()) != null) {
                    v.add(i);
                }
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                KrudWorld.error("PART OF: " + biome.getLoadFile().getAbsolutePath() + " HAS AN INVALID DECORATOR near 'partOf'!!!");
            }
        }

        if (v.isNotEmpty()) {
            return v.get(rng.nextInt(v.size()));
        }

        return null;
    }

    protected BlockData fixFaces(BlockData b, Hunk<BlockData> hunk, int rX, int rZ, int x, int y, int z) {
        if (B.isVineBlock(b)) {
            MultipleFacing data = (MultipleFacing) b.clone();
            data.getFaces().forEach(f -> data.setFace(f, false));

            boolean found = false;
            for (BlockFace f : BlockFace.values()) {
                if (!f.isCartesian())
                    continue;
                int yy = y + f.getModY();

                BlockData r = getEngine().getMantle().get(x + f.getModX(), yy, z + f.getModZ());
                if (r.isFaceSturdy(f.getOppositeFace(), BlockSupport.FULL)) {
                    found = true;
                    data.setFace(f, true);
                    continue;
                }

                int xx = rX + f.getModX();
                int zz = rZ + f.getModZ();
                if (xx < 0 || xx > 15 || zz < 0 || zz > 15 || yy < 0 || yy > hunk.getHeight())
                    continue;

                r = hunk.get(xx, yy, zz);
                if (r.isFaceSturdy(f.getOppositeFace(), BlockSupport.FULL)) {
                    found = true;
                    data.setFace(f, true);
                }
            }
            if (!found)
                data.setFace(BlockFace.DOWN, true);
            return data;
        }
        return b;
    }
}
