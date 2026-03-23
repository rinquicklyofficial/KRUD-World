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
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.RegistryListResource;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Desc("Represents a structure piece pool")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldJigsawPool extends KrudWorldRegistrant {
    @RegistryListResource(KrudWorldJigsawPiece.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("A list of structure piece pools")
    private KList<String> pieces = new KList<>();

    @Override
    public String getFolderName() {
        return "jigsaw-pools";
    }

    @Override
    public String getTypeName() {
        return "Jigsaw Pool";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
