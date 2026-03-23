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

package dev.krud.world.util.context;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.KrudWorldComplex;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.scheduling.ChronoLatch;
import lombok.Data;

@Data
public class KrudWorldContext {
    private static final KMap<Thread, KrudWorldContext> context = new KMap<>();
    private static final ChronoLatch cl = new ChronoLatch(60000);
    private final Engine engine;
    private ChunkContext chunkContext;

    public KrudWorldContext(Engine engine) {
        this.engine = engine;
    }

    public static KrudWorldContext getOr(Engine engine) {
        KrudWorldContext c = get();

        if (c == null) {
            c = new KrudWorldContext(engine);
            touch(c);
        }

        return c;
    }

    public static KrudWorldContext get() {
        return context.get(Thread.currentThread());
    }

    public static void touch(KrudWorldContext c) {
        context.put(Thread.currentThread(), c);

        if (!cl.couldFlip()) return;
        synchronized (cl) {
            if (cl.flip()) {
                dereference();
            }
        }
    }

    public static synchronized void dereference() {
        var it = context.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var thread = entry.getKey();
            var context = entry.getValue();
            if (thread == null || context == null) {
                it.remove();
                continue;
            }

            if (!thread.isAlive() || context.engine.isClosed()) {
                KrudWorld.debug("Dereferenced Context<Engine> " + thread.getName() + " " + thread.threadId());
                it.remove();
            }
        }
    }

    public void touch() {
        KrudWorldContext.touch(this);
    }

    public KrudWorldData getData() {
        return engine.getData();
    }

    public KrudWorldComplex getComplex() {
        return engine.getComplex();
    }

    public KMap<String, Object> asContext() {
        var hash32 = engine.getHash32().getNow(null);
        var dimension = engine.getDimension();
        var mantle = engine.getMantle();
        return new KMap<String, Object>()
                .qput("studio", engine.isStudio())
                .qput("closed", engine.isClosed())
                .qput("pack", new KMap<>()
                        .qput("key", dimension == null ? "" : dimension.getLoadKey())
                        .qput("version", dimension == null ? "" : dimension.getVersion())
                        .qput("hash", hash32 == null ? "" : Long.toHexString(hash32)))
                .qput("mantle", new KMap<>()
                        .qput("idle", mantle.getAdjustedIdleDuration())
                        .qput("loaded", mantle.getLoadedRegionCount())
                        .qput("queued", mantle.getUnloadRegionCount()));
    }
}
