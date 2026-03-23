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

public class ByteTag extends NumberTag<Byte> implements Comparable<ByteTag> {

    public static final byte ID = 1;
    public static final byte ZERO_VALUE = 0;

    public ByteTag() {
        super(ZERO_VALUE);
    }

    public ByteTag(byte value) {
        super(value);
    }

    public ByteTag(boolean value) {
        super((byte) (value ? 1 : 0));
    }

    @Override
    public byte getID() {
        return ID;
    }

    public boolean asBoolean() {
        return getValue() > 0;
    }

    public void setValue(byte value) {
        super.setValue(value);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && asByte() == ((ByteTag) other).asByte();
    }

    @Override
    public int compareTo(ByteTag other) {
        return getValue().compareTo(other.getValue());
    }

    @Override
    public ByteTag clone() {
        return new ByteTag(getValue());
    }
}
