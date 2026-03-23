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

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.function.Consumer3;

import java.io.File;

public class ReactiveFolder {
    private final File folder;
    private final Consumer3<KList<File>, KList<File>, KList<File>> hotload;
    private FolderWatcher fw;
    private int checkCycle = 0;

    public ReactiveFolder(File folder, Consumer3<KList<File>, KList<File>, KList<File>> hotload) {
        this.folder = folder;
        this.hotload = hotload;
        this.fw = new FolderWatcher(folder);
        fw.checkModified();
    }

    public void checkIgnore() {
        fw = new FolderWatcher(folder);
    }

    public boolean check() {
        checkCycle++;
        boolean modified = false;

        if (checkCycle % 3 == 0 ? fw.checkModified() : fw.checkModifiedFast()) {
            for (File i : fw.getCreated()) {
                if (i.getName().endsWith(".iob") || i.getName().endsWith(".json") || i.getName().endsWith(".kts")) {
                    if (i.getPath().contains(".iris") || i.getName().endsWith(".gradle.kts")) {
                        continue;
                    }

                    modified = true;
                    break;
                }
            }

            if (!modified) {
                for (File i : fw.getChanged()) {
                    if (i.getPath().contains(".iris") || i.getName().endsWith(".gradle.kts")) {
                        continue;
                    }

                    if (i.getName().endsWith(".iob") || i.getName().endsWith(".json") || i.getName().endsWith(".kts")) {
                        modified = true;
                        break;
                    }
                }
            }

            if (!modified) {
                for (File i : fw.getDeleted()) {
                    if (i.getPath().contains(".iris") || i.getName().endsWith(".gradle.kts")) {
                        continue;
                    }

                    if (i.getName().endsWith(".iob") || i.getName().endsWith(".json") || i.getName().endsWith(".kts")) {
                        modified = true;
                        break;
                    }
                }
            }
        }

        if (modified) {
            hotload.accept(fw.getCreated(), fw.getChanged(), fw.getDeleted());
        }

        return fw.checkModified();
    }

    public void clear() {
        fw.clear();
    }
}
