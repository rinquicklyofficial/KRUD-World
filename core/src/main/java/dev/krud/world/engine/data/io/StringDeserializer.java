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

package dev.krud.world.engine.data.io;

import java.io.*;

public interface StringDeserializer<T> extends Deserializer<T> {

    T fromReader(Reader reader) throws IOException;

    default T fromString(String s) throws IOException {
        return fromReader(new StringReader(s));
    }

    @Override
    default T fromStream(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return fromReader(reader);
        }
    }

    @Override
    default T fromFile(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return fromReader(reader);
        }
    }

    @Override
    default T fromBytes(byte[] data) throws IOException {
        return fromReader(new StringReader(new String(data)));
    }
}
