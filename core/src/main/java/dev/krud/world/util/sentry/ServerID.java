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

package dev.krud.world.util.sentry;

import dev.krud.world.util.io.IO;
import io.sentry.protocol.User;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import oshi.SystemInfo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerID {
    public static final String ID = calculate();

    public static User asUser() {
        User u = new User();
        u.setId(ID);
        return u;
    }

    @SneakyThrows
    private static String calculate() {
        Digest md = new Digest();
        md.update(System.getProperty("java.vm.name"));
        md.update(System.getProperty("java.vm.version"));
        md.update(new SystemInfo().getHardware().getProcessor().toString());
        md.update(Runtime.getRuntime().maxMemory());
        for (var p : Bukkit.getPluginManager().getPlugins()) {
            md.update(p.getName());
        }

        return IO.bytesToHex(md.digest());
    }

    private static final class Digest {
        private final MessageDigest md = MessageDigest.getInstance("SHA-256");
        private final byte[] buffer = new byte[8];
        private final ByteBuffer wrapped = ByteBuffer.wrap(buffer);

        private Digest() throws NoSuchAlgorithmException {
        }

        public void update(String string) {
            if (string == null) return;
            md.update(string.getBytes(StandardCharsets.UTF_8));
        }

        public void update(long Long) {
            wrapped.putLong(0, Long);
            md.update(buffer, 0, 8);
        }

        public byte[] digest() {
            return md.digest();
        }
    }
}
