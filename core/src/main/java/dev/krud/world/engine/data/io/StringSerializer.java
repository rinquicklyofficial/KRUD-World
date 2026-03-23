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

public interface StringSerializer<T> extends Serializer<T> {

    void toWriter(T object, Writer writer) throws IOException;

    default String toString(T object) throws IOException {
        Writer writer = new StringWriter();
        toWriter(object, writer);
        writer.flush();
        return writer.toString();
    }

    @Override
    default void toStream(T object, OutputStream stream) throws IOException {
        Writer writer = new OutputStreamWriter(stream);
        toWriter(object, writer);
        writer.flush();
    }

    @Override
    default void toFile(T object, File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            toWriter(object, writer);
        }
    }
}
