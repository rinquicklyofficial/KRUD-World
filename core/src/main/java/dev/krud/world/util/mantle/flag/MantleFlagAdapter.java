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

package dev.krud.world.util.mantle.flag;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class MantleFlagAdapter extends TypeAdapter<MantleFlag> {
    private static final String CUSTOM = "CUSTOM:";
    private static final int CUSTOM_LENGTH = CUSTOM.length();

    @Override
    public void write(JsonWriter out, MantleFlag value) throws IOException {
        if (value == null) out.nullValue();
        else out.value(value.toString());
    }

    @Override
    public MantleFlag read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String s = in.nextString();
        if (s.startsWith(CUSTOM) && s.length() > CUSTOM_LENGTH)
            return MantleFlag.of(Integer.parseInt(s.substring(CUSTOM_LENGTH)));
        return ReservedFlag.valueOf(s);
    }
}
