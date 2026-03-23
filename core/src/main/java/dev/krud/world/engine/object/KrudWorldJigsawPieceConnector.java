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

import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Snippet("connector")
@Desc("Represents a structure tile")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldJigsawPieceConnector {
    @Required
    @Desc("The name of this connector, such as entry, or table node. This is a name for organization. Other connectors can specifically use targetName to target a specific connector type. Multiple connectors can use the same name.")
    private String name = "";

    @Required
    @Desc("Target a piece's connector with the specified name. For any piece's connector, define * or don't define it.")
    private String targetName = "*";

    @Desc("Rotates the placed piece on this connector. If rotation is enabled, this connector will effectivley rotate, if this connector is facing the Z direction, then the connected piece would rotate in the X,Y direction in 90 degree segments.")
    private boolean rotateConnector = false;

    @Desc("If set to true, this connector is allowed to place pieces inside of it's own piece. For example if you are adding a light post, or house on top of a path piece, you would set this to true to allow the piece to collide with the path bounding box.")
    private boolean innerConnector = false;

    @RegistryListResource(KrudWorldJigsawPool.class)
    @Desc("Pick piece pools to place onto this connector")
    @ArrayType(type = String.class, min = 1)
    @Required
    private KList<String> pools = new KList<>();

    @RegistryListResource(KrudWorldEntity.class)
    @Desc("Pick an entity to spawn on this connector")
    private String spawnEntity;

    @Desc("Stop the entity from despawning")
    private boolean keepEntity;

    @MaxNumber(50)
    @MinNumber(1)
    @Desc("The amount of entities to spawn (must be a whole number)")
    private int entityCount = 1;

    @Desc("The relative position this connector is located at for connecting to other pieces")
    @Required
    private KrudWorldPosition position = new KrudWorldPosition(0, 0, 0);

    @Desc("The relative position to this connector to place entities at")
    @DependsOn({"spawnEntity"})
    private KrudWorldPosition entityPosition = null;

    @Desc("The direction this connector is facing. If the direction is set to UP, then pieces will place ABOVE the connector.")
    @Required
    private KrudWorldDirection direction = KrudWorldDirection.UP_POSITIVE_Y;

    @Desc("Lock the Y position of this connector")
    private boolean lockY = false;

    public String toString() {
        return direction.getFace().name() + "@(" + position.getX() + "," + position.getY() + "," + position.getZ() + ")";
    }

    public KrudWorldJigsawPieceConnector copy() {
        KrudWorldJigsawPieceConnector c = new KrudWorldJigsawPieceConnector();
        c.setInnerConnector(isInnerConnector());
        c.setTargetName(getTargetName());
        c.setPosition(getPosition().copy());
        c.setDirection(getDirection());
        c.setRotateConnector(isRotateConnector());
        c.setName(getName());
        c.setSpawnEntity(getSpawnEntity());
        c.setPools(getPools().copy());
        return c;
    }
}
