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

package dev.krud.world.util.decree;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.plugin.VolmitSender;

public interface DecreeContextHandler<T> {
    KMap<Class<?>, DecreeContextHandler<?>> contextHandlers = buildContextHandlers();

    static KMap<Class<?>, DecreeContextHandler<?>> buildContextHandlers() {
        KMap<Class<?>, DecreeContextHandler<?>> contextHandlers = new KMap<>();

        try {
            KrudWorld.initialize("dev.krud.world.util.decree.context").forEach((i)
                    -> contextHandlers.put(((DecreeContextHandler<?>) i).getType(), (DecreeContextHandler<?>) i));
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }

        return contextHandlers;
    }

    Class<T> getType();

    T handle(VolmitSender sender);
}
