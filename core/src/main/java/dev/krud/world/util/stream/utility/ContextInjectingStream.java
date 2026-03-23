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

import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.context.KrudWorldContext;
import dev.krud.world.util.function.Function3;
import dev.krud.world.util.stream.BasicStream;
import dev.krud.world.util.stream.ProceduralStream;

public class ContextInjectingStream<T> extends BasicStream<T> {
    private final Function3<ChunkContext, Integer, Integer, T> contextAccessor;

    public ContextInjectingStream(ProceduralStream<T> stream, Function3<ChunkContext, Integer, Integer, T> contextAccessor) {
        super(stream);
        this.contextAccessor = contextAccessor;
    }

    @Override
    public T get(double x, double z) {
        KrudWorldContext context = KrudWorldContext.get();

        if (context != null) {
            ChunkContext chunkContext = context.getChunkContext();

            if (chunkContext != null && (int) x >> 4 == chunkContext.getX() >> 4 && (int) z >> 4 == chunkContext.getZ() >> 4) {
                T t = contextAccessor.apply(chunkContext, (int) x & 15, (int) z & 15);

                if (t != null) {
                    return t;
                }
            }
        }

        return getTypedSource().get(x, z);
    }

    @Override
    public T get(double x, double y, double z) {
        return getTypedSource().get(x, y, z);
    }

    @Override
    public double toDouble(T t) {
        return getTypedSource().toDouble(t);
    }

    @Override
    public T fromDouble(double d) {
        return getTypedSource().fromDouble(d);
    }
}
