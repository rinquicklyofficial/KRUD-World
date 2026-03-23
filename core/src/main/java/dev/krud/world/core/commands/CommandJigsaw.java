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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.edit.JigsawEditor;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.placer.WorldObjectPlacer;
import dev.krud.world.engine.jigsaw.PlannedStructure;
import dev.krud.world.engine.object.KrudWorldJigsawPiece;
import dev.krud.world.engine.object.KrudWorldJigsawStructure;
import dev.krud.world.engine.object.KrudWorldObject;
import dev.krud.world.engine.object.KrudWorldPosition;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.DecreeOrigin;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.decree.specialhandlers.ObjectHandler;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.PrecisionStopwatch;

import java.io.File;

@Decree(name = "jigsaw", origin = DecreeOrigin.PLAYER, studio = true, description = "KrudWorld jigsaw commands")
public class CommandJigsaw implements DecreeExecutor {
    @Decree(description = "Edit a jigsaw piece")
    public void edit(
            @Param(description = "The jigsaw piece to edit")
            KrudWorldJigsawPiece piece
    ) {
        File dest = piece.getLoadFile();
        new JigsawEditor(player(), piece, KrudWorldData.loadAnyObject(piece.getObject(), data()), dest);
    }

    @Decree(description = "Place a jigsaw structure")
    public void place(
            @Param(description = "The jigsaw structure to place")
            KrudWorldJigsawStructure structure
    ) {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        try {
            var world = world();
            WorldObjectPlacer placer = new WorldObjectPlacer(world);
            PlannedStructure ps = new PlannedStructure(structure, new KrudWorldPosition(player().getLocation().add(0, world.getMinHeight(), 0)), new RNG(), true);
            VolmitSender sender = sender();
            sender.sendMessage(C.GREEN + "Generated " + ps.getPieces().size() + " pieces in " + Form.duration(p.getMilliseconds(), 2));
            ps.place(placer, failed -> sender.sendMessage(failed ? C.GREEN + "Placed the structure!" : C.RED + "Failed to place the structure!"));
        } catch (IllegalArgumentException e) {
            sender().sendMessage(C.RED + "Failed to place the structure: " + e.getMessage());
        }
    }

    @Decree(description = "Create a jigsaw piece")
    public void create(
            @Param(description = "The name of the jigsaw piece")
            String piece,
            @Param(description = "The project to add the jigsaw piece to")
            String project,
            @Param(description = "The object to use for this piece", customHandler = ObjectHandler.class)
            String object
    ) {
        KrudWorldObject o = KrudWorldData.loadAnyObject(object, data());

        if (object == null) {
            sender().sendMessage(C.RED + "Failed to find existing object");
            return;
        }

        File dest = KrudWorld.instance.getDataFile("packs", project, "jigsaw-pieces", piece + ".json");
        new JigsawEditor(player(), null, o, dest);
        sender().sendMessage(C.GRAY + "* Right Click blocks to make them connectors");
        sender().sendMessage(C.GRAY + "* Right Click connectors to orient them");
        sender().sendMessage(C.GRAY + "* Shift + Right Click connectors to remove them");
        sender().sendMessage(C.GREEN + "Remember to use /iris jigsaw save");
    }

    @Decree(description = "Exit the current jigsaw editor")
    public void exit() {
        JigsawEditor editor = JigsawEditor.editors.get(player());

        if (editor == null) {
            sender().sendMessage(C.GOLD + "You don't have any pieces open to exit!");
            return;
        }

        editor.exit();
        sender().sendMessage(C.GREEN + "Exited Jigsaw Editor");
    }

    @Decree(description = "Save & Exit the current jigsaw editor")
    public void save() {
        JigsawEditor editor = JigsawEditor.editors.get(player());

        if (editor == null) {
            sender().sendMessage(C.GOLD + "You don't have any pieces open to save!");
            return;
        }

        editor.close();
        sender().sendMessage(C.GREEN + "Saved & Exited Jigsaw Editor");
    }
}
