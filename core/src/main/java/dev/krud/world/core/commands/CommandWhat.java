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
import dev.krud.world.core.edit.BlockSignal;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.data.B;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.DecreeOrigin;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.format.C;
import dev.krud.world.util.matter.MatterMarker;
import dev.krud.world.util.scheduling.J;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Decree(name = "what", origin = DecreeOrigin.PLAYER, studio = true, description = "KrudWorld What?")
public class CommandWhat implements DecreeExecutor {
    @Decree(description = "What is in my hand?", origin = DecreeOrigin.PLAYER)
    public void hand() {
        try {
            BlockData bd = player().getInventory().getItemInMainHand().getType().createBlockData();
            if (!bd.getMaterial().equals(Material.AIR)) {
                sender().sendMessage("Material: " + C.GREEN + bd.getMaterial().name());
                sender().sendMessage("Full: " + C.WHITE + bd.getAsString(true));
            } else {
                sender().sendMessage("Please hold a block/item");
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            Material bd = player().getInventory().getItemInMainHand().getType();
            if (!bd.equals(Material.AIR)) {
                sender().sendMessage("Material: " + C.GREEN + bd.name());
            } else {
                sender().sendMessage("Please hold a block/item");
            }
        }
    }

    @Decree(description = "What biome am i in?", origin = DecreeOrigin.PLAYER)
    public void biome() {
        try {
            KrudWorldBiome b = engine().getBiome(player().getLocation().getBlockX(), player().getLocation().getBlockY() - player().getWorld().getMinHeight(), player().getLocation().getBlockZ());
            sender().sendMessage("IBiome: " + b.getLoadKey() + " (" + b.getDerivative().name() + ")");

        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage("Non-KrudWorld Biome: " + player().getLocation().getBlock().getBiome().name());

            if (player().getLocation().getBlock().getBiome().equals(Biome.CUSTOM)) {
                try {
                    sender().sendMessage("Data Pack Biome: " + INMS.get().getTrueBiomeBaseKey(player().getLocation()) + " (ID: " + INMS.get().getTrueBiomeBaseId(INMS.get().getTrueBiomeBase(player().getLocation())) + ")");
                } catch (Throwable ee) {
                    KrudWorld.reportError(ee);
                }
            }
        }
    }

    @Decree(description = "What region am i in?", origin = DecreeOrigin.PLAYER)
    public void region() {
        try {
            Chunk chunk = world().getChunkAt(player().getLocation().getBlockZ() / 16, player().getLocation().getBlockZ() /  16);
            KrudWorldRegion r = engine().getRegion(chunk);
            sender().sendMessage("IRegion: " + r.getLoadKey() + " (" + r.getName() + ")");

        } catch (Throwable e) {
            KrudWorld.reportError(e);
            sender().sendMessage(C.IRIS + "KrudWorld worlds only.");
        }
    }

    @Decree(description = "What block am i looking at?", origin = DecreeOrigin.PLAYER)
    public void block() {
        BlockData bd;
        try {
            bd = player().getTargetBlockExact(128, FluidCollisionMode.NEVER).getBlockData();
        } catch (NullPointerException e) {
            KrudWorld.reportError(e);
            sender().sendMessage("Please look at any block, not at the sky");
            bd = null;
        }

        if (bd != null) {
            sender().sendMessage("Material: " + C.GREEN + bd.getMaterial().name());
            sender().sendMessage("Full: " + C.WHITE + bd.getAsString(true));

            if (B.isStorage(bd)) {
                sender().sendMessage(C.YELLOW + "* Storage Block (Loot Capable)");
            }

            if (B.isLit(bd)) {
                sender().sendMessage(C.YELLOW + "* Lit Block (Light Capable)");
            }

            if (B.isFoliage(bd)) {
                sender().sendMessage(C.YELLOW + "* Foliage Block");
            }

            if (B.isDecorant(bd)) {
                sender().sendMessage(C.YELLOW + "* Decorant Block");
            }

            if (B.isFluid(bd)) {
                sender().sendMessage(C.YELLOW + "* Fluid Block");
            }

            if (B.isFoliagePlantable(bd)) {
                sender().sendMessage(C.YELLOW + "* Plantable Foliage Block");
            }

            if (B.isSolid(bd)) {
                sender().sendMessage(C.YELLOW + "* Solid Block");
            }
        }
    }

    @Decree(description = "Show markers in chunk", origin = DecreeOrigin.PLAYER)
    public void markers(@Param(description = "Marker name such as cave_floor or cave_ceiling") String marker) {
        Chunk c = player().getLocation().getChunk();

        if (KrudWorldToolbelt.isKrudWorldWorld(c.getWorld())) {
            int m = 1;
            AtomicInteger v = new AtomicInteger(0);

            for (int xxx = c.getX() - 4; xxx <= c.getX() + 4; xxx++) {
                for (int zzz = c.getZ() - 4; zzz <= c.getZ() + 4; zzz++) {
                    KrudWorldToolbelt.access(c.getWorld()).getEngine().getMantle().findMarkers(xxx, zzz, new MatterMarker(marker))
                            .convert((i) -> i.toLocation(c.getWorld())).forEach((i) -> {
                                J.s(() -> BlockSignal.of(i.getBlock(), 100));
                                v.incrementAndGet();
                            });
                }
            }

            sender().sendMessage("Found " + v.get() + " Nearby Markers (" + marker + ")");
        } else {
            sender().sendMessage(C.IRIS + "KrudWorld worlds only.");
        }
    }
}
