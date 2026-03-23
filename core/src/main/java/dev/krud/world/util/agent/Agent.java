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

import dev.krud.world.KrudWorld;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Agent {
    private static final String NAME = "dev.krud.world.util.agent.Installer";
    public static final File AGENT_JAR = new File(KrudWorld.instance.getDataFolder(), "agent.jar");

    public static ClassReloadingStrategy installed() {
        return ClassReloadingStrategy.of(getInstrumentation());
    }

    public static Instrumentation getInstrumentation() {
        Instrumentation instrumentation = doGetInstrumentation();
        if (instrumentation == null) throw new IllegalStateException("The agent is not initialized or unavailable");
        return instrumentation;
    }

    public static boolean install() {
        if (doGetInstrumentation() != null)
            return true;
        try {
            Files.copy(KrudWorld.instance.getResource("agent.jar"), AGENT_JAR.toPath(), StandardCopyOption.REPLACE_EXISTING);
            KrudWorld.info("Installing Java Agent...");
            ByteBuddyAgent.attach(AGENT_JAR, ByteBuddyAgent.ProcessProvider.ForCurrentVm.INSTANCE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return doGetInstrumentation() != null;
    }

    private static Instrumentation doGetInstrumentation() {
        try {
            return (Instrumentation) Class.forName(NAME, true, ClassLoader.getSystemClassLoader()).getMethod("getInstrumentation").invoke(null);
        } catch (Exception ex) {
            return null;
        }
    }
}
