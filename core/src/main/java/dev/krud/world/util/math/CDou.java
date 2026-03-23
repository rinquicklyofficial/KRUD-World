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

@SuppressWarnings("ALL")
public class CDou {
    private final double max;
    private double number;

    public CDou(double max) {
        number = 0;
        this.max = max;
    }

    public CDou set(double n) {
        number = n;
        circ();
        return this;
    }

    public CDou add(double a) {
        number += a;
        circ();
        return this;
    }

    public CDou sub(double a) {
        number -= a;
        circ();
        return this;
    }

    public double get() {
        return number;
    }

    public void circ() {
        if (number < 0) {
            number = max - (Math.abs(number) > max ? max : Math.abs(number));
        }

        number = number % (max);
    }
}
