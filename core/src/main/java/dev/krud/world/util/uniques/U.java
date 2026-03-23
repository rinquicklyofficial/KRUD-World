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

package dev.krud.world.util.uniques;

import java.io.File;

public class U {
    public static void main(String[] a) {
        UniqueRenderer r = new UniqueRenderer("helloworld", 2560, 1440);

        r.writeCollectionFrames(new File("collection"), 1, 1024);

        System.exit(0);
    }
}
