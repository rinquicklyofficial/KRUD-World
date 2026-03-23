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

package dev.krud.world.engine.mantle;

import dev.krud.world.util.mantle.flag.MantleFlag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = "engineMantle")
@ToString(exclude = "engineMantle")
public abstract class KrudWorldMantleComponent implements MantleComponent {
    private final EngineMantle engineMantle;
    private final MantleFlag flag;
    private final int priority;

    private volatile int radius = -1;
    private final Object lock = new Object();
    private boolean enabled = true;

    protected abstract int computeRadius();

    @Override
    public void hotload() {
        synchronized (lock) {
            radius = -1;
        }
    }

    @Override
    public final int getRadius() {
        int r = radius;
        if(r != -1) return r;

        synchronized (lock) {
            if((r = radius) != -1) {
                return r;
            }
            r = computeRadius();
            if(r < 0) r = 0;
            return radius = r;
        }
    }
}
