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
import dev.krud.world.util.math.M;

public class BooleanHandler implements DecreeParameterHandler<Boolean> {
    @Override
    public KList<Boolean> getPossibilities() {
        return null;
    }

    @Override
    public String toString(Boolean aByte) {
        return aByte.toString();
    }

    @Override
    public Boolean parse(String in, boolean force) throws DecreeParsingException {
        try {
            if (in.equals("null") || in.equals("other") || in.equals("flip")) {
                return null;
            }
            return Boolean.parseBoolean(in);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to parse boolean \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Boolean.class) || type.equals(boolean.class);
    }

    @Override
    public String getRandomDefault() {
        return M.r(0.5) + "";
    }
}
