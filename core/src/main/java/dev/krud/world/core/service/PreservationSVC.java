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

package dev.krud.world.core.service;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.MeteredCache;
import dev.krud.world.util.context.KrudWorldContext;
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.scheduling.Looper;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreservationSVC implements KrudWorldService {
    private final List<Thread> threads = new CopyOnWriteArrayList<>();
    private final List<ExecutorService> services = new CopyOnWriteArrayList<>();
    private final List<WeakReference<MeteredCache>> caches = new CopyOnWriteArrayList<>();
    private Looper dereferencer;

    public void register(Thread t) {
        threads.add(t);
    }

    public void register(ExecutorService service) {
        services.add(service);
    }

    public void printCaches() {
        var c = getCaches();
        long s = 0;
        long m = 0;
        double p = 0;
        double mf = Math.max(c.size(), 1);

        for (MeteredCache i : c) {
            s += i.getSize();
            m += i.getMaxSize();
            p += i.getUsage();
        }

        KrudWorld.info("Cached " + Form.f(s) + " / " + Form.f(m) + " (" + Form.pc(p / mf) + ") from " + caches.size() + " Caches");
    }

    public void dereference() {
        KrudWorldContext.dereference();
        KrudWorldData.dereference();
        threads.removeIf((i) -> !i.isAlive());
        services.removeIf(ExecutorService::isShutdown);
        updateCaches();
    }

    @Override
    public void onEnable() {
        /*
         * Dereferences copies of Engine instances that are closed to prevent memory from
         * hanging around and keeping copies of complex, caches and other dead objects.
         */
        dereferencer = new Looper() {
            @Override
            protected long loop() {
                dereference();
                return 60000;
            }
        };
    }

    @Override
    public void onDisable() {
        dereferencer.interrupt();
        dereference();

        postShutdown(() -> {
            for (Thread i : threads) {
                if (i.isAlive()) {
                    try {
                        i.interrupt();
                        KrudWorld.info("Shutdown Thread " + i.getName());
                    } catch (Throwable e) {
                        KrudWorld.reportError(e);
                    }
                }
            }

            for (ExecutorService i : services) {
                try {
                    i.shutdownNow();
                    KrudWorld.info("Shutdown Executor Service " + i);
                } catch (Throwable e) {
                    KrudWorld.reportError(e);
                }
            }
        });
    }

    public void updateCaches() {
        caches.removeIf(ref -> {
            var c = ref.get();
            return c == null || c.isClosed();
        });
    }

    public void registerCache(MeteredCache cache) {
        caches.add(new WeakReference<>(cache));
    }

    public List<KCache<?, ?>> caches() {
        return cacheStream().map(MeteredCache::getRawCache).collect(Collectors.toList());
    }

    @Unmodifiable
    public List<MeteredCache> getCaches() {
        return cacheStream().toList();
    }

    private Stream<MeteredCache> cacheStream() {
        return caches.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .filter(cache -> !cache.isClosed());
    }
}
