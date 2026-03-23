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
import dev.krud.world.util.format.Form;
import dev.krud.world.util.math.RNG;

import java.util.concurrent.atomic.AtomicReference;

public class FloatHandler implements DecreeParameterHandler<Float> {
    @Override
    public KList<Float> getPossibilities() {
        return null;
    }

    @Override
    public Float parse(String in, boolean force) throws DecreeParsingException {
        try {
            AtomicReference<String> r = new AtomicReference<>(in);
            double m = getMultiplier(r);
            return (float) (Float.parseFloat(r.get()) * m);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to parse float \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Float.class) || type.equals(float.class);
    }

    @Override
    public String toString(Float f) {
        return f.toString();
    }

    @Override
    public String getRandomDefault() {
        return Form.f(RNG.r.d(0, 99.99), 1) + "";
    }
}
