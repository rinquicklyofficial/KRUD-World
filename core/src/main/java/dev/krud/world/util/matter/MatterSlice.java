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

package dev.krud.world.util.matter;

import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.util.data.Varint;
import dev.krud.world.util.data.palette.Palette;
import dev.krud.world.util.data.palette.PaletteType;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.hunk.bits.DataContainer;
import dev.krud.world.util.hunk.bits.Writable;
import dev.krud.world.util.hunk.storage.PaletteOrHunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface MatterSlice<T> extends Hunk<T>, PaletteType<T>, Writable<T> {
    Class<T> getType();

    Palette<T> getGlobalPalette();

    @Override
    default void writePaletteNode(DataOutputStream dos, T s) throws IOException {
        writeNode(s, dos);
    }

    @Override
    default void writeNodeData(DataOutputStream dos, T s) throws IOException {
        writeNode(s, dos);
    }

    @Override
    default T readPaletteNode(DataInputStream din) throws IOException {
        return readNode(din);
    }

    @Override
    default T readNodeData(DataInputStream din) throws IOException {
        return readNode(din);
    }

    default void applyFilter(MatterFilter<T> filter) {
        updateSync(filter::update);
    }

    default void inject(MatterSlice<T> slice) {
        iterateSync(slice::set);
    }

    default void forceInject(MatterSlice<?> slice) {
        inject((MatterSlice<T>) slice);
    }

    void writeNode(T b, DataOutputStream dos) throws IOException;

    T readNode(DataInputStream din) throws IOException;

    <W> MatterWriter<W, T> writeInto(Class<W> mediumType);

    <W> MatterReader<W, T> readFrom(Class<W> mediumType);

    default Class<?> getClass(Object w) {
        Class<?> c = w.getClass();

        if (w instanceof World) {
            c = World.class;
        } else if (w instanceof BlockData) {
            c = BlockData.class;
        } else if (w instanceof Entity) {
            c = Entity.class;
        }

        return c;
    }

    default boolean writeInto(Location location) {
        return writeInto(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default <W> boolean writeInto(W w, int x, int y, int z) {
        MatterWriter<W, T> injector = (MatterWriter<W, T>) writeInto(getClass(w));

        if (injector == null) {
            return false;
        }

        iterateSync((a, b, c, t) -> injector.writeMatter(w, t, a + x, b + y, c + z));

        return true;
    }

    default boolean readFrom(Location location) {
        return readFrom(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default <W> boolean readFrom(W w, int x, int y, int z) {
        MatterReader<W, T> ejector = (MatterReader<W, T>) readFrom(getClass(w));

        if (ejector == null) {
            return false;
        }

        for (int i = x; i < x + getWidth(); i++) {
            for (int j = y; j < y + getHeight(); j++) {
                for (int k = z; k < z + getDepth(); k++) {
                    T v = ejector.readMatter(w, i, j, k);

                    if (v != null) {
                        set(i - x, j - y, k - z, v);
                    }
                }
            }
        }

        return true;
    }

    default boolean canWrite(Class<?> mediumType) {
        return writeInto(mediumType) != null;
    }

    default boolean canRead(Class<?> mediumType) {
        return readFrom(mediumType) != null;
    }

    default int getBitsPer(int needed) {
        int target = 1;
        for (int i = 1; i < 8; i++) {
            if (Math.pow(2, i) > needed) {
                target = i;
                break;
            }
        }

        return target;
    }

    default void write(DataOutputStream dos) throws IOException {
        dos.writeUTF(getType().getCanonicalName());

        if ((this instanceof PaletteOrHunk f && f.isPalette())) {
            f.palette().writeDos(dos);
            return;
        }

        int w = getWidth();
        int h = getHeight();
        MatterPalette<T> palette = new MatterPalette<T>(this);
        iterateSync((x, y, z, b) -> palette.assign(b));
        palette.writePalette(dos);
        dos.writeBoolean(isMapped());

        if (isMapped()) {
            Varint.writeUnsignedVarInt(getEntryCount(), dos);
            iterateSyncIO((x, y, z, b) -> {
                Varint.writeUnsignedVarInt(Cache.to1D(x, y, z, w, h), dos);
                palette.writeNode(b, dos);
            });
        } else {
            iterateSyncIO((x, y, z, b) -> palette.writeNode(b, dos));
        }
    }

    default void read(DataInputStream din) throws IOException {
        if ((this instanceof PaletteOrHunk f && f.isPalette())) {
            f.setPalette(new DataContainer<>(din, this));
            return;
        }

        int w = getWidth();
        int h = getHeight();
        MatterPalette<T> palette = new MatterPalette<T>(this, din);
        if (din.readBoolean()) {
            int nodes = Varint.readUnsignedVarInt(din);
            int[] pos;

            while (nodes-- > 0) {
                pos = Cache.to3D(Varint.readUnsignedVarInt(din), w, h);
                setRaw(pos[0], pos[1], pos[2], palette.readNode(din));
            }
        } else {
            iterateSyncIO((x, y, z, b) -> setRaw(x, y, z, palette.readNode(din)));
        }
    }

    default void rotateSliceInto(Matter n, double x, double y, double z) {
        rotate(x, y, z, (_x, _y, _z) -> n.slice(getType()));
    }

    default boolean containsKey(BlockVector v) {
        return get(v.getBlockX(), v.getBlockY(), v.getBlockZ()) != null;
    }

    default void put(BlockVector v, T d) {
        set(v.getBlockX(), v.getBlockY(), v.getBlockZ(), d);
    }

    default T get(BlockVector v) {
        return get(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }
}
