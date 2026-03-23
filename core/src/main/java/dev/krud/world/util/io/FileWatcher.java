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

package dev.krud.world.util.io;

import java.io.File;

public class FileWatcher {
    protected final File file;
    private long lastModified;
    private long size;

    public FileWatcher(File file) {
        this.file = file;
        readProperties();
    }

    protected void readProperties() {
        boolean exists = file.exists();
        lastModified = exists ? file.lastModified() : -1;
        size = exists ? file.isDirectory() ? -2 : file.length() : -1;
    }

    public boolean checkModified() {
        long m = lastModified;
        long g = size;
        boolean mod = false;
        readProperties();

        if (lastModified != m || g != size) {
            mod = true;
        }

        return mod;
    }
}
