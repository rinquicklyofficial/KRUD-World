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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class DelegateStream {

    public static InputStream read(FileChannel channel) throws IOException {
        channel.position(0);
        return new Input(channel);
    }

    public static OutputStream write(FileChannel channel) throws IOException {
        channel.position(0);
        return new Output(channel);
    }

    private static class Input extends InputStream {
        private final InputStream delegate;

        private Input(FileChannel channel) {
            this.delegate = Channels.newInputStream(channel);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public byte @NotNull [] readAllBytes() throws IOException {
            return delegate.readAllBytes();
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            return delegate.readNBytes(b, off, len);
        }

        @Override
        public byte @NotNull [] readNBytes(int len) throws IOException {
            return delegate.readNBytes(len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public void skipNBytes(long n) throws IOException {
            delegate.skipNBytes(n);
        }

        @Override
        public long transferTo(OutputStream out) throws IOException {
            return delegate.transferTo(out);
        }
    }

    private static class Output extends OutputStream {
        private final FileChannel channel;
        private final OutputStream delegate;

        private Output(FileChannel channel) {
            this.channel = channel;
            this.delegate = Channels.newOutputStream(channel);
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            channel.truncate(channel.position());
        }

        @Override
        public void close() throws IOException {
            channel.force(true);
        }
    }
}
