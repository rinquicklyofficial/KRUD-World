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

package dev.krud.world.core.link;

import org.bukkit.NamespacedKey;

public record Identifier(String namespace, String key) {

    private static final String DEFAULT_NAMESPACE = "minecraft";

    public static Identifier fromNamespacedKey(NamespacedKey key) {
        return new Identifier(key.getNamespace(), key.getKey());
    }

    public static Identifier fromString(String id) {
        String[] strings = id.split(":", 2);
        if (strings.length == 1) {
            return new Identifier(DEFAULT_NAMESPACE, strings[0]);
        } else {
            return new Identifier(strings[0], strings[1]);
        }
    }

    @Override
    public String toString() {
        return namespace + ":" + key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Identifier i) {
            return i.namespace().equals(this.namespace) && i.key().equals(this.key);
        } else if (obj instanceof NamespacedKey i) {
            return i.getNamespace().equals(this.namespace) && i.getKey().equals(this.key);
        } else {
            return false;
        }
    }
}
