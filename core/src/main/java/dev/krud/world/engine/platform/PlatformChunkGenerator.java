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

package dev.krud.world.engine.platform;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineTarget;
import dev.krud.world.engine.framework.Hotloadable;
import dev.krud.world.util.data.DataProvider;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface PlatformChunkGenerator extends Hotloadable, DataProvider {
    @Nullable
    Engine getEngine();

    @Override
    default KrudWorldData getData() {
        return getTarget().getData();
    }

    @NotNull
    EngineTarget getTarget();

    void injectChunkReplacement(World world, int x, int z, Executor syncExecutor);

    void close();

    boolean isStudio();

    void touch(World world);

    CompletableFuture<Integer> getSpawnChunks();
}
