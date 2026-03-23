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

package dev.krud.world.util.board;

import dev.krud.world.util.format.C;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * @author Missionary (missionarymc@gmail.com)
 * @since 3/29/2018
 */
@SuppressWarnings("ClassCanBeRecord")
public class BoardEntry {

    @Getter
    private final String prefix, suffix;

    private BoardEntry(final String prefix, final String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static BoardEntry translateToEntry(String input) {
        if (input.isEmpty()) {
            return new BoardEntry("", "");
        }
        if (input.length() <= 16) {
            return new BoardEntry(input, "");
        } else {
            String prefix = input.substring(0, 16);
            String suffix = "";

            if (prefix.endsWith("\u00a7")) {
                prefix = prefix.substring(0, prefix.length() - 1);
                suffix = "\u00a7" + suffix;
            }

            suffix = StringUtils.left(C.getLastColors(prefix) + suffix + input.substring(16), 16);
            return new BoardEntry(prefix, suffix);
        }
    }
}