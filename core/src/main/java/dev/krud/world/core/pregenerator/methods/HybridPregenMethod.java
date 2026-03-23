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
import org.bukkit.World;

public class HybridPregenMethod implements PregeneratorMethod {
    private final PregeneratorMethod inWorld;
    private final World world;

    public HybridPregenMethod(World world, int threads) {
        this.world = world;
        inWorld = new AsyncOrMedievalPregenMethod(world, threads);
    }

    @Override
    public String getMethod(int x, int z) {
        return "Hybrid<" + inWorld.getMethod(x, z) + ">";
    }

    @Override
    public void init() {
        inWorld.init();
    }

    @Override
    public void close() {
        inWorld.close();
    }

    @Override
    public void save() {
        inWorld.save();
    }

    @Override
    public boolean supportsRegions(int x, int z, PregenListener listener) {
        return inWorld.supportsRegions(x, z, listener);
    }

    @Override
    public void generateRegion(int x, int z, PregenListener listener) {
        inWorld.generateRegion(x, z, listener);
    }

    @Override
    public void generateChunk(int x, int z, PregenListener listener) {
        inWorld.generateChunk(x, z, listener);
    }

    @Override
    public Mantle getMantle() {
        return inWorld.getMantle();
    }
}
