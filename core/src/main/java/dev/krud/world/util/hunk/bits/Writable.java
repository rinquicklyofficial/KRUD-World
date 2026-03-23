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

package dev.krud.world.util.hunk.bits;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Writable<T> {
    T readNodeData(DataInputStream din) throws IOException;

    void writeNodeData(DataOutputStream dos, T t) throws IOException;
}
