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

public class DoubleTag extends NumberTag<Double> implements Comparable<DoubleTag> {

    public static final byte ID = 6;
    public static final double ZERO_VALUE = 0.0D;

    public DoubleTag() {
        super(ZERO_VALUE);
    }

    public DoubleTag(double value) {
        super(value);
    }

    @Override
    public byte getID() {
        return ID;
    }

    public void setValue(double value) {
        super.setValue(value);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && getValue().equals(((DoubleTag) other).getValue());
    }

    @Override
    public int compareTo(DoubleTag other) {
        return getValue().compareTo(other.getValue());
    }

    @Override
    public DoubleTag clone() {
        return new DoubleTag(getValue());
    }
}
