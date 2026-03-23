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

import java.util.concurrent.atomic.AtomicReference;

public class ShortHandler implements DecreeParameterHandler<Short> {
    @Override
    public KList<Short> getPossibilities() {
        return null;
    }

    @Override
    public Short parse(String in, boolean force) throws DecreeParsingException {
        try {
            AtomicReference<String> r = new AtomicReference<>(in);
            double m = getMultiplier(r);
            return (short) (Short.valueOf(r.get()).doubleValue() * m);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to parse short \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Short.class) || type.equals(short.class);
    }

    @Override
    public String toString(Short f) {
        return f.toString();
    }

    @Override
    public String getRandomDefault() {
        return RNG.r.i(0, 99) + "";
    }
}
