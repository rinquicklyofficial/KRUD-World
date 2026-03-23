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

package dev.krud.world.util.parallel;

import dev.krud.world.util.function.NastyRunnable;
import dev.krud.world.util.io.IORunnable;

import java.io.IOException;
import java.util.function.Supplier;

public class NOOPGridLock extends GridLock {
    public NOOPGridLock(int x, int z) {
        super(x, z);
    }

    @Override
    public void with(int x, int z, Runnable r) {
        r.run();
    }

    @Override
    public void withNasty(int x, int z, NastyRunnable r) throws Throwable {
        r.run();
    }

    @Override
    public void withIO(int x, int z, IORunnable r) throws IOException {
        r.run();
    }

    @Override
    public <T> T withResult(int x, int z, Supplier<T> r) {
        return r.get();
    }

    @Override
    public void withAll(Runnable r) {
        r.run();
    }

    @Override
    public <T> T withAllResult(Supplier<T> r) {
        return r.get();
    }

    @Override
    public boolean tryLock(int x, int z) {
        return true;
    }

    @Override
    public boolean tryLock(int x, int z, long timeout) {
        return true;
    }

    @Override
    public void lock(int x, int z) {

    }

    @Override
    public void unlock(int x, int z) {

    }
}
