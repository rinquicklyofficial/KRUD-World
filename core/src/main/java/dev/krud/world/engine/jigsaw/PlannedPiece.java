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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.math.AxisAlignedBB;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlannedPiece {
    private KrudWorldPosition position;
    private KrudWorldObject object;
    private KrudWorldObject ogObject;
    private KrudWorldJigsawPiece piece;
    private KrudWorldObjectRotation rotation;
    @EqualsAndHashCode.Exclude
    private KrudWorldData data;
    private KList<KrudWorldJigsawPieceConnector> connected;
    private boolean dead = false;
    private AxisAlignedBB box;
    @EqualsAndHashCode.Exclude
    private PlannedStructure structure;
    @EqualsAndHashCode.Exclude
    @Setter(AccessLevel.NONE)
    private ParentConnection parent = null;
    @EqualsAndHashCode.Exclude
    @Setter(AccessLevel.NONE)
    private KMap<KrudWorldJigsawPieceConnector, KrudWorldPosition> realPositions;

    public PlannedPiece(PlannedStructure structure, KrudWorldPosition position, KrudWorldJigsawPiece piece) {
        this(structure, position, piece, 0, 0, 0);
    }

    public PlannedPiece(PlannedStructure structure, KrudWorldPosition position, KrudWorldJigsawPiece piece, int rx, int ry, int rz) {
        this(structure, position, piece, KrudWorldObjectRotation.of(rx * 90D, ry * 90D, rz * 90D));
    }

    public PlannedPiece(PlannedStructure structure, KrudWorldPosition position, KrudWorldJigsawPiece piece, KrudWorldObjectRotation rot) {
        this.structure = structure;
        this.position = position;
        this.data = piece.getLoader();
        this.setRotation(rot);
        this.ogObject = data.getObjectLoader().load(piece.getObject());
        this.object = structure.rotated(piece, rotation);
        this.piece = rotation.rotateCopy(piece, new KrudWorldPosition(object.getShrinkOffset()));
        this.piece.setLoadKey(piece.getLoadKey());
        this.object.setLoadKey(piece.getObject());
        this.ogObject.setLoadKey(piece.getObject());
        this.connected = new KList<>();
        this.realPositions = new KMap<>();

    }

    public void setPosition(KrudWorldPosition p) {
        this.position = p;
        box = null;
    }

    public String toString() {
        return piece.getLoadKey() + "@(" + position.getX() + "," + position.getY() + "," + position.getZ() + ")[rot:" + rotation.toString() + "]";
    }

    public AxisAlignedBB getBox() {
        if (box != null) {
            return box;
        }

        BlockVector v = getObject().getCenter();
        KrudWorldPosition pos = new KrudWorldPosition();
        KrudWorldObjectPlacement options = piece.getPlacementOptions();
        if (options != null && options.getTranslate() != null) {
            KrudWorldObjectTranslate translate = options.getTranslate();
            pos.setX(translate.getX());
            pos.setY(translate.getY());
            pos.setZ(translate.getZ());
        }
        box = object.getAABB().shifted(position.add(new KrudWorldPosition(object.getCenter())).add(pos));
        return box;
    }

    public boolean contains(KrudWorldPosition p) {
        return getBox().contains(p);
    }

    public boolean collidesWith(PlannedPiece p) {
        return getBox().intersects(p.getBox());
    }

    public KList<KrudWorldJigsawPieceConnector> getAvailableConnectors() {
        if (connected.isEmpty()) {
            return piece.getConnectors().copy();
        }

        if (connected.size() == piece.getConnectors().size()) {
            return new KList<>();
        }

        KList<KrudWorldJigsawPieceConnector> c = new KList<>();

        for (KrudWorldJigsawPieceConnector i : piece.getConnectors()) {
            if (!connected.contains(i)) {
                c.add(i);
            }
        }

        return c;
    }

    public KList<KrudWorldJigsawPieceConnector> getChildConnectors() {
        ParentConnection pc = getParent();
        KList<KrudWorldJigsawPieceConnector> c = getConnected().copy();
        if (pc != null) c.removeIf(i -> i.equals(pc.connector));
        return c;
    }

    public boolean connect(KrudWorldJigsawPieceConnector c, PlannedPiece p, KrudWorldJigsawPieceConnector pc) {
        if (piece.getConnectors().contains(c) && p.getPiece().getConnectors().contains(pc)) {
            if (connected.contains(c) || p.connected.contains(pc)) return false;
            connected.add(c);
            p.connected.add(pc);
            p.parent = new ParentConnection(this, c, p, pc);
            return true;
        }
        return false;
    }

    public KrudWorldPosition getWorldPosition(KrudWorldJigsawPieceConnector c) {
        return getWorldPosition(c.getPosition());
    }

    public List<KrudWorldPosition> getConnectorWorldPositions() {
        List<KrudWorldPosition> worldPositions = new ArrayList<>();

        for (KrudWorldJigsawPieceConnector connector : this.piece.getConnectors()) {
            KrudWorldPosition worldPosition = getWorldPosition(connector.getPosition());
            worldPositions.add(worldPosition);
        }

        return worldPositions;
    }

    public KrudWorldPosition getWorldPosition(KrudWorldPosition position) {
        return this.position.add(position).add(new KrudWorldPosition(object.getCenter()));
    }

    public void debugPrintConnectorPositions() {
        KrudWorld.debug("Connector World Positions for PlannedPiece at " + position + ":");
        List<KrudWorldPosition> connectorPositions = getConnectorWorldPositions();
        for (KrudWorldPosition pos : connectorPositions) {
            KrudWorld.debug(" - Connector at: " + pos);
        }
    }

    public boolean isFull() {
        return connected.size() >= piece.getConnectors().size();
    }

    public void setRealPositions(int x, int y, int z, IObjectPlacer placer) {
        boolean isUnderwater = piece.getPlacementOptions().isUnderwater();
        for (KrudWorldJigsawPieceConnector c : piece.getConnectors()) {
            var pos = c.getPosition().add(new KrudWorldPosition(x, 0, z));
            if (y < 0) {
                pos.setY(pos.getY() + placer.getHighest(pos.getX(), pos.getZ(), getData(), isUnderwater) + (object.getH() / 2));
            } else {
                pos.setY(pos.getY() + y);
            }
            realPositions.put(c, pos);
        }
    }

    public record ParentConnection(PlannedPiece parent, KrudWorldJigsawPieceConnector parentConnector, PlannedPiece self, KrudWorldJigsawPieceConnector connector) {
        public KrudWorldPosition getTargetPosition() {
            var pos = parent.realPositions.get(parentConnector);
            if (pos == null) return null;
            return pos.add(new KrudWorldPosition(parentConnector.getDirection().toVector()))
                    .sub(connector.getPosition())
                    .sub(new KrudWorldPosition(self.object.getCenter()));
        }
    }
}
