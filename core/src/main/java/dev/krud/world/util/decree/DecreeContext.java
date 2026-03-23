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

import dev.krud.world.util.plugin.VolmitSender;

public class DecreeContext {
    private static final ThreadLocal<VolmitSender> context = new ThreadLocal<>();

    public static VolmitSender get() {
        return context.get();
    }

    public static void touch(VolmitSender c) {
        context.set(c);
    }

    public static void remove() {
        context.remove();
    }
}
