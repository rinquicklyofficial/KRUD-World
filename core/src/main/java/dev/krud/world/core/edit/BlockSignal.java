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

package dev.krud.world.core.edit;

import dev.krud.world.util.parallel.MultiBurst;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.SR;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("InstantiationOfUtilityClass")
public class BlockSignal {
    public static final AtomicInteger active = new AtomicInteger(0);

    public BlockSignal(Block block, int ticks) {
        active.incrementAndGet();
        Location tg = block.getLocation().clone().add(0.5, 0, 0.5);
        FallingBlock e = block.getWorld().spawnFallingBlock(tg, block.getBlockData());
        e.setGravity(false);
        e.setInvulnerable(true);
        e.setGlowing(true);
        e.setDropItem(false);
        e.setHurtEntities(false);
        e.setSilent(true);
        e.setTicksLived(1);
        e.setVelocity(new Vector(0, 0, 0));
        J.s(() -> {
            e.remove();
            active.decrementAndGet();
            BlockData type = block.getBlockData();
            MultiBurst.burst.lazy(() -> {
                for (Player i : block.getWorld().getPlayers()) {
                    i.sendBlockChange(block.getLocation(), block.getBlockData());
                }
            });
        }, ticks);
    }

    public static void of(Block block, int ticks) {
        new BlockSignal(block, ticks);
    }

    public static void of(Block block) {
        of(block, 100);
    }

    public static Runnable forever(Block block) {
        Location tg = block.getLocation().clone().add(0.5, 0, 0.5).clone();
        FallingBlock e = block.getWorld().spawnFallingBlock(tg.clone(), block.getBlockData());
        e.setGravity(false);
        e.setInvulnerable(true);
        e.setGlowing(true);
        e.teleport(tg.clone());
        e.setDropItem(false);
        e.setHurtEntities(false);
        e.setSilent(true);
        e.setTicksLived(1);
        e.setVelocity(new Vector(0, 0, 0));

        new SR(20) {
            @Override
            public void run() {
                if (e.isDead()) {
                    cancel();
                    return;
                }

                e.setTicksLived(1);
                e.teleport(tg.clone());
                e.setVelocity(new Vector(0, 0, 0));
            }
        };

        return () -> {
            e.remove();
            BlockData type = block.getBlockData();

            MultiBurst.burst.lazy(() -> {
                for (Player i : block.getWorld().getPlayers()) {
                    i.sendBlockChange(block.getLocation(), block.getBlockData());
                }
            });
        };
    }
}
