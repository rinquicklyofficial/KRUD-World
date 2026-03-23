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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.service.PreservationSVC;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.MeteredCache;
import dev.krud.world.util.cache.WorldCache2D;
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class CachedStream2D<T> extends BasicStream<T> implements ProceduralStream<T>, MeteredCache {
    private final ProceduralStream<T> stream;
    private final WorldCache2D<T> cache;
    private final Engine engine;
    private final boolean chunked = true;

    public CachedStream2D(String name, Engine engine, ProceduralStream<T> stream, int size) {
        super();
        this.stream = stream;
        this.engine = engine;
        cache = new WorldCache2D<>(stream::get, size);
        KrudWorld.service(PreservationSVC.class).registerCache(this);
    }

    @Override
    public double toDouble(T t) {
        return stream.toDouble(t);
    }

    @Override
    public T fromDouble(double d) {
        return stream.fromDouble(d);
    }

    @Override
    public T get(double x, double z) {
        //return stream.get(x, z);
        return cache.get((int) x, (int) z);
    }

    @Override
    public T get(double x, double y, double z) {
        return stream.get(x, y, z);
    }

    @Override
    public long getSize() {
        return cache.getSize();
    }

    @Override
    public KCache<?, ?> getRawCache() {
        return null;
    }

    @Override
    public long getMaxSize() {
        return cache.getMaxSize();
    }

    @Override
    public boolean isClosed() {
        return engine.isClosed();
    }
}
