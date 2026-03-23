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

package dev.krud.world.engine.object;

import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class KrudWorldScript extends KrudWorldRegistrant {
    private final String source;

    public KrudWorldScript() {
        this("");
    }

    public KrudWorldScript(String source) {
        this.source = source;
    }

    @Override
    public String getFolderName() {
        return "scripts";
    }

    @Override
    public String getTypeName() {
        return "Script";
    }

    public String toString() {
        return source;
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
