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

package dev.krud.world.engine.jigsaw;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.placer.WorldObjectPlacer;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.math.Position2;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.slices.container.JigsawPieceContainer;
import dev.krud.world.util.matter.slices.container.JigsawStructureContainer;
import dev.krud.world.util.matter.slices.container.JigsawStructuresContainer;
import dev.krud.world.util.scheduling.J;
import lombok.Data;
import org.bukkit.Axis;

import java.util.function.Consumer;

@Data
public class PlannedStructure {
    private static ConcurrentLinkedHashMap<String, KrudWorldObject> objectRotationCache
            = new ConcurrentLinkedHashMap.Builder<String, KrudWorldObject>()
            .initialCapacity(64)
            .maximumWeightedCapacity(1024)
            .concurrencyLevel(32)
            .build();
    private KList<PlannedPiece> pieces;
    private KrudWorldJigsawStructure structure;
    private KrudWorldPosition position;
    private KrudWorldData data;
    private RNG rng;
    private boolean forcePlace;
    private boolean verbose;
    private boolean terminating;

    public PlannedStructure(KrudWorldJigsawStructure structure, KrudWorldPosition position, RNG rng, boolean forcePlace) {
        terminating = false;
        verbose = true;
        this.pieces = new KList<>();
        this.structure = structure;
        this.position = position;
        this.rng = rng;
        this.forcePlace = forcePlace || structure.isForcePlace();
        this.data = structure.getLoader();
        generateStartPiece();

        for (int i = 0; i < structure.getMaxDepth(); i++) {
            generateOutwards();
        }

        generateTerminators();

        KrudWorld.debug("JPlace: ROOT @ relative " + position.toString());

        for (PlannedPiece i : pieces) {
            KrudWorld.debug("Place: " + i.getObject().getLoadKey() + " at @ relative " + i.getPosition().toString());
        }
    }

    public boolean place(IObjectPlacer placer, Mantle e, Engine eng) {
        KrudWorldObjectPlacement options = new KrudWorldObjectPlacement();
        options.setRotation(KrudWorldObjectRotation.of(0,0,0));
        int startHeight = pieces.get(0).getPosition().getY();

        boolean placed = false;
        for (PlannedPiece i : pieces) {
            if (place(i, startHeight, options, placer, e, eng))
                placed = true;
        }
        if (placed) {
            Position2 chunkPos = new Position2(position.getX() >> 4, position.getZ() >> 4);
            Position2 regionPos = new Position2(chunkPos.getX() >> 5, chunkPos.getZ() >> 5);
            JigsawStructuresContainer slice = e.get(regionPos.getX(), 0, regionPos.getZ(), JigsawStructuresContainer.class);
            if (slice == null) slice = new JigsawStructuresContainer();
            slice.add(structure, chunkPos);
            e.set(regionPos.getX(), 0, regionPos.getZ(), slice);
        }
        return placed;
    }

    public boolean place(PlannedPiece i, int startHeight, KrudWorldObjectPlacement o, IObjectPlacer placer, Mantle e, Engine eng) {
        KrudWorldObjectPlacement options = o;

        if (i.getPiece().getPlacementOptions() != null) {
            options = i.getPiece().getPlacementOptions();
            options.getRotation().setEnabled(false);
            options.setRotateTowardsSlope(false);
            options.setWarp(new KrudWorldGeneratorStyle(NoiseStyle.FLAT));
        } else {
            options.setMode(i.getPiece().getPlaceMode());
        }
        if (forcePlace) {
            options.setForcePlace(true);
        }

        KrudWorldObject v = i.getObject();
        int sx = (v.getW() / 2);
        int sz = (v.getD() / 2);
        int xx = i.getPosition().getX() + sx;
        int zz = i.getPosition().getZ() + sz;
        int offset = i.getPosition().getY() - startHeight;
        int height;

        if (i.getStructure().getStructure().getLockY() == -1) {
            if (i.getStructure().getStructure().getOverrideYRange() != null) {
                height = (int) i.getStructure().getStructure().getOverrideYRange().get(rng, xx, zz, getData());
            } else {
                height = placer.getHighest(xx, zz, getData(), options.isUnderwater());
            }
        } else {
            height = i.getStructure().getStructure().getLockY();
        }

        PlannedPiece.ParentConnection connection = i.getParent();
        if (connection != null && connection.connector().isLockY()) {
            var pos = connection.getTargetPosition();
            if (pos != null) {
                height = pos.getY();
                offset = 0;
            } else {
                KrudWorld.warn("Failed to get target position for " + v.getLoadKey());
            }
        }

        height += offset + (v.getH() / 2);

        if (options.getMode().equals(ObjectPlaceMode.PAINT)) {
            height = -1;
        }

        int id = rng.i(0, Integer.MAX_VALUE);
        JigsawPieceContainer piece = JigsawPieceContainer.toContainer(i.getPiece());
        JigsawStructureContainer structure = JigsawStructureContainer.toContainer(getStructure());
        i.setRealPositions(xx, height, zz, placer);
        return v.place(xx, height, zz, placer, options, rng, (b, data) -> {
            placer.setData(b.getX(), b.getY(), b.getZ(), v.getLoadKey() + "@" + id);
            placer.setData(b.getX(), b.getY(), b.getZ(), structure);
            placer.setData(b.getX(), b.getY(), b.getZ(), piece);
        }, null, getData().getEngine() != null ? getData() : eng.getData()) != -1;
    }

