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

import dev.krud.world.util.io.IO;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.Semaphore;

class Holder {
    private final FileChannel channel;
    private final Semaphore semaphore = new Semaphore(1);
    private volatile boolean closed;

    Holder(FileChannel channel) throws IOException {
        this.channel = channel;
        IO.lock(channel);
    }

    SynchronizedChannel acquire() {
        semaphore.acquireUninterruptibly();
        if (closed) {
            semaphore.release();
            return null;
        }

        return new SynchronizedChannel(channel, semaphore);
    }

    void close() throws IOException {
        semaphore.acquireUninterruptibly();
        try {
            if (closed) return;
            closed = true;
            channel.close();
        } finally {
            semaphore.release();
        }
    }
}
