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

package dev.krud.world.util.stream;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasicLayer implements ProceduralLayer {
    private final long seed;
    private final double zoom;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public BasicLayer(long seed, double zoom) {
        this(seed, zoom, 0D, 0D, 0D);
    }

    public BasicLayer(long seed) {
        this(seed, 1D);
    }

    public BasicLayer() {
        this(1337);
    }
}
