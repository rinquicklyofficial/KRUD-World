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

package dev.krud.world.util.nbt.mca;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public enum CompressionType {

    NONE(0, t -> t, t -> t),
    GZIP(1, GZIPOutputStream::new, GZIPInputStream::new),
    ZLIB(2, DeflaterOutputStream::new, InflaterInputStream::new);

    private final byte id;
    private final ExceptionFunction<OutputStream, ? extends OutputStream, IOException> compressor;
    private final ExceptionFunction<InputStream, ? extends InputStream, IOException> decompressor;

    CompressionType(int id,
                    ExceptionFunction<OutputStream, ? extends OutputStream, IOException> compressor,
                    ExceptionFunction<InputStream, ? extends InputStream, IOException> decompressor) {
        this.id = (byte) id;
        this.compressor = compressor;
        this.decompressor = decompressor;
    }

    public static CompressionType getFromID(byte id) {
        for (CompressionType c : CompressionType.values()) {
            if (c.id == id) {
                return c;
            }
        }
        return null;
    }

    public byte getID() {
        return id;
    }

    public OutputStream compress(OutputStream out) throws IOException {
        return compressor.accept(out);
    }

    public InputStream decompress(InputStream in) throws IOException {
        return decompressor.accept(in);
    }
}
