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

package dev.krud.world.core.commands;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldJigsawStructure;
import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.DecreeOrigin;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.decree.specialhandlers.ObjectHandler;
import dev.krud.world.util.format.C;

@Decree(name = "find", origin = DecreeOrigin.PLAYER, description = "KrudWorld Find commands", aliases = "goto")
public class CommandFind implements DecreeExecutor {
    @Decree(description = "Find a biome")
    public void biome(
            @Param(description = "The biome to look for")
            KrudWorldBiome biome,
            @Param(description = "Should you be teleported", defaultValue = "true")
            boolean teleport
    ) {
        Engine e = engine();

        if (e == null) {
            sender().sendMessage(C.GOLD + "Not in an KrudWorld World!");
            return;
        }

        e.gotoBiome(biome, player(), teleport);
    }

    @Decree(description = "Find a region")
    public void region(
            @Param(description = "The region to look for")
            KrudWorldRegion region,
            @Param(description = "Should you be teleported", defaultValue = "true")
            boolean teleport
    ) {
        Engine e = engine();

        if (e == null) {
            sender().sendMessage(C.GOLD + "Not in an KrudWorld World!");
            return;
        }

        e.gotoRegion(region, player(), teleport);
    }

    @Decree(description = "Find a structure")
    public void structure(
            @Param(description = "The structure to look for")
            KrudWorldJigsawStructure structure,
            @Param(description = "Should you be teleported", defaultValue = "true")
            boolean teleport
    ) {
        Engine e = engine();

        if (e == null) {
            sender().sendMessage(C.GOLD + "Not in an KrudWorld World!");
            return;
        }

        e.gotoJigsaw(structure, player(), teleport);
    }

    @Decree(description = "Find a point of interest.")
    public void poi(
            @Param(description = "The type of PoI to look for.")
            String type,
            @Param(description = "Should you be teleported", defaultValue = "true")
            boolean teleport
    ) {
        Engine e = engine();
        if (e == null) {
            sender().sendMessage(C.GOLD + "Not in an KrudWorld World!");
            return;
        }

        e.gotoPOI(type, player(), teleport);
    }

    @Decree(description = "Find an object")
    public void object(
            @Param(description = "The object to look for", customHandler = ObjectHandler.class)
            String object,
            @Param(description = "Should you be teleported", defaultValue = "true")
            boolean teleport
    ) {
        Engine e = engine();

        if (e == null) {
            sender().sendMessage(C.GOLD + "Not in an KrudWorld World!");
            return;
        }

        e.gotoObject(object, player(), teleport);
    }
}
