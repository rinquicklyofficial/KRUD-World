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
import java.net.URL;

public interface Deserializer<T> {

    T fromStream(InputStream stream) throws IOException;

    default T fromFile(File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            return fromStream(bis);
        }
    }

    default T fromBytes(byte[] data) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        return fromStream(stream);
    }

    default T fromResource(Class<?> clazz, String path) throws IOException {
        try (InputStream stream = clazz.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("resource \"" + path + "\" not found");
            }
            return fromStream(stream);
        }
    }

    default T fromURL(URL url) throws IOException {
        try (InputStream stream = url.openStream()) {
            return fromStream(stream);
        }
    }


}