    public void place(WorldObjectPlacer placer, Consumer<Boolean> consumer) {
        J.s(() -> consumer.accept(place(placer, placer.getMantle().getMantle(), placer.getEngine())));
    }

    private void generateOutwards() {
        for (PlannedPiece i : getPiecesWithAvailableConnectors().shuffle(rng)) {
            if (!generatePieceOutwards(i)) {
                i.setDead(true);
            }
        }
    }

    private boolean generatePieceOutwards(PlannedPiece piece) {
        boolean b = false;

        for (KrudWorldJigsawPieceConnector i : piece.getAvailableConnectors().shuffleCopy(rng)) {
            if (generateConnectorOutwards(piece, i)) {
                b = true;
                piece.debugPrintConnectorPositions();
            }
        }

        return b;
    }

    private boolean generateConnectorOutwards(PlannedPiece piece, KrudWorldJigsawPieceConnector pieceConnector) {
        for (KrudWorldJigsawPiece i : getShuffledPiecesFor(pieceConnector)) {
            if (generateRotatedPiece(piece, pieceConnector, i)) {
                return true;
            }
        }

        return false;
    }

    private boolean generateRotatedPiece(PlannedPiece piece, KrudWorldJigsawPieceConnector pieceConnector, KrudWorldJigsawPiece idea) {
        if (!piece.getPiece().getPlacementOptions().getRotation().isEnabled()) {
            return generateRotatedPiece(piece, pieceConnector, idea, 0, 0, 0);
        }

        KList<Integer> forder1 = new KList<Integer>().qadd(0).qadd(1).qadd(2).qadd(3).shuffle(rng);
        KList<Integer> forder2 = new KList<Integer>().qadd(0).qadd(1).qadd(2).qadd(3).shuffle(rng);

        for (Integer i : forder1) {
            if (pieceConnector.isRotateConnector()) {
                assert pieceConnector.getDirection().getAxis() != null;
                if (!pieceConnector.getDirection().getAxis().equals(Axis.Y)) {
                    for (Integer j : forder2) {
                        if (pieceConnector.getDirection().getAxis().equals(Axis.X) && generateRotatedPiece(piece, pieceConnector, idea, j, i, 0)) {
                            return true;
                        }

                        if (pieceConnector.getDirection().getAxis().equals(Axis.Z) && generateRotatedPiece(piece, pieceConnector, idea, 0, i, j)) {
                            return true;
                        }
                    }
                }
            }

            if (generateRotatedPiece(piece, pieceConnector, idea, 0, i, 0)) {
                return true;
            }
        }

        return false;
    }

    private boolean generateRotatedPiece(PlannedPiece piece, KrudWorldJigsawPieceConnector pieceConnector, KrudWorldJigsawPiece idea, KrudWorldObjectRotation rotation) {
        if (!idea.getPlacementOptions().getRotation().isEnabled()) {
            rotation = piece.getRotation();
        }

        PlannedPiece test = new PlannedPiece(this, piece.getPosition(), idea, rotation);

        for (KrudWorldJigsawPieceConnector j : test.getPiece().getConnectors().shuffleCopy(rng)) {
            if (generatePositionedPiece(piece, pieceConnector, test, j)) {
                return true;
            }
        }

        return false;
    }

