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

package dev.krud.world.util.math;

import dev.krud.world.util.scheduling.Wrapper;

/**
 * Represents a number that can be finalized and be changed
 *
 * @author cyberpwn
 */
public class FinalInteger extends Wrapper<Integer> {
    public FinalInteger(Integer t) {
        super(t);
    }

    /**
     * Add to this value
     *
     * @param i the number to add to this value (value = value + i)
     */
    public void add(int i) {
        set(get() + i);
    }

    /**
     * Subtract from this value
     *
     * @param i the number to subtract from this value (value = value - i)
     */
    public void sub(int i) {
        set(get() - i);
    }
}
