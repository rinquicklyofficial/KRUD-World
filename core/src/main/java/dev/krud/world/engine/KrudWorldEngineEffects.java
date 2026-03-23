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

package dev.krud.world.engine;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedComponent;
import dev.krud.world.engine.framework.EngineEffects;
import dev.krud.world.engine.framework.EnginePlayer;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.math.M;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class KrudWorldEngineEffects extends EngineAssignedComponent implements EngineEffects {
    private final KMap<UUID, EnginePlayer> players;
    private final Semaphore limit;

    public KrudWorldEngineEffects(Engine engine) {
        super(engine, "FX");
        players = new KMap<>();
        limit = new Semaphore(1);
    }

    @Override
    public void updatePlayerMap() {
        List<Player> pr = getEngine().getWorld().getPlayers();

        if (pr == null) {
            return;
        }

        for (Player i : pr) {
            boolean pcc = players.containsKey(i.getUniqueId());
            if (!pcc) {
                players.put(i.getUniqueId(), new EnginePlayer(getEngine(), i));
            }
        }

        for (UUID i : players.k()) {
            if (!pr.contains(players.get(i).getPlayer())) {
                players.remove(i);
            }
        }
    }

    @Override
    public void tickRandomPlayer() {
        if (limit.tryAcquire()) {
            if (M.r(0.02)) {
                updatePlayerMap();
                limit.release();
                return;
            }

            if (players.isEmpty()) {
                limit.release();
                return;
            }

            double limitms = 1.5;
            int max = players.size();
            PrecisionStopwatch p = new PrecisionStopwatch();

            while (max-- > 0 && M.ms() - p.getMilliseconds() < limitms) {
                players.v().getRandom().tick();
            }

            limit.release();
        }
    }
}
