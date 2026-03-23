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

package dev.krud.world.core.nms;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.nms.v1X.NMSBinding1X;
import org.bukkit.Bukkit;

import java.util.List;

public class INMS {
    private static final Version CURRENT = Boolean.getBoolean("iris.no-version-limit") ?
            new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, null) :
            new Version(21, 11, null);

    private static final List<Version> REVISION = List.of(
            new Version(21, 11, "v1_21_R7"),
            new Version(21, 9, "v1_21_R6"),
            new Version(21, 6, "v1_21_R5"),
            new Version(21, 5, "v1_21_R4"),
            new Version(21, 4, "v1_21_R3"),
            new Version(21, 2, "v1_21_R2"),
            new Version(21, 0, "v1_21_R1"),
            new Version(20, 5, "v1_20_R4")
    );

    private static final List<Version> PACKS = List.of(
            new Version(21, 5, "31100"),
            new Version(21, 4, "31020"),
            new Version(21, 2, "31000"),
            new Version(20, 1, "3910")
    );

    //@done
    private static final INMSBinding binding = bind();
    public static final String OVERWORLD_TAG = getTag(PACKS, "3910");

    public static INMSBinding get() {
        return binding;
    }

    public static String getNMSTag() {
        if (KrudWorldSettings.get().getGeneral().isDisableNMS()) {
            return "BUKKIT";
        }

        try {
            String name = Bukkit.getServer().getClass().getCanonicalName();
            if (name.equals("org.bukkit.craftbukkit.CraftServer")) {
                return getTag(REVISION, "BUKKIT");
            } else {
                return name.split("\\Q.\\E")[3];
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            KrudWorld.error("Failed to determine server nms version!");
            e.printStackTrace();
        }

        return "BUKKIT";
    }

    private static INMSBinding bind() {
        String code = getNMSTag();
        KrudWorld.info("Locating NMS Binding for " + code);

        try {
            Class<?> clazz = Class.forName("dev.krud.world.core.nms."+code+".NMSBinding");
            try {
                Object b = clazz.getConstructor().newInstance();
                if (b instanceof INMSBinding binding) {
                    KrudWorld.info("Craftbukkit " + code + " <-> " + b.getClass().getSimpleName() + " Successfully Bound");
                    return binding;
                }
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
        } catch (ClassNotFoundException|NoClassDefFoundError classNotFoundException) {}

        KrudWorld.info("Craftbukkit " + code + " <-> " + NMSBinding1X.class.getSimpleName() + " Successfully Bound");
        KrudWorld.warn("Note: Some features of KrudWorld may not work the same since you are on an unsupported version of Minecraft.");
        KrudWorld.warn("Note: If this is a new version, expect an update soon.");

        return new NMSBinding1X();
    }

    private static String getTag(List<Version> versions, String def) {
        var version = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.", 3);
        int major = 0;
        int minor = 0;

        if (version.length > 2) {
            major = Integer.parseInt(version[1]);
            minor = Integer.parseInt(version[2]);
        } else if (version.length == 2) {
            major = Integer.parseInt(version[1]);
        }
        if (CURRENT.major < major || CURRENT.minor < minor) {
            return versions.getFirst().tag;
        }

        for (var p : versions) {
            if (p.major > major || p.minor > minor)
                continue;
            return p.tag;
        }
        return def;
    }

    private record Version(int major, int minor, String tag) {}
}
