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

public class StringTag extends Tag<String> implements Comparable<StringTag> {

    public static final byte ID = 8;
    public static final String ZERO_VALUE = "";

    public StringTag() {
        super(ZERO_VALUE);
    }

    public StringTag(String value) {
        super(value);
    }

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    public String valueToString(int maxDepth) {
        return escapeString(getValue(), false);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && getValue().equals(((StringTag) other).getValue());
    }

    @Override
    public int compareTo(StringTag o) {
        return getValue().compareTo(o.getValue());
    }

    @Override
    public StringTag clone() {
        return new StringTag(getValue());
    }
}
