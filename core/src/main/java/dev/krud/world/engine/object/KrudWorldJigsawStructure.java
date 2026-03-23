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
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.engine.object.annotations.functions.StructureKeyFunction;
import dev.krud.world.engine.object.annotations.functions.StructureKeyOrTagFunction;
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

@Desc("Represents a jigsaw structure")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldJigsawStructure extends KrudWorldRegistrant {
    @RegistryListFunction(StructureKeyFunction.class)
    @ArrayType(min = 1, type = String.class)
    @Desc("The datapack structures. Randomply chooses a structure to place\nIgnores every other setting")
    private KList<String> datapackStructures = new KList<>();

    @RegistryListResource(KrudWorldJigsawPiece.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("The starting pieces. Randomly chooses a starting piece, then connects pieces using the pools define in the starting piece.")
    private KList<String> pieces = new KList<>();

    @MaxNumber(32)
    @MinNumber(1)
    @Desc("The maximum pieces that can step out from the center piece")
    private int maxDepth = 9;

    @Desc("Jigsaw grows the parallax layer which slows iris down a bit. Since there are so many pieces, KrudWorld takes the avg piece size and calculates the parallax radius from that. Unless your structures are using only the biggest pieces, your structure should fit in the chosen size fine. If you are seeing cut-off parts of your structures or broken terrain, turn this option on. This option will pick the biggest piece dimensions and multiply it by your (maxDepth+1) * 2 as the size to grow the parallax layer by. But typically keep this off.")
    private boolean useMaxPieceSizeForParallaxRadius = false;

    @Desc("If set to true, iris will look for any pieces with only one connector in valid pools for edge connectors and attach them to 'terminate' the paths/piece connectors. Essentially it caps off ends. For example in a village, KrudWorld would add houses to the ends of roads where possible. For terminators to be selected, they can only have one connector or they wont be chosen.")
    private boolean terminate = true;

    @RegistryListResource(KrudWorldJigsawPool.class)
    @Desc("The pool to use when terminating pieces")
    private String terminatePool = null;

    @Desc("Override the y range instead of placing on the height map")
    private KrudWorldStyledRange overrideYRange = null;

    @Desc("Force Y to a specific value")
    private int lockY = -1;

    @Desc("Set to true to prevent rotating the initial structure piece")
    private boolean disableInitialRotation = false;

    @RegistryListFunction(StructureKeyOrTagFunction.class)
    @Desc("The minecraft key to use when creating treasure maps")
    private String structureKey = null;

    @Desc("Force Place the whole structure")
    private boolean forcePlace = false;

    private transient AtomicCache<Integer> maxDimension = new AtomicCache<>();

    private void loadPool(String p, KList<String> pools, KList<String> pieces) {
        if (p.isEmpty()) {
            return;
        }

        KrudWorldJigsawPool pool = getLoader().getJigsawPoolLoader().load(p);

        if (pool == null) {
            KrudWorld.warn("Can't find jigsaw pool: " + p);
            return;
        }

        for (String i : pool.getPieces()) {
            if (pieces.addIfMissing(i)) {
                loadPiece(i, pools, pieces);
            }
        }
    }

    private void loadPiece(String p, KList<String> pools, KList<String> pieces) {
        KrudWorldJigsawPiece piece = getLoader().getJigsawPieceLoader().load(p);

        if (piece == null) {
            KrudWorld.warn("Can't find jigsaw piece: " + p);
            return;
        }

        for (KrudWorldJigsawPieceConnector i : piece.getConnectors()) {
            for (String j : i.getPools()) {
                if (pools.addIfMissing(j)) {
                    loadPool(j, pools, pieces);
                }
            }
        }
    }

    public int getMaxDimension() {
        return maxDimension.aquire(() -> {
            if (datapackStructures.isNotEmpty()) {
                return 0;
            }

            if (useMaxPieceSizeForParallaxRadius) {
                int max = 0;
                KList<String> pools = new KList<>();
                KList<String> pieces = new KList<>();

                for (String i : getPieces()) {
                    loadPiece(i, pools, pieces);
                }

                for (String i : pieces) {
                    max = Math.max(max, getLoader().getJigsawPieceLoader().load(i).getMax3dDimension());
                }

                return max * (((getMaxDepth() + 1) * 2) + 1);
            } else {
                KList<String> pools = new KList<>();
                KList<String> pieces = new KList<>();

                for (String i : getPieces()) {
                    loadPiece(i, pools, pieces);
                }

                if (pieces.isEmpty()) {
                    int max = 0;
                    for (String i : getPieces()) {
                        max = Math.max(max, getLoader().getJigsawPieceLoader().load(i).getMax2dDimension());
                    }
                    return max;
                }

                int avg = 0;

                for (String i : pieces) {
                    avg += getLoader().getJigsawPieceLoader().load(i).getMax2dDimension();
                }

                return (avg / (!pieces.isEmpty() ? pieces.size() : 1)) * (((getMaxDepth() + 1) * 2) + 1);
            }
        });
    }

    @Override
    public String getFolderName() {
        return "jigsaw-structures";
    }

    @Override
    public String getTypeName() {
        return "Jigsaw Structure";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
