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

import java.io.IOException;

public class ParseException extends IOException {

    public ParseException(String msg) {
        super(msg);
    }

    public ParseException(String msg, String value, int index) {
        super(msg + " at: " + formatError(value, index));
    }

    private static String formatError(String value, int index) {
        StringBuilder builder = new StringBuilder();
        int i = Math.min(value.length(), index);
        if (i > 35) {
            builder.append("...");
        }
        builder.append(value, Math.max(0, i - 35), i);
        builder.append("<--[HERE]");
        return builder.toString();
    }
}
