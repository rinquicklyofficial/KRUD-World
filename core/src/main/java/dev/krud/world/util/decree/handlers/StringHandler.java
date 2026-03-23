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

package dev.krud.world.util.decree.handlers;

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.DecreeParameterHandler;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;

/**
 * Abstraction can sometimes breed stupidity
 */
public class StringHandler implements DecreeParameterHandler<String> {
    @Override
    public KList<String> getPossibilities() {
        return null;
    }

    @Override
    public String toString(String s) {
        return s;
    }

    @Override
    public String parse(String in, boolean force) throws DecreeParsingException {
        return in;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(String.class);
    }

    @Override
    public String getRandomDefault() {
        return new KList<String>().qadd("text").qadd("string")
                .qadd("blah").qadd("derp").qadd("yolo").getRandom();
    }
}
