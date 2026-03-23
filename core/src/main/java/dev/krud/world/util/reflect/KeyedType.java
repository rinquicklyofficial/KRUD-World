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

package dev.krud.world.util.reflect;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import dev.krud.world.util.data.registry.RegistryTypeAdapter;
import dev.krud.world.util.data.registry.RegistryUtil;
import org.bukkit.Keyed;

public class KeyedType {
    private static final boolean KEYED_ENABLED = Boolean.getBoolean("iris.keyed-types");
    private static final boolean KEYED_LENIENT = Boolean.getBoolean("iris.keyed-lenient");

    public static String[] values(Class<?> type) {
        if (!isKeyed(type)) return new String[0];
        if (!KEYED_ENABLED) return OldEnum.values(type);
        return RegistryUtil.lookup(type)
                        .map()
                        .keySet()
                        .stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
    }

    public static boolean isKeyed(Class<?> type) {
        if (KEYED_ENABLED) {
            if (KEYED_LENIENT) return !RegistryUtil.lookup(type).isEmpty();
            else return Keyed.class.isAssignableFrom(type);
        } else return OldEnum.isOldEnum(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeAdapter<T> createTypeAdapter(Gson gson, TypeToken<T> type) {
        if (!isKeyed(type.getRawType())) return null;
        return (TypeAdapter<T>) (KEYED_ENABLED ? RegistryTypeAdapter.of(type.getRawType()) : OldEnum.create(type.getRawType()));
    }
}