    private boolean generateRotatedPiece(PlannedPiece piece, KrudWorldJigsawPieceConnector pieceConnector, KrudWorldJigsawPiece idea, int x, int y, int z) {
        return generateRotatedPiece(piece, pieceConnector, idea, KrudWorldObjectRotation.of(x * 90D, y * 90D, z * 90D));
    }

    private boolean generatePositionedPiece(PlannedPiece piece, KrudWorldJigsawPieceConnector pieceConnector, PlannedPiece test, KrudWorldJigsawPieceConnector testConnector) {
        test.setPosition(new KrudWorldPosition(0, 0, 0));
        KrudWorldPosition connector = piece.getWorldPosition(pieceConnector);
        KrudWorldDirection desiredDirection = pieceConnector.getDirection().reverse();
        KrudWorldPosition desiredPosition = connector.sub(new KrudWorldPosition(desiredDirection.toVector()));

        if (!pieceConnector.getTargetName().equals("*") && !pieceConnector.getTargetName().equals(testConnector.getName())) {
            return false;
        }

        if (!testConnector.getDirection().equals(desiredDirection)) {
            return false;
        }

        KrudWorldPosition shift = test.getWorldPosition(testConnector);
        test.setPosition(desiredPosition.sub(shift));

        if (collidesWith(test, piece)) {
            return false;
        }

        piece.connect(pieceConnector, test, testConnector);
        pieces.add(test);

        return true;
    }

    private KList<KrudWorldJigsawPiece> getShuffledPiecesFor(KrudWorldJigsawPieceConnector c) {
        KList<KrudWorldJigsawPiece> p = new KList<>();

        KList<String> pools = terminating && getStructure().getTerminatePool() != null ? new KList<>(getStructure().getTerminatePool()) : c.getPools().shuffleCopy(rng);
        for (String i : pools) {
            for (String j : getData().getJigsawPoolLoader().load(i).getPieces().shuffleCopy(rng)) {
                KrudWorldJigsawPiece pi = getData().getJigsawPieceLoader().load(j);

                if (pi == null || (terminating && !pi.isTerminal())) {
                    continue;
                }

                p.addIfMissing(pi);
            }
        }
        return p.shuffle(rng);
    }

    private void generateStartPiece() {
        pieces.add(new PlannedPiece(this, position, getData().getJigsawPieceLoader().load(rng.pick(getStructure().getPieces())), 0, getStructure().isDisableInitialRotation() ? 0 : rng.nextInt(4), 0));
    }

    private void generateTerminators() {
        if (getStructure().isTerminate()) {
            terminating = true;
            generateOutwards();
        }
    }

    public KList<PlannedPiece> getPiecesWithAvailableConnectors() {
        KList<PlannedPiece> available = pieces.copy().removeWhere(PlannedPiece::isFull);
        if (!terminating) available.removeIf(PlannedPiece::isDead);
        return available;
    }

    public int getVolume() {
        int v = 0;

        for (PlannedPiece i : pieces) {
            v += i.getObject().getH() * i.getObject().getW() * i.getObject().getD();
        }

        return v;
    }

    public int getMass() {
        int v = 0;

        for (PlannedPiece i : pieces) {
            v += i.getObject().getBlocks().size();
        }

        return v;
    }

    public boolean collidesWith(PlannedPiece piece, PlannedPiece ignore) {
        for (PlannedPiece i : pieces) {
            if (i.equals(ignore)) {
                continue;
            }

            if (i.collidesWith(piece)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(KrudWorldPosition p) {
        for (PlannedPiece i : pieces) {
            if (i.contains(p)) {
                return true;
            }
        }

        return false;
    }

    public KrudWorldObject rotated(KrudWorldJigsawPiece piece, KrudWorldObjectRotation rotation) {
        String key = piece.getObject() + "-" + rotation.hashCode();

        return objectRotationCache.computeIfAbsent(key, (k) -> rotation.rotateCopy(data.getObjectLoader().load(piece.getObject())));
    }
}
