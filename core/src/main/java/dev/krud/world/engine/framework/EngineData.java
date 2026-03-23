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

package dev.krud.world.engine.framework;

import com.google.gson.Gson;
import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.KrudWorldPosition;
import dev.krud.world.util.io.IO;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
public class EngineData {
    private String dimension;
    private String lastVersion;
    private List<KrudWorldPosition> strongholdPositions;

    public static EngineData load(File f) {
        try {
            f.getParentFile().mkdirs();
            return new Gson().fromJson(IO.readAll(f), EngineData.class);
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }

        return new EngineData();
    }

    public void save(File f) {
        try {
            f.getParentFile().mkdirs();
            IO.writeAll(f, new Gson().toJson(this));
        } catch (IOException e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }
    }
}
