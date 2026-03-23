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

package dev.krud.world.engine.modifier;

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineAssignedModifier;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.data.B;
import dev.krud.world.util.hunk.Hunk;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class KrudWorldPerfectionModifier extends EngineAssignedModifier<BlockData> {
    private static final BlockData AIR = B.get("AIR");
    private static final BlockData WATER = B.get("WATER");

    public KrudWorldPerfectionModifier(Engine engine) {
        super(engine, "Perfection");
    }

    @Override
    public void onModify(int x, int z, Hunk<BlockData> output, boolean multicore, ChunkContext context) {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        AtomicBoolean changed = new AtomicBoolean(true);
        int passes = 0;
        AtomicInteger changes = new AtomicInteger();
        List<Integer> surfaces = new ArrayList<>();
        List<Integer> ceilings = new ArrayList<>();
        BurstExecutor burst = burst().burst(multicore);
        while (changed.get()) {
            passes++;
            changed.set(false);
            for (int i = 0; i < 16; i++) {
                int finalI = i;
                burst.queue(() -> {
                    for (int j = 0; j < 16; j++) {
                        surfaces.clear();
                        ceilings.clear();
                        int top = getHeight(output, finalI, j);
                        boolean inside = true;
                        surfaces.add(top);

                        for (int k = top; k >= 0; k--) {
                            BlockData b = output.get(finalI, k, j);
                            boolean now = b != null && !(B.isAir(b) || B.isFluid(b));

                            if (now != inside) {
                                inside = now;

                                if (inside) {
                                    surfaces.add(k);
                                } else {
                                    ceilings.add(k + 1);
                                }
                            }
                        }

                        for (int k : surfaces) {
                            BlockData tip = output.get(finalI, k, j);

                            if (tip == null) {
                                continue;
                            }

                            boolean remove = false;
                            boolean remove2 = false;

                            if (B.isDecorant(tip)) {
                                BlockData bel = output.get(finalI, k - 1, j);

                                if (bel == null) {
                                    remove = true;
                                } else if (!B.canPlaceOnto(tip.getMaterial(), bel.getMaterial())) {
                                    remove = true;
                                } else if (bel instanceof Bisected) {
                                    BlockData bb = output.get(finalI, k - 2, j);
                                    if (bb == null || !B.canPlaceOnto(bel.getMaterial(), bb.getMaterial())) {
                                        remove = true;
                                        remove2 = true;
                                    }
                                }

                                if (remove) {
                                    changed.set(true);
                                    changes.getAndIncrement();
                                    output.set(finalI, k, j, AIR);

                                    if (remove2) {
                                        changes.getAndIncrement();
                                        output.set(finalI, k - 1, j, AIR);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

        getEngine().getMetrics().getPerfection().put(p.getMilliseconds());
    }

    private int getHeight(Hunk<BlockData> output, int x, int z) {
        for (int i = output.getHeight() - 1; i >= 0; i--) {
            BlockData b = output.get(x, i, z);

            if (b != null && !B.isAir(b) && !B.isFluid(b)) {
                return i;
            }
        }

        return 0;
    }
}
