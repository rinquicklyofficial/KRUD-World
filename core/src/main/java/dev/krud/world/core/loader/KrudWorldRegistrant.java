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

package dev.krud.world.core.loader;

import com.google.gson.GsonBuilder;
import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.KrudWorldScript;
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.RegistryListResource;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.io.File;

@Data
public abstract class KrudWorldRegistrant {
    @Desc("Preprocess this object in-memory when it's loaded, run scripts using the variable 'object' and modify properties about this object before it's used.\nFile extension: .proc.kts")
    @RegistryListResource(KrudWorldScript.class)
    @ArrayType(min = 1, type = String.class)
    private KList<String> preprocessors = new KList<>();

    @EqualsAndHashCode.Exclude
    private transient KrudWorldData loader;

    private transient String loadKey;

    private transient File loadFile;

    public abstract String getFolderName();

    public abstract String getTypeName();

    public void registerTypeAdapters(GsonBuilder builder) {

    }

    public File openInVSCode() {
        try {
            Desktop.getDesktop().open(getLoadFile());
        } catch (Throwable e) {
            KrudWorld.reportError(e);
        }

        return getLoadFile();
    }

    public abstract void scanForErrors(JSONObject p, VolmitSender sender);
}
