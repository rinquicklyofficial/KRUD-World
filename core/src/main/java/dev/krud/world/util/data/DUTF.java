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

package dev.krud.world.util.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * <p>Encodes signed and unsigned values using a common variable-length
 * scheme, found for example in
 * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
 * Google's Protocol Buffers</a>. It uses fewer bytes to encode smaller values,
 * but will use slightly more bytes to encode large values.</p>
 * <p/>
 * <p>Signed values are further encoded using so-called zig-zag encoding
 * in order to make them "compatible" with variable-length encoding.</p>
 */
public final class DUTF {

    private DUTF() {
    }

    public static void write(String s, DataOutputStream dos) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        dos.writeShort(b.length);
        dos.write(b);
    }

    public static String read(DataInputStream din) throws IOException {
        byte[] d = new byte[din.readShort()];
        din.read(d);
        return new String(d, StandardCharsets.UTF_8);
    }
}