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

package dev.krud.world.util.plugin;

import dev.krud.world.KrudWorld;

public abstract class Controller implements IController {
    private final String name;
    private int tickRate;

    public Controller() {
        name = getClass().getSimpleName().replaceAll("Controller", "") + " Controller";
        tickRate = -1;
    }

    protected void setTickRate(@SuppressWarnings("SameParameterValue") int rate) {
        this.tickRate = rate;
    }

    protected void disableTicking() {
        setTickRate(-1);
    }

    @Override
    public void l(Object l) {
        KrudWorld.info("[" + getName() + "]: " + l);
    }

    @Override
    public void w(Object l) {
        KrudWorld.warn("[" + getName() + "]: " + l);
    }

    @Override
    public void f(Object l) {
        KrudWorld.error("[" + getName() + "]: " + l);
    }

    @Override
    public void v(Object l) {
        KrudWorld.verbose("[" + getName() + "]: " + l);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public abstract void start();

    @Override
    public abstract void stop();

    @Override
    public abstract void tick();

    @Override
    public int getTickInterval() {
        return tickRate;
    }
}
