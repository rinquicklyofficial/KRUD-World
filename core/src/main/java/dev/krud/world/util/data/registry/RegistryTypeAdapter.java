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

package dev.krud.world.util.data.registry;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class RegistryTypeAdapter<T> extends TypeAdapter<T> {
    private final KeyedRegistry<T> registry;

    private RegistryTypeAdapter(KeyedRegistry<T> type) {
        this.registry = type;
    }

    @Nullable
    public static <T> RegistryTypeAdapter<T> of(@NonNull Class<T> type) {
        final var registry = RegistryUtil.lookup(type);
        return registry.isEmpty() ? null : new RegistryTypeAdapter<>(registry);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        final var key = registry.keyOf(value);
        if (key == null) out.nullValue();
        else out.value(key.toString());
    }

    @Override
    public T read(JsonReader in) throws IOException {
        final NamespacedKey key = NamespacedKey.fromString(in.nextString());
        return key == null ? null : registry.get(key);
    }
}
