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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.Getter;

import java.util.Objects;

public class KrudWorldMatter extends KrudWorldRegistrant implements Matter {
    protected static final KMap<Class<?>, MatterSlice<?>> slicers = buildSlicers();

    @Getter
    private final MatterHeader header;

    @Getter
    private final int width;

    @Getter
    private final int height;

    @Getter
    private final int depth;

    @Getter
    private final KMap<Class<?>, MatterSlice<?>> sliceMap;

    public KrudWorldMatter(int width, int height, int depth) {
        if (width < 1 || height < 1 || depth < 1) {
            throw new RuntimeException("Invalid Matter Size " + width + "x" + height + "x" + depth);
        }

        this.width = width;
        this.height = height;
        this.depth = depth;
        this.header = new MatterHeader();
        this.sliceMap = new KMap<>();
    }

    private static KMap<Class<?>, MatterSlice<?>> buildSlicers() {
        KMap<Class<?>, MatterSlice<?>> c = new KMap<>();
        for (Object i : KrudWorld.initialize("dev.krud.world.util.matter.slices", Sliced.class)) {
            MatterSlice<?> s = (MatterSlice<?>) i;
            c.put(s.getType(), s);
        }

        return c;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MatterSlice<T> slice(Class<?> c) {
        return (MatterSlice<T>) sliceMap.computeIfAbsent(c, $ -> Objects.requireNonNull(createSlice(c, this), "Bad slice " + c.getCanonicalName()));
    }

    @Override
    public <T> MatterSlice<T> createSlice(Class<T> type, Matter m) {
        MatterSlice<?> slice = slicers.get(type);

        if (slice == null) {
            return null;
        }

        try {
            return slice.getClass().getConstructor(int.class, int.class, int.class).newInstance(getWidth(), getHeight(), getDepth());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getFolderName() {
        return "matter";
    }

    @Override
    public String getTypeName() {
        return "matter";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
