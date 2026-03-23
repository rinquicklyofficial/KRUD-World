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

package dev.krud.world.util.nbt.tag;

public abstract class NumberTag<T extends Number & Comparable<T>> extends Tag<T> {

    public NumberTag(T value) {
        super(value);
    }

    public byte asByte() {
        return getValue().byteValue();
    }

    public short asShort() {
        return getValue().shortValue();
    }

    public int asInt() {
        return getValue().intValue();
    }

    public long asLong() {
        return getValue().longValue();
    }

    public float asFloat() {
        return getValue().floatValue();
    }

    public double asDouble() {
        return getValue().doubleValue();
    }

    @Override
    public String valueToString(int maxDepth) {
        return getValue().toString();
    }
}
