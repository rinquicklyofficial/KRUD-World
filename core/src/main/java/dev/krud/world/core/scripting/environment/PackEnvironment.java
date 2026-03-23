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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.scripting.kotlin.environment.KrudWorldPackExecutionEnvironment;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.util.math.RNG;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public interface PackEnvironment extends SimpleEnvironment {
    static PackEnvironment create(@NonNull KrudWorldData data) {
        return new KrudWorldPackExecutionEnvironment(data);
    }

    @NonNull
    KrudWorldData getData();

    @Nullable
    Object createNoise(@NonNull String script, @NonNull RNG rng);

    EngineEnvironment with(@NonNull Engine engine);
}