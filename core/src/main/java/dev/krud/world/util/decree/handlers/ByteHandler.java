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
import dev.krud.world.util.math.RNG;

public class ByteHandler implements DecreeParameterHandler<Byte> {
    @Override
    public KList<Byte> getPossibilities() {
        return null;
    }

    @Override
    public String toString(Byte aByte) {
        return aByte.toString();
    }

    @Override
    public Byte parse(String in, boolean force) throws DecreeParsingException {
        try {
            return Byte.parseByte(in);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to parse byte \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Byte.class) || type.equals(byte.class);
    }

    @Override
    public String getRandomDefault() {
        return RNG.r.i(0, Byte.MAX_VALUE) + "";
    }
}
