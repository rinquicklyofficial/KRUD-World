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

package dev.krud.world.engine.mantle;

import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.nms.container.Pair;
import dev.krud.world.engine.KrudWorldComplex;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineTarget;
import dev.krud.world.engine.mantle.components.MantleJigsawComponent;
import dev.krud.world.engine.mantle.components.MantleObjectComponent;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.engine.object.KrudWorldPosition;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.B;
import dev.krud.world.util.documentation.BlockCoordinates;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.mantle.MantleChunk;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.matter.*;
import dev.krud.world.util.matter.slices.UpdateMatter;
import dev.krud.world.util.parallel.MultiBurst;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface EngineMantle extends MatterGenerator {
    BlockData AIR = B.get("AIR");

    Mantle getMantle();

    Engine getEngine();

    int getRadius();

    int getRealRadius();

    @UnmodifiableView
    List<Pair<List<MantleComponent>, Integer>> getComponents();

    @UnmodifiableView
    Map<MantleFlag, MantleComponent> getRegisteredComponents();

    boolean registerComponent(MantleComponent c);

    @UnmodifiableView
    KList<MantleFlag> getComponentFlags();

    void hotload();

    default int getHighest(int x, int z) {
        return getHighest(x, z, getData());
    }

    @ChunkCoordinates
    default KList<KrudWorldPosition> findMarkers(int x, int z, MatterMarker marker) {
        KList<KrudWorldPosition> p = new KList<>();
        getMantle().iterateChunk(x, z, MatterMarker.class, (xx, yy, zz, mm) -> {
            if (marker.equals(mm)) {
                p.add(new KrudWorldPosition(xx + (x << 4), yy, zz + (z << 4)));
            }
        });

        return p;
    }

    default int getHighest(int x, int z, boolean ignoreFluid) {
        return getHighest(x, z, getData(), ignoreFluid);
    }

    default int getHighest(int x, int z, KrudWorldData data) {
        return getHighest(x, z, data, false);
    }

    default int getHighest(int x, int z, KrudWorldData data, boolean ignoreFluid) {
        return ignoreFluid ? trueHeight(x, z) : Math.max(trueHeight(x, z), getEngine().getDimension().getFluidHeight());
    }

    default int trueHeight(int x, int z) {
        return getComplex().getRoundedHeighteightStream().get(x, z);
    }

    @Deprecated(forRemoval = true)
    default boolean isCarved(int x, int h, int z) {
        return getMantle().get(x, h, z, MatterCavern.class) != null;
    }

    @Deprecated(forRemoval = true)
    default BlockData get(int x, int y, int z) {
        BlockData block = getMantle().get(x, y, z, BlockData.class);
        if (block == null)
            return AIR;
        return block;
    }

    default boolean isPreventingDecay() {
        return getEngine().getDimension().isPreventLeafDecay();
    }

    default boolean isUnderwater(int x, int z) {
        return getHighest(x, z, true) <= getFluidHeight();
    }

    default int getFluidHeight() {
        return getEngine().getDimension().getFluidHeight();
    }

    default boolean isDebugSmartBore() {
        return getEngine().getDimension().isDebugSmartBore();
    }

    default void trim(long dur, int limit) {
        getMantle().trim(dur, limit);
    }

    default KrudWorldData getData() {
        return getEngine().getData();
    }

    default EngineTarget getTarget() {
        return getEngine().getTarget();
    }

    default KrudWorldDimension getDimension() {
        return getEngine().getDimension();
    }

    default KrudWorldComplex getComplex() {
        return getEngine().getComplex();
    }

    default void close() {
        getMantle().close();
    }

    default void saveAllNow() {
        getMantle().saveAll();
    }

    default void save() {

    }

    default void trim(int limit) {
        getMantle().trim(TimeUnit.SECONDS.toMillis(KrudWorldSettings.get().getPerformance().getMantleKeepAlive()), limit);
    }
    default int unloadTectonicPlate(int tectonicLimit){
        return getMantle().unloadTectonicPlate(tectonicLimit);
    }

    default MultiBurst burst() {
        return getEngine().burst();
    }

    @ChunkCoordinates
    default <T> void insertMatter(int x, int z, Class<T> t, Hunk<T> blocks, boolean multicore) {
        if (!getEngine().getDimension().isUseMantle()) {
            return;
        }

        var chunk = getMantle().getChunk(x, z).use();
        try {
            chunk.iterate(t, blocks::set);
        } finally {
            chunk.release();
        }
    }

    @BlockCoordinates
    default void updateBlock(int x, int y, int z) {
        getMantle().set(x, y, z, UpdateMatter.ON);
    }

    @BlockCoordinates
    default void dropCavernBlock(int x, int y, int z) {
        Matter matter = getMantle().getChunk(x & 15, z & 15).get(y & 15);

        if (matter != null) {
            matter.slice(MatterCavern.class).set(x & 15, y & 15, z & 15, null);
        }
    }

    default boolean queueRegenerate(int x, int z) {
        return false; // TODO:
    }

    default boolean dequeueRegenerate(int x, int z) {
        return false;// TODO:
    }

    default int getLoadedRegionCount() {
        return getMantle().getLoadedRegionCount();
    }

    MantleJigsawComponent getJigsawComponent();

    MantleObjectComponent getObjectComponent();

    default boolean isCovered(int x, int z) {
        int s = getRealRadius();

        for (int i = -s; i <= s; i++) {
            for (int j = -s; j <= s; j++) {
                int xx = i + x;
                int zz = j + z;
                if (!getMantle().hasFlag(xx, zz, MantleFlag.REAL)) {
                    return false;
                }
            }
        }

        return true;
    }

    default void cleanupChunk(int x, int z) {
        if (!isCovered(x, z)) return;
        MantleChunk chunk = getMantle().getChunk(x, z).use();
        try {
            chunk.raiseFlagUnchecked(MantleFlag.CLEANED, () -> {
                chunk.deleteSlices(BlockData.class);
                chunk.deleteSlices(String.class);
                chunk.deleteSlices(MatterCavern.class);
                chunk.deleteSlices(MatterFluidBody.class);
            });
        } finally {
            chunk.release();
        }
    }

    default int getUnloadRegionCount() {
        return getMantle().getUnloadRegionCount();
    }

    default double getAdjustedIdleDuration() {
        return getMantle().getAdjustedIdleDuration();
    }
}