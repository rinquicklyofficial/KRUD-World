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
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Desc("Represents a marker")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldMarker extends KrudWorldRegistrant {
    @Desc("A list of spawners to add to anywhere this marker is.")
    @RegistryListResource(KrudWorldSpawner.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> spawners = new KList<>();

    @Desc("Remove this marker when the block it's assigned to is changed.")
    private boolean removeOnChange = true;

    @Desc("If true, markers will only be placed here if there is 2 air blocks above it.")
    private boolean emptyAbove = true;

    @Desc("If this marker is used, what is the chance it removes itself. For example 25% (0.25) would mean that on average 4 uses will remove a specific marker. Set this below 0 (-1) to never exhaust & set this to 1 or higher to always exhaust on first use.")
    private double exhaustionChance = 0;

    public boolean shouldExhaust() {
        return exhaustionChance > RNG.r.nextDouble();
    }

    @Override
    public String getFolderName() {
        return "markers";
    }

    @Override
    public String getTypeName() {
        return "Marker";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
