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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.data.cache.AtomicCache;
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
import org.bukkit.util.BlockVector;

import java.io.IOException;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Desc("Represents a structure tile")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldJigsawPiece extends KrudWorldRegistrant {
    @RegistryListResource(KrudWorldObject.class)
    @Required
    @Desc("The object this piece represents")
    private String object = "";

    @ArrayType(type = KrudWorldJigsawPieceConnector.class)
    @Desc("The connectors this object contains")
    private KList<KrudWorldJigsawPieceConnector> connectors = new KList<>();

    @Desc("Configure everything about the object placement. Please don't define this unless you actually need it as using this option will slow down the jigsaw deign stage. Use this where you need it, just avoid using it everywhere to keep things fast.")
    private KrudWorldObjectPlacement placementOptions = new KrudWorldObjectPlacement().setMode(ObjectPlaceMode.FAST_MAX_HEIGHT);

    private transient AtomicCache<Integer> max2dDim = new AtomicCache<>();
    private transient AtomicCache<Integer> max3dDim = new AtomicCache<>();

    public int getMax2dDimension() {
        return max2dDim.aquire(() -> {
            try {
                BlockVector v = KrudWorldObject.sampleSize(getLoader().getObjectLoader().findFile(getObject()));
                return Math.max(v.getBlockX(), v.getBlockZ());
            } catch (IOException e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }

            return 0;
        });
    }

    public int getMax3dDimension() {
        return max3dDim.aquire(() -> {
            try {
                BlockVector v = KrudWorldObject.sampleSize(getLoader().getObjectLoader().findFile(getObject()));
                return Math.max(Math.max(v.getBlockX(), v.getBlockZ()), v.getBlockY());
            } catch (IOException e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }

            return -1;
        });
    }


    public KrudWorldJigsawPieceConnector getConnector(KrudWorldPosition relativePosition) {
        for (KrudWorldJigsawPieceConnector i : connectors) {
            if (i.getPosition().equals(relativePosition)) {
                return i;
            }
        }

        return null;
    }

    public KrudWorldJigsawPiece copy() {
        var gson = getLoader().getGson();
        KrudWorldJigsawPiece copy = gson.fromJson(gson.toJson(this), KrudWorldJigsawPiece.class);
        copy.setLoader(getLoader());
        copy.setLoadKey(getLoadKey());
        copy.setLoadFile(getLoadFile());
        return copy;
    }

    public boolean isTerminal() {
        return connectors.size() == 1;
    }

    public ObjectPlaceMode getPlaceMode() {
        return getPlacementOptions().getMode();
    }

    @Override
    public String getFolderName() {
        return "jigsaw-pieces";
    }

    @Override
    public String getTypeName() {
        return "Jigsaw Piece";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
