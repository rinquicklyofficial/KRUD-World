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

package dev.krud.world.core.link;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

// See/update https://app.gitbook.com/@volmitsoftware/s/iris/compatability/papi/
public class KrudWorldPapiExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "iris";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Volmit Software";
    }

    @Override
    public @NotNull String getVersion() {
        return KrudWorld.instance.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String p) {
        Location l = null;
        PlatformChunkGenerator a = null;

        if (player.isOnline() && player.getPlayer() != null) {
            l = player.getPlayer().getLocation().add(0, 2, 0);
            a = KrudWorldToolbelt.access(l.getWorld());
        }

        if (p.equalsIgnoreCase("biome_name")) {
            if (a != null) {
                return getBiome(a, l).getName();
            }
        } else if (p.equalsIgnoreCase("biome_id")) {
            if (a != null) {
                return getBiome(a, l).getLoadKey();
            }
        } else if (p.equalsIgnoreCase("biome_file")) {
            if (a != null) {
                return getBiome(a, l).getLoadFile().getPath();
            }
        } else if (p.equalsIgnoreCase("region_name")) {
            if (a != null) {
                return a.getEngine().getRegion(l).getName();
            }
        } else if (p.equalsIgnoreCase("region_id")) {
            if (a != null) {
                return a.getEngine().getRegion(l).getLoadKey();
            }
        } else if (p.equalsIgnoreCase("region_file")) {
            if (a != null) {
                return a.getEngine().getRegion(l).getLoadFile().getPath();
            }
        } else if (p.equalsIgnoreCase("terrain_slope")) {
            if (a != null) {
                return (a.getEngine())
                        .getComplex().getSlopeStream()
                        .get(l.getX(), l.getZ()) + "";
            }
        } else if (p.equalsIgnoreCase("terrain_height")) {
            if (a != null) {
                return Math.round(a.getEngine().getHeight(l.getBlockX(), l.getBlockZ())) + "";
            }
        } else if (p.equalsIgnoreCase("world_mode")) {
            if (a != null) {
                return a.isStudio() ? "Studio" : "Production";
            }
        } else if (p.equalsIgnoreCase("world_seed")) {
            if (a != null) {
                return a.getEngine().getSeedManager().getSeed() + "";
            }
        } else if (p.equalsIgnoreCase("world_speed")) {
            if (a != null) {
                return a.getEngine().getGeneratedPerSecond() + "/s";
            }
        }

        return null;
    }

    private KrudWorldBiome getBiome(PlatformChunkGenerator a, Location l) {
        return a.getEngine().getBiome(l.getBlockX(), l.getBlockY() - l.getWorld().getMinHeight(), l.getBlockZ());
    }
}
