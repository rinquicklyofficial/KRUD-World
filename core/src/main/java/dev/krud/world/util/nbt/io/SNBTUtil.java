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

package dev.krud.world.util.nbt.io;

import dev.krud.world.util.nbt.tag.Tag;

import java.io.IOException;

public class SNBTUtil {

    public static String toSNBT(Tag<?> tag) throws IOException {
        return new SNBTSerializer().toString(tag);
    }

    public static Tag<?> fromSNBT(String string) throws IOException {
        return new SNBTDeserializer().fromString(string);
    }
}
