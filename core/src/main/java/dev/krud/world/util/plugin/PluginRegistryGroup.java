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

public class PluginRegistryGroup<T> {
    private final KMap<String, PluginRegistry<T>> registries = new KMap<>();

    public T resolve(String namespace, String id) {
        if (registries.isEmpty()) {
            return null;
        }

        PluginRegistry<T> r = registries.get(namespace);
        if (r == null) {
            return null;
        }

        return r.resolve(id);
    }

    public void clearRegistries() {
        registries.clear();
    }

    public void removeRegistry(String namespace) {
        registries.remove(namespace);
    }

    public PluginRegistry<T> getRegistry(String namespace) {
        return registries.computeIfAbsent(namespace, PluginRegistry::new);
    }

    public KList<String> compile() {
        KList<String> l = new KList<>();
        registries.values().forEach((i)
                -> i.getRegistries().forEach((j)
                -> l.add(i.getNamespace() + ":" + j)));
        return l;
    }
}
