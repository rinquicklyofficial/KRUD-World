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

package dev.krud.world.engine.modifier;

import dev.krud.world.engine.actuator.KrudWorldDecorantActuator;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedModifier;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.data.B;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.function.Consumer4;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.mantle.MantleChunk;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.matter.slices.MarkerMatter;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class KrudWorldCarveModifier extends EngineAssignedModifier<BlockData> {
    private final RNG rng;
    private final BlockData AIR = Material.CAVE_AIR.createBlockData();
    private final BlockData LAVA = Material.LAVA.createBlockData();
    private final KrudWorldDecorantActuator decorant;

    public KrudWorldCarveModifier(Engine engine) {
        super(engine, "Carve");
        rng = new RNG(getEngine().getSeedManager().getCarve());
        decorant = new KrudWorldDecorantActuator(engine);
    }

    @Override
    @ChunkCoordinates
    public void onModify(int x, int z, Hunk<BlockData> output, boolean multicore, ChunkContext context) {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        Mantle mantle = getEngine().getMantle().getMantle();
        MantleChunk mc = mantle.getChunk(x, z).use();
        KMap<Long, KList<Integer>> positions = new KMap<>();
        KMap<KrudWorldPosition, MatterCavern> walls = new KMap<>();
        Consumer4<Integer, Integer, Integer, MatterCavern> iterator = (xx, yy, zz, c) -> {
            if (c == null) {
                return;
            }

            if (yy >= getEngine().getWorld().maxHeight() - getEngine().getWorld().minHeight() || yy <= 0) { // Yes, skip bedrock
                return;
            }

            int rx = xx & 15;
            int rz = zz & 15;

            BlockData current = output.get(rx, yy, rz);

            if (B.isFluid(current)) {
                return;
            }

            positions.computeIfAbsent(Cache.key(rx, rz), (k) -> new KList<>()).qadd(yy);

            //todo: Fix chunk decoration not working on chunk's border

            if (rz < 15 && mc.get(xx, yy, zz + 1, MatterCavern.class) == null) {
                walls.put(new KrudWorldPosition(rx, yy, rz + 1), c);
            }

            if (rx < 15 && mc.get(xx + 1, yy, zz, MatterCavern.class) == null) {
                walls.put(new KrudWorldPosition(rx + 1, yy, rz), c);
            }

            if (rz > 0 && mc.get(xx, yy, zz - 1, MatterCavern.class) == null) {
                walls.put(new KrudWorldPosition(rx, yy, rz - 1), c);
            }

            if (rx > 0 && mc.get(xx - 1, yy, zz, MatterCavern.class) == null) {
                walls.put(new KrudWorldPosition(rx - 1, yy, rz), c);
            }

            if (current.getMaterial().isAir()) {
                return;
            }

            if (c.isWater()) {
                output.set(rx, yy, rz, context.getFluid().get(rx, rz));
            } else if (c.isLava()) {
                output.set(rx, yy, rz, LAVA);
            } else {
                if (getEngine().getDimension().getCaveLavaHeight() > yy) {
                    output.set(rx, yy, rz, LAVA);
                } else {
                    output.set(rx, yy, rz, AIR);
                }
            }
        };

        mc.iterate(MatterCavern.class, iterator);

        walls.forEach((i, v) -> {
            KrudWorldBiome biome = v.getCustomBiome().isEmpty()
                    ? getEngine().getCaveBiome(i.getX() + (x << 4), i.getZ() + (z << 4))
                    : getEngine().getData().getBiomeLoader().load(v.getCustomBiome());

            if (biome != null) {
                biome.setInferredType(InferredType.CAVE);
                BlockData d = biome.getWall().get(rng, i.getX() + (x << 4), i.getY(), i.getZ() + (z << 4), getData());

                if (d != null && B.isSolid(output.get(i.getX(), i.getY(), i.getZ())) && i.getY() <= context.getHeight().get(i.getX(), i.getZ())) {
                    output.set(i.getX(), i.getY(), i.getZ(), d);
                }
            }
        });

        positions.forEach((k, v) -> {
            if (v.isEmpty()) {
                return;
            }

            int rx = Cache.keyX(k);
            int rz = Cache.keyZ(k);
            v.sort(Integer::compare);
            CaveZone zone = new CaveZone();
            zone.setFloor(v.get(0));
            int buf = v.get(0) - 1;

            for (Integer i : v) {
                if (i < 0 || i > getEngine().getHeight()) {
                    continue;
                }

                if (i == buf + 1) {
                    buf = i;
                    zone.ceiling = buf;
                } else if (zone.isValid(getEngine())) {
                    processZone(output, mc, mantle, zone, rx, rz, rx + (x << 4), rz + (z << 4));
                    zone = new CaveZone();
                    zone.setFloor(i);
                    buf = i;
                }
            }

            if (zone.isValid(getEngine())) {
                processZone(output, mc, mantle, zone, rx, rz, rx + (x << 4), rz + (z << 4));
            }
        });

        getEngine().getMetrics().getDeposit().put(p.getMilliseconds());
        mc.release();
    }

    private void processZone(Hunk<BlockData> output, MantleChunk mc, Mantle mantle, CaveZone zone, int rx, int rz, int xx, int zz) {
        boolean decFloor = B.isSolid(output.getClosest(rx, zone.floor - 1, rz));
        boolean decCeiling = B.isSolid(output.getClosest(rx, zone.ceiling + 1, rz));
        int center = (zone.floor + zone.ceiling) / 2;
        int thickness = zone.airThickness();
        String customBiome = "";

        if (B.isDecorant(output.getClosest(rx, zone.ceiling + 1, rz))) {
            output.set(rx, zone.ceiling + 1, rz, AIR);
        }

        if (B.isDecorant(output.get(rx, zone.ceiling, rz))) {
            output.set(rx, zone.ceiling, rz, AIR);
        }

        if (M.r(1D / 16D)) {
            mantle.set(xx, zone.ceiling, zz, MarkerMatter.CAVE_CEILING);
        }

        if (M.r(1D / 16D)) {
            mantle.set(xx, zone.floor, zz, MarkerMatter.CAVE_FLOOR);
        }

        for (int i = zone.floor; i <= zone.ceiling; i++) {
            MatterCavern cavernData = (MatterCavern) mc.getOrCreate(i >> 4).slice(MatterCavern.class)
                    .get(rx, i & 15, rz);

            if (cavernData != null && !cavernData.getCustomBiome().isEmpty()) {
                customBiome = cavernData.getCustomBiome();
                break;
            }
        }

        KrudWorldBiome biome = customBiome.isEmpty()
                ? getEngine().getCaveBiome(xx, zz)
                : getEngine().getData().getBiomeLoader().load(customBiome);

        if (biome == null) {
            return;
        }

        biome.setInferredType(InferredType.CAVE);

        KList<BlockData> blocks = biome.generateLayers(getDimension(), xx, zz, rng, 3, zone.floor, getData(), getComplex());

        for (int i = 0; i < zone.floor - 1; i++) {
            if (!blocks.hasIndex(i)) {
                break;
            }
            int y = zone.floor - i - 1;

            BlockData b = blocks.get(i);
            BlockData down = output.get(rx, y, rz);

            if (!B.isSolid(down)) {
                continue;
            }

            if (B.isOre(down)) {
                output.set(rx, y, rz, B.toDeepSlateOre(down, b));
                continue;
            }

            output.set(rx, y, rz, blocks.get(i));
        }

        blocks = biome.generateCeilingLayers(getDimension(), xx, zz, rng, 3, zone.ceiling, getData(), getComplex());

        if (zone.ceiling + 1 < mantle.getWorldHeight()) {
            for (int i = 0; i < zone.ceiling + 1; i++) {
                if (!blocks.hasIndex(i)) {
                    break;
                }

                BlockData b = blocks.get(i);
                BlockData up = output.get(rx, zone.ceiling + i + 1, rz);

                if (!B.isSolid(up)) {
                    continue;
                }

                if (B.isOre(up)) {
                    output.set(rx, zone.ceiling + i + 1, rz, B.toDeepSlateOre(up, b));
                    continue;
                }

                output.set(rx, zone.ceiling + i + 1, rz, b);
            }
        }

        for (KrudWorldDecorator i : biome.getDecorators()) {
            if (i.getPartOf().equals(KrudWorldDecorationPart.NONE) && B.isSolid(output.get(rx, zone.getFloor() - 1, rz))) {
                decorant.getSurfaceDecorator().decorate(rx, rz, xx, xx, xx, zz, zz, zz, output, biome, zone.getFloor() - 1, zone.airThickness());
            } else if (i.getPartOf().equals(KrudWorldDecorationPart.CEILING) && B.isSolid(output.get(rx, zone.getCeiling() + 1, rz))) {
                decorant.getCeilingDecorator().decorate(rx, rz, xx, xx, xx, zz, zz, zz, output, biome, zone.getCeiling(), zone.airThickness());
            }
        }
    }

    @Data
    public static class CaveZone {
        private int ceiling = -1;
        private int floor = -1;

        public int airThickness() {
            return (ceiling - floor) - 1;
        }

        public boolean isValid(Engine engine) {
            return floor < ceiling && ceiling - floor >= 1 && floor >= 0 && ceiling <= engine.getHeight() && airThickness() > 0;
        }

        public String toString() {
            return floor + "-" + ceiling;
        }
    }
}
