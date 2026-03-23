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
import dev.krud.world.engine.framework.Engine;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

public interface IObjectPlacer {
    int getHighest(int x, int z, KrudWorldData data);

    int getHighest(int x, int z, KrudWorldData data, boolean ignoreFluid);

    void set(int x, int y, int z, BlockData d);

    BlockData get(int x, int y, int z);

    boolean isPreventingDecay();

    boolean isCarved(int x, int y, int z);

    boolean isSolid(int x, int y, int z);

    boolean isUnderwater(int x, int z);

    int getFluidHeight();

    boolean isDebugSmartBore();

    void setTile(int xx, int yy, int zz, TileData tile);

    <T> void setData(int xx, int yy, int zz, T data);

    <T> @Nullable T getData(int xx, int yy, int zz, Class<T> t);

    Engine getEngine();
}
