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

package dev.krud.world.core.service;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.scheduling.J;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

public class ObjectSVC implements KrudWorldService {

    @Getter
    private final Deque<Map<Block, BlockData>> undos = new ArrayDeque<>();


    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void addChanges(Map<Block, BlockData> oldBlocks) {
        undos.add(oldBlocks);
    }

    public void revertChanges(int amount) {
        loopChange(amount);
    }

    private void loopChange(int amount) {
        if (undos.size() > 0) {
            revert(undos.pollLast());
            if (amount > 1) {
                J.s(() -> loopChange(amount - 1), 2);
            }
        }
    }

    /**
     * Reverts all the block changes provided, 200 blocks per tick
     *
     * @param blocks The blocks to remove
     */
    private void revert(Map<Block, BlockData> blocks) {
        Iterator<Map.Entry<Block, BlockData>> it = blocks.entrySet().iterator();
        Bukkit.getScheduler().runTask(KrudWorld.instance, () -> {
            int amount = 0;
            while (it.hasNext()) {
                Map.Entry<Block, BlockData> entry = it.next();
                BlockData data = entry.getValue();
                entry.getKey().setBlockData(data, false);

                it.remove();

                amount++;

                if (amount > 200) {
                    J.s(() -> revert(blocks), 1);
                }
            }
        });
    }
}