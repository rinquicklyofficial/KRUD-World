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

package dev.krud.world.util.mantle;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.documentation.ChunkRelativeBlockCoordinates;
import dev.krud.world.util.function.Consumer4;
import dev.krud.world.util.io.CountingDataInputStream;
import dev.krud.world.util.matter.KrudWorldMatter;
import dev.krud.world.util.matter.Matter;
import dev.krud.world.util.matter.MatterSlice;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Represents a mantle chunk. Mantle chunks contain sections of matter (see matter api)
 * Mantle Chunks are fully atomic & thread safe
 */
public class MantleChunk extends FlaggedChunk {
    @Getter
    private final int x;
    @Getter
    private final int z;
    private final AtomicReferenceArray<Matter> sections;
    private final Semaphore ref = new Semaphore(Integer.MAX_VALUE, true);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Create a mantle chunk
     *
     * @param sectionHeight the height of the world in sections (blocks >> 4)
     */
    @ChunkCoordinates
    public MantleChunk(int sectionHeight, int x, int z) {
        sections = new AtomicReferenceArray<>(sectionHeight);
        this.x = x;
        this.z = z;
    }

    /**
     * Load a mantle chunk from a data stream
     *
     * @param sectionHeight the height of the world in sections (blocks >> 4)
     * @param din           the data input
     * @throws IOException            shit happens
     * @throws ClassNotFoundException shit happens
     */
    public MantleChunk(int version, int sectionHeight, CountingDataInputStream din) throws IOException {
        this(sectionHeight, din.readByte(), din.readByte());
        int s = din.readByte();
        readFlags(version, din);

        for (int i = 0; i < s; i++) {
            KrudWorld.addPanic("read.section", "Section[" + i + "]");
            long size = din.readInt();
            if (size == 0) continue;
            long start = din.count();
            if (i >= sectionHeight) {
                din.skipTo(start + size);
                continue;
            }

            try {
                sections.set(i, Matter.readDin(din));
            } catch (IOException e) {
                long end = start + size;
                KrudWorld.error("Failed to read chunk section, skipping it.");
                KrudWorld.addPanic("read.byte.range", start + " " + end);
                KrudWorld.addPanic("read.byte.current", din.count() + "");
                KrudWorld.reportError(e);
                e.printStackTrace();
                KrudWorld.panic();

                din.skipTo(end);
                TectonicPlate.addError();
            }
            if (din.count() != start + size) {
                throw new IOException("Chunk section read size mismatch!");
            }
        }
    }

    @SneakyThrows
    public void close() {
        closed.set(true);
        ref.acquire(Integer.MAX_VALUE);
        ref.release(Integer.MAX_VALUE);
    }

    public boolean inUse() {
        return ref.availablePermits() < Integer.MAX_VALUE;
    }

    public MantleChunk use() {
        if (closed.get()) throw new IllegalStateException("Chunk is closed!");
        ref.acquireUninterruptibly();
        if (closed.get()) {
            ref.release();
            throw new IllegalStateException("Chunk is closed!");
        }
        return this;
    }

    public void release() {
        ref.release();
    }

    public void copyFrom(MantleChunk chunk) {
        use();
        super.copyFrom(chunk, () -> {
            for (int i = 0; i < sections.length(); i++) {
                sections.set(i, chunk.get(i));
            }
        });
        release();
    }

    /**
     * Check if a section exists (same as get(section) != null)
     *
     * @param section the section (0 - (worldHeight >> 4))
     * @return true if it exists
     */
    @ChunkCoordinates
    public boolean exists(int section) {
        return get(section) != null;
    }

    /**
     * Get thje matter at the given section or null if it doesnt exist
     *
     * @param section the section (0 - (worldHeight >> 4))
     * @return the matter or null if it doesnt exist
     */
    @ChunkCoordinates
    public Matter get(int section) {
        return sections.get(section);
    }

    @Nullable
    @ChunkRelativeBlockCoordinates
    @SuppressWarnings("unchecked")
    public <T> T get(int x, int y, int z, Class<T> type) {
        return (T) getOrCreate(y >> 4)
                .slice(type)
                .get(x & 15, y & 15, z & 15);
    }

    /**
     * Clear all matter from this chunk
     */
    public void clear() {
        for (int i = 0; i < sections.length(); i++) {
            delete(i);
        }
    }

    /**
     * Delete the matter from the given section
     *
     * @param section the section (0 - (worldHeight >> 4))
     */
    @ChunkCoordinates
    public void delete(int section) {
        sections.set(section, null);
    }

    /**
     * Get or create a new matter section at the given section
     *
     * @param section the section (0 - (worldHeight >> 4))
     * @return the matter
     */
    @ChunkCoordinates
    public Matter getOrCreate(int section) {
        final Matter matter = get(section);
        if (matter != null) return matter;

        final Matter instance = new KrudWorldMatter(16, 16, 16);
        final Matter value = sections.compareAndExchange(section, null, instance);
        return value == null ? instance : value;
    }

    /**
     * Write this chunk to a data stream
     *
     * @param dos the stream
     * @throws IOException shit happens
     */
    public void write(DataOutputStream dos) throws IOException {
        close();
        dos.writeByte(x);
        dos.writeByte(z);
        dos.writeByte(sections.length());
        writeFlags(dos);

        var bytes = new ByteArrayOutputStream(8192);
        var sub = new DataOutputStream(bytes);
        for (int i = 0; i < sections.length(); i++) {
            trimSlice(i);

            if (exists(i)) {
                try {
                    Matter matter = get(i);
                    matter.writeDos(sub);
                    dos.writeInt(bytes.size());
                    bytes.writeTo(dos);
                } finally {
                    bytes.reset();
                }
            } else {
                dos.writeInt(0);
            }
        }
    }

    private void trimSlice(int i) {
        if (exists(i)) {
            Matter m = get(i);

            if (m.getSliceMap().isEmpty()) {
                sections.set(i, null);
            } else {
                m.trimSlices();
                if (m.getSliceMap().isEmpty()) {
                    sections.set(i, null);
                }
            }
        }
    }

    public <T> void iterate(Class<T> type, Consumer4<Integer, Integer, Integer, T> iterator) {
        for (int i = 0; i < sections.length(); i++) {
            int bs = (i << 4);
            Matter matter = get(i);

            if (matter != null) {
                MatterSlice<T> t = matter.getSlice(type);

                if (t != null) {
                    t.iterateSync((a, b, c, f) -> iterator.accept(a, b + bs, c, f));
                }
            }
        }
    }

    public void deleteSlices(Class<?> c) {
        if (KrudWorldToolbelt.isRetainingMantleDataForSlice(c.getCanonicalName()))
            return;
        for (int i = 0; i < sections.length(); i++) {
            Matter m = sections.get(i);
            if (m != null && m.hasSlice(c)) {
                m.deleteSlice(c);
            }
        }
    }

    public void trimSlices() {
        for (int i = 0; i < sections.length(); i++) {
            if (exists(i)) {
                trimSlice(i);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }
}
