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

package dev.krud.world.util.stream.utility;

import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

import java.util.concurrent.Semaphore;

public class SemaphoreStream<T> extends BasicStream<T> {
    private final Semaphore semaphore;

    public SemaphoreStream(ProceduralStream<T> stream, int permits) {
        super(stream);
        this.semaphore = new Semaphore(permits);
    }

    @Override
    public double toDouble(T t) {
        return getTypedSource().toDouble(t);
    }

    @Override
    public T fromDouble(double d) {
        return getTypedSource().fromDouble(d);
    }

    @Override
    public T get(double x, double z) {
        try {
            semaphore.acquire();
            T t = getTypedSource().get(x, z);
            semaphore.release();
            return t;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public T get(double x, double y, double z) {
        try {
            semaphore.acquire();
            T t = getTypedSource().get(x, y, z);
            semaphore.release();
            return t;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
