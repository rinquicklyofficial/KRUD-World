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
import io.papermc.lib.PaperLib;
import org.bukkit.World;

public class AsyncOrMedievalPregenMethod implements PregeneratorMethod {
    private final PregeneratorMethod method;

    public AsyncOrMedievalPregenMethod(World world, int threads) {
        method = PaperLib.isPaper() ? new AsyncPregenMethod(world, threads) : new MedievalPregenMethod(world);
    }

    @Override
    public void init() {
        method.init();
    }

    @Override
    public void close() {
        method.close();
    }

    @Override
    public void save() {
        method.save();
    }

    @Override
    public String getMethod(int x, int z) {
        return method.getMethod(x, z);
    }

    @Override
    public boolean supportsRegions(int x, int z, PregenListener listener) {
        return false;
    }

    @Override
    public void generateRegion(int x, int z, PregenListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void generateChunk(int x, int z, PregenListener listener) {
        method.generateChunk(x, z, listener);
    }

    @Override
    public Mantle getMantle() {
        return method.getMantle();
    }
}
