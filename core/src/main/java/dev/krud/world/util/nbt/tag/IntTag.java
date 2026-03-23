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

public class IntTag extends NumberTag<Integer> implements Comparable<IntTag> {

    public static final byte ID = 3;
    public static final int ZERO_VALUE = 0;

    public IntTag() {
        super(ZERO_VALUE);
    }

    public IntTag(int value) {
        super(value);
    }

    @Override
    public byte getID() {
        return ID;
    }

    public void setValue(int value) {
        super.setValue(value);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && asInt() == ((IntTag) other).asInt();
    }

    @Override
    public int compareTo(IntTag other) {
        return getValue().compareTo(other.getValue());
    }

    @Override
    public IntTag clone() {
        return new IntTag(getValue());
    }
}
