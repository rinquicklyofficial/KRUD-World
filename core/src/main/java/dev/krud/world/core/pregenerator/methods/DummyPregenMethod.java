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

package dev.krud.world.core.pregenerator.methods;

import dev.krud.world.core.pregenerator.PregenListener;
import dev.krud.world.core.pregenerator.PregeneratorMethod;
import dev.krud.world.util.mantle.Mantle;

public class DummyPregenMethod implements PregeneratorMethod {
    @Override
    public void init() {

    }

    @Override
    public void close() {

    }

    @Override
    public String getMethod(int x, int z) {
        return "Dummy";
    }

    @Override
    public void save() {

    }

    @Override
    public boolean supportsRegions(int x, int z, PregenListener listener) {
        return false;
    }

    @Override
    public void generateRegion(int x, int z, PregenListener listener) {

    }

    @Override
    public void generateChunk(int x, int z, PregenListener listener) {

    }

    @Override
    public Mantle getMantle() {
        return null;
    }
}
