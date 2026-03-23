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

package dev.krud.world.util.stream.interpolation;

import dev.krud.world.util.stream.ProceduralStream;

public interface Interpolator<T> {
    @SuppressWarnings("unchecked")
    default InterpolatorFactory<T> into() {
        if (this instanceof ProceduralStream) {
            return new InterpolatorFactory<>((ProceduralStream<T>) this);
        }

        return null;
    }
}
