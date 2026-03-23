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

package dev.krud.world.core.scripting.environment;

import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.core.scripting.func.UpdateExecutor;
import dev.krud.world.core.scripting.kotlin.environment.KrudWorldExecutionEnvironment;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.util.mantle.MantleChunk;
import lombok.NonNull;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EngineEnvironment extends PackEnvironment {
    static EngineEnvironment create(@NonNull Engine engine) {
        return new KrudWorldExecutionEnvironment(engine);
    }

    @NonNull
    Engine getEngine();

    @Nullable
    Object spawnMob(@NonNull String script, @NonNull Location location);

    void postSpawnMob(@NonNull String script, @NonNull Location location, @NonNull Entity mob);

    void preprocessObject(@NonNull String script, @NonNull KrudWorldRegistrant object);

    void updateChunk(@NonNull String script, @NonNull MantleChunk mantleChunk, @NonNull Chunk chunk, @NonNull UpdateExecutor executor);
}