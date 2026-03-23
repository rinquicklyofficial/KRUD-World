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

package dev.krud.world.engine.framework.placer;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.IObjectPlacer;
import dev.krud.world.engine.object.KrudWorldObjectPlacement;
import dev.krud.world.engine.object.TileData;
import dev.krud.world.util.math.RNG;
import org.bukkit.block.data.BlockData;

public class HeightmapObjectPlacer implements IObjectPlacer {
    private final long s;
    private final KrudWorldObjectPlacement config;
    private final IObjectPlacer oplacer;

    public HeightmapObjectPlacer(Engine engine, RNG rng, int x, int yv, int z, KrudWorldObjectPlacement config, IObjectPlacer oplacer) {
        s = rng.nextLong() + yv + z - x;
        this.config = config;
        this.oplacer = oplacer;
    }

    public int getHighest(int param1Int1, int param1Int2, KrudWorldData data) {
        return (int) Math.round(config.getHeightmap().getNoise(this.s, param1Int1, param1Int2, data));
    }

    public int getHighest(int param1Int1, int param1Int2, KrudWorldData data, boolean param1Boolean) {
        return (int) Math.round(config.getHeightmap().getNoise(this.s, param1Int1, param1Int2, data));
    }

    public void set(int param1Int1, int param1Int2, int param1Int3, BlockData param1BlockData) {
        oplacer.set(param1Int1, param1Int2, param1Int3, param1BlockData);
    }

    public BlockData get(int param1Int1, int param1Int2, int param1Int3) {
        return oplacer.get(param1Int1, param1Int2, param1Int3);
    }

    public boolean isPreventingDecay() {
        return oplacer.isPreventingDecay();
    }

    @Override
    public boolean isCarved(int x, int y, int z) {
        return oplacer.isCarved(x,y,z);
    }

    public boolean isSolid(int param1Int1, int param1Int2, int param1Int3) {
        return oplacer.isSolid(param1Int1, param1Int2, param1Int3);
    }

    public boolean isUnderwater(int param1Int1, int param1Int2) {
        return oplacer.isUnderwater(param1Int1, param1Int2);
    }

    public int getFluidHeight() {
        return oplacer.getFluidHeight();
    }

    public boolean isDebugSmartBore() {
        return oplacer.isDebugSmartBore();
    }

    public void setTile(int param1Int1, int param1Int2, int param1Int3, TileData param1TileData) {
        oplacer.setTile(param1Int1, param1Int2, param1Int3, param1TileData);
    }

    @Override
    public <T> void setData(int xx, int yy, int zz, T data) {
        oplacer.setData(xx, yy, zz, data);
    }

    @Override
    public <T> T getData(int xx, int yy, int zz, Class<T> t) {
        return oplacer.getData(xx, yy, zz, t);
    }

    @Override
    public Engine getEngine() {
        return null;
    }
}
