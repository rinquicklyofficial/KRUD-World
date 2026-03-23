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

package dev.krud.world.engine.framework;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldEffect;
import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.math.M;
import dev.krud.world.util.scheduling.J;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Data
public class EnginePlayer {
    private final Engine engine;
    private final Player player;
    private KrudWorldBiome biome;
    private KrudWorldRegion region;
    private Location lastLocation;
    private long lastSample;

    public EnginePlayer(Engine engine, Player player) {
        this.engine = engine;
        this.player = player;
        lastLocation = player.getLocation().clone();
        lastSample = -1;
        sample();
    }

    public void tick() {
        if (sample() || !KrudWorldSettings.get().getWorld().isEffectSystem())
            return;

        J.a(() -> {
            if (region != null) {
                for (KrudWorldEffect j : region.getEffects()) {
                    try {
                        j.apply(player, getEngine());
                    } catch (Throwable e) {
                        KrudWorld.reportError(e);

                    }
                }
            }

            if (biome != null) {
                for (KrudWorldEffect j : biome.getEffects()) {
                    try {
                        j.apply(player, getEngine());
                    } catch (Throwable e) {
                        KrudWorld.reportError(e);

                    }
                }
            }
        });
    }

    public long ticksSinceLastSample() {
        return M.ms() - lastSample;
    }

    public boolean sample() {
        Location current = player.getLocation().clone();
        if (current.getWorld() != engine.getWorld().realWorld())
            return true;
        try {
            if (ticksSinceLastSample() > 55 && current.distanceSquared(lastLocation) > 9 * 9) {
                lastLocation = current;
                lastSample = M.ms();
                biome = engine.getBiome(current);
                region = engine.getRegion(current);
            }
            return false;
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }
        return true;
    }
}
