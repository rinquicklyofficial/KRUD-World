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

package dev.krud.world.engine.data.io;

public interface MaxDepthIO {

    default int decrementMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("negative maximum depth is not allowed");
        } else if (maxDepth == 0) {
            throw new MaxDepthReachedException("reached maximum depth of NBT structure");
        }
        return --maxDepth;
    }
}
