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

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.KrudWorldEngine;
import dev.krud.world.engine.object.IRare;
import dev.krud.world.engine.object.KrudWorldStyledRange;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.MatterSlice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("matter-placer")
@EqualsAndHashCode()
@Accessors(chain = true)
@NoArgsConstructor
@Desc("Represents an iris object placer. It places matter objects.")
@Data
public class KrudWorldMatterPlacement implements IRare {
    @RegistryListResource(KrudWorldMatterObject.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("List of objects to place")
    private KList<String> place = new KList<>();

    @MinNumber(0)
    @Desc("The rarity of this object placing")
    private int rarity = 0;

    @MinNumber(0)
    @Desc("The styled density of this object")
    private KrudWorldStyledRange densityRange;

    @Desc("The absolute density for this object")
    private double density = 1;

    @Desc("Translate this matter object before placement")
    private KrudWorldMatterTranslate translate;

    @Desc("Place this object on the surface height, bedrock or the sky, then use translate if need be.")
    private KrudWorldMatterPlacementLocation location = KrudWorldMatterPlacementLocation.SURFACE;

    public void place(KrudWorldEngine engine, KrudWorldData data, RNG rng, int ax, int az) {
        KrudWorldMatterObject object = data.getMatterLoader().load(place.getRandom(rng));
        int x = ax;
        int z = az;
        int yoff = 0;

        if (translate != null) {
            x += translate.xOffset(data, rng, x, z);
            yoff += translate.yOffset(data, rng, x, z);
            z += translate.zOffset(data, rng, x, z);
        }

        int y = yoff + location.at(engine, x, z);
        Mantle mantle = engine.getMantle().getMantle();

        int xx = x;
        int yy = y;
        int zz = z;

        for (MatterSlice<?> slice : object.getMatter().getSliceMap().values()) {
            slice.iterate((mx, my, mz, v) -> {
                mantle.set(xx + mx, yy + my, zz + mz, v);
            });
        }
    }
}
