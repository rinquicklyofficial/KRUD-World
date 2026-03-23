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
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.math.BlockPosition;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class CachedStream3D<T> extends BasicStream<T> implements ProceduralStream<T>, MeteredCache {
    private final ProceduralStream<T> stream;
    private final KCache<BlockPosition, T> cache;
    private final Engine engine;

    public CachedStream3D(String name, Engine engine, ProceduralStream<T> stream, int size) {
        super();
        this.stream = stream;
        this.engine = engine;
        cache = new KCache<>((k) -> stream.get(k.getX(), k.getY(), k.getZ()), size);
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
        return cache.get(new BlockPosition((int) x, 0, (int) z));
    }

    @Override
    public T get(double x, double y, double z) {
        return cache.get(new BlockPosition((int) x, (int) y, (int) z));
    }

    @Override
    public long getSize() {
        return cache.getSize();
    }

    @Override
    public KCache<?, ?> getRawCache() {
        return cache;
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
