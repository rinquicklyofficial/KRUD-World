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

package dev.krud.world.util.exceptions;

public class KrudWorldException extends Exception {
    public KrudWorldException() {
        super();
    }

    public KrudWorldException(String message) {
        super(message);
    }

    public KrudWorldException(Throwable message) {
        super(message);
    }

    public KrudWorldException(String message, Throwable e) {
        super(message, e);
    }
}
