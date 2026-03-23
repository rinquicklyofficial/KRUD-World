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

package dev.krud.world.util.network;

import java.io.IOException;

public class DownloadException extends IOException {
    private static final long serialVersionUID = 5137918663903349839L;

    public DownloadException() {
        super();
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(Throwable cause) {
        super(cause);
    }
}