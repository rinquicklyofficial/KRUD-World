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

import org.bukkit.event.Listener;

@SuppressWarnings("EmptyMethod")
public interface IController extends Listener {
    String getName();

    void start();

    void stop();

    void tick();

    int getTickInterval();

    void l(Object l);

    void w(Object l);

    void f(Object l);

    void v(Object l);
}
