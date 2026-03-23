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

package dev.krud.world.engine.object.matter;

import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.object.KrudWorldObject;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.matter.KrudWorldMatter;
import dev.krud.world.util.matter.Matter;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldMatterObject extends KrudWorldRegistrant {
    private final Matter matter;

    public KrudWorldMatterObject() {
        this(1, 1, 1);
    }

    public KrudWorldMatterObject(int w, int h, int d) {
        this(new KrudWorldMatter(w, h, d));
    }

    public KrudWorldMatterObject(Matter matter) {
        this.matter = matter;
    }

    public static KrudWorldMatterObject from(KrudWorldObject object) {
        return new KrudWorldMatterObject(Matter.from(object));
    }

    public static KrudWorldMatterObject from(File j) throws IOException, ClassNotFoundException {
        return new KrudWorldMatterObject(Matter.read(j));
    }

    @Override
    public String getFolderName() {
        return "matter";
    }

    @Override
    public String getTypeName() {
        return "Matter";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
