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

package dev.krud.world.util.matter.slices.container;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;

public abstract class RegistrantContainer<T extends KrudWorldRegistrant> {
    private final Class<T> type;
    private final String loadKey;

    public RegistrantContainer(Class<T> type, String loadKey) {
        this.type = type;
        this.loadKey = loadKey;
    }

    public T load(KrudWorldData data) {
        return (T) data.getLoaders().get(type).load(loadKey);
    }

    public String getLoadKey() {
        return loadKey;
    }
}
