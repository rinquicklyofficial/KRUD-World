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

package dev.krud.world.util.sentry;

import dev.krud.world.KrudWorld;
import io.sentry.ILogger;
import io.sentry.SentryLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

public class KrudWorldLogger implements ILogger {
    @Override
    public void log(@NotNull SentryLevel level, @NotNull String message, @Nullable Object... args) {
        KrudWorld.msg(String.format("%s: %s", level, String.format(message, args)));
    }

    @Override
    public void log(@NotNull SentryLevel level, @NotNull String message, @Nullable Throwable throwable) {
        if (throwable == null) {
            log(level, message);
        } else {
            KrudWorld.msg(String.format("%s: %s\n%s", level, String.format(message, throwable), captureStackTrace(throwable)));
        }
    }

    @Override
    public void log(@NotNull SentryLevel level, @Nullable Throwable throwable, @NotNull String message, @Nullable Object... args) {
        if (throwable == null) {
            log(level, message, args);
        } else {
            KrudWorld.msg(String.format("%s: %s\n%s", level, String.format(message, throwable), captureStackTrace(throwable)));
        }
    }

    @Override
    public boolean isEnabled(@Nullable SentryLevel level) {
        return true;
    }

    private @NotNull String captureStackTrace(@NotNull Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
