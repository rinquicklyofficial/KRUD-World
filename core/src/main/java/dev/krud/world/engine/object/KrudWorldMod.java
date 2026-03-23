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
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Desc("Represents a dimension")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldMod extends KrudWorldRegistrant {
    @MinNumber(2)
    @Required
    @Desc("The human readable name of this dimension")
    private String name = "A Dimension Mod";

    @Desc("If this mod only works with a specific dimension, define it's load key here. Such as overworld, or flat. Otherwise iris will assume this mod works with anything.")
    private String forDimension = "";

    @MinNumber(-1)
    @MaxNumber(512)
    @Desc("Override the fluid height. Otherwise set it to -1")
    private int overrideFluidHeight = -1;

    @Desc("A list of biomes to remove")
    @RegistryListResource(KrudWorldBiome.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> removeBiomes = new KList<>();

    @Desc("A list of objects to remove")
    @RegistryListResource(KrudWorldObject.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> removeObjects = new KList<>();

    @Desc("A list of regions to remove")
    @RegistryListResource(KrudWorldRegion.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> removeRegions = new KList<>();

    @Desc("A list of regions to inject")
    @RegistryListResource(KrudWorldRegion.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> injectRegions = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModBiomeInjector.class)
    @Desc("Inject biomes into existing regions")
    private KList<KrudWorldModBiomeInjector> biomeInjectors = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModBiomeReplacer.class)
    @Desc("Replace biomes with other biomes")
    private KList<KrudWorldModBiomeReplacer> biomeReplacers = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModObjectReplacer.class)
    @Desc("Replace objects with other objects")
    private KList<KrudWorldModObjectReplacer> objectReplacers = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModObjectPlacementBiomeInjector.class)
    @Desc("Inject placers into existing biomes")
    private KList<KrudWorldModObjectPlacementBiomeInjector> biomeObjectPlacementInjectors = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModObjectPlacementRegionInjector.class)
    @Desc("Inject placers into existing regions")
    private KList<KrudWorldModObjectPlacementRegionInjector> regionObjectPlacementInjectors = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModRegionReplacer.class)
    @Desc("Replace biomes with other biomes")
    private KList<KrudWorldModRegionReplacer> regionReplacers = new KList<>();

    @ArrayType(min = 1, type = KrudWorldObjectReplace.class)
    @Desc("Replace blocks with other blocks")
    private KList<KrudWorldObjectReplace> blockReplacers = new KList<>();

    @ArrayType(min = 1, type = KrudWorldModNoiseStyleReplacer.class)
    @Desc("Replace noise styles with other styles")
    private KList<KrudWorldModNoiseStyleReplacer> styleReplacers = new KList<>();

    @Override
    public String getFolderName() {
        return "mods";
    }

    @Override
    public String getTypeName() {
        return "Mod";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
