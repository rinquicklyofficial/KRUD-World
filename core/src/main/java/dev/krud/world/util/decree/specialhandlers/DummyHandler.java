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

package dev.krud.world.util.decree.specialhandlers;

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.DecreeParameterHandler;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;

public class DummyHandler implements DecreeParameterHandler<Object> {
    @Override
    public KList getPossibilities() {
        return null;
    }

    public boolean isDummy() {
        return true;
    }

    @Override
    public String toString(Object o) {
        return null;
    }

    @Override
    public Object parse(String in, boolean force) throws DecreeParsingException {
        return null;
    }

    @Override
    public boolean supports(Class type) {
        return false;
    }
}
