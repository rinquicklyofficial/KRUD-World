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

public final class EndTag extends Tag<Void> {

    public static final byte ID = 0;
    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
        super(null);
    }

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    protected Void checkValue(Void value) {
        return value;
    }

    @Override
    public String valueToString(int maxDepth) {
        return "\"end\"";
    }

    @Override
    public EndTag clone() {
        return INSTANCE;
    }
}
