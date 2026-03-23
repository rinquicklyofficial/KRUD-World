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

package dev.krud.world.util.agent;

import java.lang.instrument.Instrumentation;

public class Installer {
    private static volatile Instrumentation instrumentation;

    public static Instrumentation getInstrumentation() {
        Instrumentation instrumentation = Installer.instrumentation;
        if (instrumentation == null) {
            throw new IllegalStateException("The agent is not loaded or this method is not called via the system class loader");
        }
        return instrumentation;
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        doMain(instrumentation);
    }

    public static void agentmain(String arguments, Instrumentation instrumentation) {
        doMain(instrumentation);
    }

    private static synchronized void doMain(Instrumentation instrumentation) {
        if (Installer.instrumentation != null)
            return;
        Installer.instrumentation = instrumentation;
    }
}