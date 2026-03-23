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

import dev.krud.world.util.math.RNG;

import java.io.File;
import java.io.IOException;

public class InstanceState {
    public static int getInstanceId() {
        try {
            return Integer.parseInt(IO.readAll(instanceFile()).trim());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void updateInstanceId() {
        try {
            IO.writeAll(instanceFile(), RNG.r.imax() + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File instanceFile() {
        File f = new File("plugins/KrudWorld/cache/instance");
        f.getParentFile().mkdirs();
        return f;
    }
}
