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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.Semaphore;

public class SynchronizedChannel implements Closeable {
    private final FileChannel channel;
    private final Semaphore lock;
    private transient boolean closed;

    SynchronizedChannel(FileChannel channel, Semaphore lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public InputStream read() throws IOException {
        if (closed) throw new IOException("Channel is closed!");
        return DelegateStream.read(channel);
    }

    public OutputStream write() throws IOException {
        if (closed) throw new IOException("Channel is closed!");
        return DelegateStream.write(channel);
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        lock.release();
    }
}
