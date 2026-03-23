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

import dev.krud.world.core.scripting.kotlin.environment.KrudWorldSimpleExecutionEnvironment;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public interface SimpleEnvironment {
    static SimpleEnvironment create() {
        return new KrudWorldSimpleExecutionEnvironment();
    }

    static SimpleEnvironment create(@NonNull File projectDir) {
        return new KrudWorldSimpleExecutionEnvironment(projectDir);
    }

    void configureProject();

    void execute(@NonNull String script);

    void execute(@NonNull String script, @NonNull Class<?> type, @Nullable Map<@NonNull String, Object> vars);

    @Nullable
    Object evaluate(@NonNull String script);

    @Nullable
    Object evaluate(@NonNull String script, @NonNull Class<?> type, @Nullable Map<@NonNull String, Object> vars);
}