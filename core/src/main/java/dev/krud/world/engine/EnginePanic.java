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

package dev.krud.world.engine;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KMap;

public class EnginePanic {
    private static final KMap<String, String> stuff = new KMap<>();
    private static KMap<String, String> last = new KMap<>();

    public static void add(String key, String value) {
        stuff.put(key, value);
    }

    public static void saveLast() {
        last = stuff.copy();
    }

    public static void lastPanic() {
        for (String i : last.keySet()) {
            KrudWorld.error("Last Panic " + i + ": " + stuff.get(i));
        }
    }

    public static void panic() {
        lastPanic();
        for (String i : stuff.keySet()) {
            KrudWorld.error("Engine Panic " + i + ": " + stuff.get(i));
        }
    }
}
