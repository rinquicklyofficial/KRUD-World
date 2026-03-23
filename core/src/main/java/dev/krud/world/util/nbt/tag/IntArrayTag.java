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

import java.util.Arrays;

public class IntArrayTag extends ArrayTag<int[]> implements Comparable<IntArrayTag> {

    public static final byte ID = 11;
    public static final int[] ZERO_VALUE = new int[0];

    public IntArrayTag() {
        super(ZERO_VALUE);
    }

    public IntArrayTag(int[] value) {
        super(value);
    }

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && Arrays.equals(getValue(), ((IntArrayTag) other).getValue());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getValue());
    }

    @Override
    public int compareTo(IntArrayTag other) {
        return Integer.compare(length(), other.length());
    }

    @Override
    public IntArrayTag clone() {
        return new IntArrayTag(Arrays.copyOf(getValue(), length()));
    }
}
