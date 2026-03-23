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

import dev.krud.world.engine.data.io.Deserializer;
import dev.krud.world.util.nbt.tag.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class NBTDeserializer implements Deserializer<NamedTag> {

    private final boolean compressed;

    public NBTDeserializer() {
        this(true);
    }

    public NBTDeserializer(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public NamedTag fromStream(InputStream stream) throws IOException {
        NBTInputStream nbtIn;
        if (compressed) {
            nbtIn = new NBTInputStream(new GZIPInputStream(stream));
        } else {
            nbtIn = new NBTInputStream(stream);
        }
        return nbtIn.readTag(Tag.DEFAULT_MAX_DEPTH);
    }
}
