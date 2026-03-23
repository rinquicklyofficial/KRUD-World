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

package dev.krud.world.util.plugin;

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PluginRegistry<T> {
    private final KMap<String, T> registry = new KMap<>();
    @Getter
    private final String namespace;

    public void unregisterAll() {
        registry.clear();
    }

    public KList<String> getRegistries() {
        return registry.k();
    }

    public T get(String s) {
        if (!registry.containsKey(s)) {
            return null;
        }

        return registry.get(s);
    }

    public void register(String s, T t) {
        registry.put(s, t);
    }

    public void unregister(String s) {
        registry.remove(s);
    }

    public T resolve(String id) {
        if (registry.isEmpty()) {
            return null;
        }

        return registry.get(id);
    }
}
