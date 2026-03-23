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

public class LongTag extends NumberTag<Long> implements Comparable<LongTag> {

    public static final byte ID = 4;
    public static final long ZERO_VALUE = 0L;

    public LongTag() {
        super(ZERO_VALUE);
    }

    public LongTag(long value) {
        super(value);
    }

    @Override
    public byte getID() {
        return ID;
    }

    public void setValue(long value) {
        super.setValue(value);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && asLong() == ((LongTag) other).asLong();
    }

    @Override
    public int compareTo(LongTag other) {
        return getValue().compareTo(other.getValue());
    }

    @Override
    public LongTag clone() {
        return new LongTag(getValue());
    }
}
