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

package dev.krud.world.core.nms.datapack;

import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.nms.datapack.v1192.DataFixerV1192;
import dev.krud.world.core.nms.datapack.v1206.DataFixerV1206;
import dev.krud.world.core.nms.datapack.v1213.DataFixerV1213;
import dev.krud.world.core.nms.datapack.v1217.DataFixerV1217;
import dev.krud.world.util.collection.KMap;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.function.Supplier;

//https://minecraft.wiki/w/Pack_format
@Getter
public enum DataVersion {
    UNSUPPORTED("0.0.0", 0, () -> null),
    V1_19_2("1.19.2", 10, DataFixerV1192::new),
    V1_20_5("1.20.6", 41, DataFixerV1206::new),
    V1_21_3("1.21.3", 57, DataFixerV1213::new),
    V1_21_11("1.21.11", 75, DataFixerV1217::new);
    private static final KMap<DataVersion, IDataFixer> cache = new KMap<>();
    @Getter(AccessLevel.NONE)
    private final Supplier<IDataFixer> constructor;
    private final String version;
    private final int packFormat;

    DataVersion(String version, int packFormat, Supplier<IDataFixer> constructor) {
        this.constructor = constructor;
        this.packFormat = packFormat;
        this.version = version;
    }

    public IDataFixer get() {
        return cache.computeIfAbsent(this, k -> constructor.get());
    }

    public static IDataFixer getDefault() {
        return INMS.get().getDataVersion().get();
    }

    public static DataVersion getLatest() {
        return values()[values().length - 1];
    }
}
