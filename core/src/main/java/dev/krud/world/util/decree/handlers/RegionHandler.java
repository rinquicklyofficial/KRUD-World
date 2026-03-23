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

import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.decree.specialhandlers.RegistrantHandler;

public class RegionHandler extends RegistrantHandler<KrudWorldRegion> {
    public RegionHandler() {
        super(KrudWorldRegion.class, true);
    }

    @Override
    public String getRandomDefault() {
        return "region";
    }
}
