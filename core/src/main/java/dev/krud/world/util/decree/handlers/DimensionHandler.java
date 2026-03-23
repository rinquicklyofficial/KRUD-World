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

import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;
import dev.krud.world.util.decree.specialhandlers.RegistrantHandler;

public class DimensionHandler extends RegistrantHandler<KrudWorldDimension> {
    public DimensionHandler() {
        super(KrudWorldDimension.class, false);
    }

    @Override
    public KrudWorldDimension parse(String in, boolean force) throws DecreeParsingException {
        if (in.equalsIgnoreCase("default")) {
            return parse(KrudWorldSettings.get().getGenerator().getDefaultWorldType());
        }
        return super.parse(in, force);
    }

    @Override
    public String getRandomDefault() {
        return "dimension";
    }
}
