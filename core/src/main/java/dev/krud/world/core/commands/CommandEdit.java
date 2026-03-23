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
import dev.krud.world.core.service.StudioSVC;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.DecreeOrigin;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.format.C;

import java.awt.*;


@Decree(name = "edit", origin = DecreeOrigin.PLAYER, studio = true, description = "Edit something")
public class CommandEdit implements DecreeExecutor {

    private boolean noStudio() {
        if (!sender().isPlayer()) {
            sender().sendMessage(C.RED + "Players only!");
            return true;
        }
        if (!KrudWorld.service(StudioSVC.class).isProjectOpen()) {
            sender().sendMessage(C.RED + "No studio world is open!");
            return true;
        }
        if (!engine().isStudio()) {
            sender().sendMessage(C.RED + "You must be in a studio world!");
            return true;
        }

        if (GraphicsEnvironment.isHeadless()) {
            sender().sendMessage(C.RED + "Cannot open files in headless environments!");
            return true;
        }

        if (!Desktop.isDesktopSupported()) {
            sender().sendMessage(C.RED + "Desktop is not supported by this environment!");
            return true;
        }
        return false;
    }


    @Decree(description = "Edit the biome you specified", aliases = {"b"}, origin = DecreeOrigin.PLAYER)
    public void biome(@Param(contextual = false, description = "The biome to edit") KrudWorldBiome biome) {
        if (noStudio()) {
            return;
        }
        try {
            if (biome == null || biome.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(biome.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + biome.getTypeName() + " " + biome.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

    @Decree(description = "Edit the region you specified", aliases = {"r"}, origin = DecreeOrigin.PLAYER)
    public void region(@Param(contextual = false, description = "The region to edit") KrudWorldRegion region) {
        if (noStudio()) {
            return;
        }
        try {
            if (region == null || region.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(region.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + region.getTypeName() + " " + region.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

    @Decree(description = "Edit the dimension you specified", aliases = {"d"}, origin = DecreeOrigin.PLAYER)
    public void dimension(@Param(contextual = false, description = "The dimension to edit") KrudWorldDimension dimension) {
        if (noStudio()) {
            return;
        }
        try {
            if (dimension == null || dimension.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(dimension.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + dimension.getTypeName() + " " + dimension.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

    @Decree(description = "Edit the cave file you specified", aliases = {"c"}, origin = DecreeOrigin.PLAYER)
    public void cave(@Param(contextual = false, description = "The cave to edit") KrudWorldCave cave) {
        if (noStudio()) {
            return;
        }
        try {
            if (cave == null || cave.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(cave.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + cave.getTypeName() + " " + cave.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

    @Decree(description = "Edit the structure file you specified", aliases = {"jigsawstructure", "structure"}, origin = DecreeOrigin.PLAYER)
    public void jigsaw(@Param(contextual = false, description = "The jigsaw structure to edit") KrudWorldJigsawStructure jigsaw) {
        if (noStudio()) {
            return;
        }
        try {
            if (jigsaw == null || jigsaw.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(jigsaw.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + jigsaw.getTypeName() + " " + jigsaw.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

    @Decree(description = "Edit the pool file you specified", aliases = {"jigsawpool", "pool"}, origin = DecreeOrigin.PLAYER)
    public void jigsawPool(@Param(contextual = false, description = "The jigsaw pool to edit") KrudWorldJigsawPool pool) {
        if (noStudio()) {
            return;
        }
        try {
            if (pool == null || pool.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(pool.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + pool.getTypeName() + " " + pool.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

    @Decree(description = "Edit the jigsaw piece file you specified", aliases = {"jigsawpiece", "piece"}, origin = DecreeOrigin.PLAYER)
    public void jigsawPiece(@Param(contextual = false, description = "The jigsaw piece to edit") KrudWorldJigsawPiece piece) {
        if (noStudio()) {
            return;
        }
        try {
            if (piece == null || piece.getLoadFile() == null) {
                sender().sendMessage(C.GOLD + "Cannot find the file; Perhaps it was not loaded directly from a file?");
                return;
            }
            Desktop.getDesktop().open(piece.getLoadFile());
            sender().sendMessage(C.GREEN + "Opening " + piece.getTypeName() + " " + piece.getLoadFile().getName().split("\\Q.\\E")[0] + " in VSCode! ");
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.RED + "Cant find the file. Or registrant does not exist");
        }
    }

}
