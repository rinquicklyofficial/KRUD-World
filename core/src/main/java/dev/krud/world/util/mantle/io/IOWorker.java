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

package dev.krud.world.util.mantle.io;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.io.CountingDataInputStream;
import dev.krud.world.util.mantle.TectonicPlate;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Objects;
import java.util.Set;

public class IOWorker {
    private static final Set<OpenOption> OPTIONS = Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.SYNC);
    private static final int MAX_CACHE_SIZE = 128;

    private final Path root;
    private final File tmp;
    private final int worldHeight;

    private final Object2ObjectLinkedOpenHashMap<String, Holder> cache = new Object2ObjectLinkedOpenHashMap<>();

    public IOWorker(File root, int worldHeight) {
        this.root = root.toPath();
        this.tmp = new File(root, ".tmp");
        this.worldHeight = worldHeight;
    }

    public TectonicPlate read(final String name) throws IOException {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        try (var channel = getChannel(name)) {
            var raw = channel.read();
            var lz4 = new LZ4BlockInputStream(raw);
            var buffered = new BufferedInputStream(lz4);
            try (var in = CountingDataInputStream.wrap(buffered)) {
                return new TectonicPlate(worldHeight, in, name.startsWith("pv."));
            } finally {
                if (TectonicPlate.hasError() && KrudWorldSettings.get().getGeneral().isDumpMantleOnError()) {
                    File dump = KrudWorld.instance.getDataFolder("dump", name + ".bin");
                    Files.copy(new LZ4BlockInputStream(channel.read()), dump.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    KrudWorld.debug("Read Tectonic Plate " + C.DARK_GREEN + name + C.RED + " in " + Form.duration(p.getMilliseconds(), 2));
                }
            }
        }
    }

    public void write(final String name, final TectonicPlate plate) throws IOException {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        try (var channel = getChannel(name)) {
            tmp.mkdirs();
            File file = File.createTempFile("iris", ".bin", tmp);
            try {
                try (var tmp = new DataOutputStream(new LZ4BlockOutputStream(new FileOutputStream(file)))) {
                    plate.write(tmp);
                }

                try (var out = channel.write()) {
                    Files.copy(file.toPath(), out);
                    out.flush();
                }
            } finally {
                file.delete();
            }
        }
        KrudWorld.debug("Saved Tectonic Plate " + C.DARK_GREEN + name + C.RED + " in " + Form.duration(p.getMilliseconds(), 2));
    }

    public void close() throws IOException {
        synchronized (cache) {
            for (Holder h : cache.values()) {
                h.close();
            }

            cache.clear();
        }
    }

    private SynchronizedChannel getChannel(final String name) throws IOException {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        try {
            synchronized (cache) {
                Holder holder = cache.getAndMoveToFirst(name);
                if (holder != null) {
                    var channel = holder.acquire();
                    if (channel != null) {
                        return channel;
                    }
                }

                if (cache.size() >= MAX_CACHE_SIZE) {
                    var last = cache.removeLast();
                    last.close();
                }


                holder = new Holder(FileChannel.open(root.resolve(name), OPTIONS));
                cache.putAndMoveToFirst(name, holder);
                return Objects.requireNonNull(holder.acquire());
            }
        } finally {
            KrudWorld.debug("Acquired Channel for " + C.DARK_GREEN + name + C.RED + " in " + Form.duration(p.getMilliseconds(), 2));
        }
    }
}
