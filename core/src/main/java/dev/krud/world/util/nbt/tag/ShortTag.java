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

public class ShortTag extends NumberTag<Short> implements Comparable<ShortTag> {

    public static final byte ID = 2;
    public static final short ZERO_VALUE = 0;

    public ShortTag() {
        super(ZERO_VALUE);
    }

    public ShortTag(short value) {
        super(value);
    }

    @Override
    public byte getID() {
        return ID;
    }

    public void setValue(short value) {
        super.setValue(value);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && asShort() == ((ShortTag) other).asShort();
    }

    @Override
    public int compareTo(ShortTag other) {
        return getValue().compareTo(other.getValue());
    }

    @Override
    public ShortTag clone() {
        return new ShortTag(getValue());
    }
}
