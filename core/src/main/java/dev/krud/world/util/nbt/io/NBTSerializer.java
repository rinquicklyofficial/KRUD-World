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

import dev.krud.world.engine.data.io.Serializer;
import dev.krud.world.util.nbt.tag.Tag;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class NBTSerializer implements Serializer<NamedTag> {

    private final boolean compressed;

    public NBTSerializer() {
        this(true);
    }

    public NBTSerializer(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public void toStream(NamedTag object, OutputStream out) throws IOException {
        NBTOutputStream nbtOut;
        if (compressed) {
            nbtOut = new NBTOutputStream(new GZIPOutputStream(out, true));
        } else {
            nbtOut = new NBTOutputStream(out);
        }
        nbtOut.writeTag(object, Tag.DEFAULT_MAX_DEPTH);
        nbtOut.flush();
    }
}
