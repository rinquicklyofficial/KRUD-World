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

import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.nms.container.BlockPos;
import dev.krud.world.core.nms.container.Pair;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.object.KrudWorldBiome;
import dev.krud.world.engine.object.KrudWorldJigsawStructure;
import dev.krud.world.engine.object.KrudWorldObject;
import dev.krud.world.engine.object.KrudWorldRegion;
import dev.krud.world.util.context.ChunkContext;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.Position2;
import dev.krud.world.util.math.Spiraler;
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.parallel.MultiBurst;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import dev.krud.world.util.scheduling.jobs.SingleJob;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@FunctionalInterface
public interface Locator<T> {
    static void cancelSearch() {
        if (LocatorCanceller.cancel != null) {
            LocatorCanceller.cancel.run();
            LocatorCanceller.cancel = null;
        }
    }

    static Locator<KrudWorldRegion> region(String loadKey) {
        return (e, c) -> e.getRegion((c.getX() << 4) + 8, (c.getZ() << 4) + 8).getLoadKey().equals(loadKey);
    }

    static Locator<KrudWorldJigsawStructure> jigsawStructure(String loadKey) {
        return (e, c) -> {
            KrudWorldJigsawStructure s = e.getStructureAt(c.getX(), c.getZ());
            return s != null && s.getLoadKey().equals(loadKey);
        };
    }

    static Locator<KrudWorldObject> object(String loadKey) {
        return (e, c) -> e.getObjectsAt(c.getX(), c.getZ()).contains(loadKey);
    }

    static Locator<KrudWorldBiome> surfaceBiome(String loadKey) {
        return (e, c) -> e.getSurfaceBiome((c.getX() << 4) + 8, (c.getZ() << 4) + 8).getLoadKey().equals(loadKey);
    }

    static Locator<BlockPos> poi(String type) {
        return (e, c) -> {
            Set<Pair<String, BlockPos>> pos = e.getPOIsAt((c.getX() << 4) + 8, (c.getZ() << 4) + 8);
            return pos.stream().anyMatch(p -> p.getA().equals(type));
        };
    }

    static Locator<KrudWorldBiome> caveBiome(String loadKey) {
        return (e, c) -> e.getCaveBiome((c.getX() << 4) + 8, (c.getZ() << 4) + 8).getLoadKey().equals(loadKey);
    }

    static Locator<KrudWorldBiome> caveOrMantleBiome(String loadKey) {
        return (e, c) -> {
            AtomicBoolean found = new AtomicBoolean(false);
            e.generateMatter(c.getX(), c.getZ(), true, new ChunkContext(c.getX() << 4, c.getZ() << 4, e.getComplex(), false));
            e.getMantle().getMantle().iterateChunk(c.getX(), c.getZ(), MatterCavern.class, (x, y, z, t) -> {
                if (found.get()) {
                    return;
                }

                if (t != null && t.getCustomBiome().equals(loadKey)) {
                    found.set(true);
                }
            });

            return found.get();
        };
    }

    boolean matches(Engine engine, Position2 chunk);

    default void find(Player player, boolean teleport, String message) {
        find(player, location -> {
            if (teleport) {
                J.s(() -> player.teleport(location));
            } else {
                player.sendMessage(C.GREEN + message + " at: " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
            }
        });
    }

    default void find(Player player, Consumer<Location> consumer) {
        find(player, 30_000, consumer);
    }

    default void find(Player player, long timeout, Consumer<Location> consumer) {
        AtomicLong checks = new AtomicLong();
        long ms = M.ms();
        new SingleJob("Searching", () -> {
            try {
                World world = player.getWorld();
                Engine engine = KrudWorldToolbelt.access(world).getEngine();
                Position2 at = find(engine, new Position2(player.getLocation().getBlockX() >> 4, player.getLocation().getBlockZ() >> 4), timeout, checks::set).get();

                if (at != null) {
                    consumer.accept(new Location(world, (at.getX() << 4) + 8,
                            engine.getHeight(
                                    (at.getX() << 4) + 8,
                                    (at.getZ() << 4) + 8, false),
                            (at.getZ() << 4) + 8));
                }
            } catch (WrongEngineBroException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }) {
            @Override
            public String getName() {
                return "Searched " + Form.f(checks.get()) + " Chunks";
            }

            @Override
            public int getTotalWork() {
                return (int) timeout;
            }

            @Override
            public int getWorkCompleted() {
                return (int) Math.min(M.ms() - ms, timeout - 1);
            }
        }.execute(new VolmitSender(player));
    }

    default Future<Position2> find(Engine engine, Position2 pos, long timeout, Consumer<Integer> checks) throws WrongEngineBroException {
        if (engine.isClosed()) {
            throw new WrongEngineBroException();
        }

        cancelSearch();

        return MultiBurst.burst.completeValue(() -> {
            int tc = KrudWorldSettings.getThreadCount(KrudWorldSettings.get().getConcurrency().getParallelism()) * 17;
            MultiBurst burst = MultiBurst.burst;
            AtomicBoolean found = new AtomicBoolean(false);
            Position2 cursor = pos;
            AtomicInteger searched = new AtomicInteger();
            AtomicBoolean stop = new AtomicBoolean(false);
            AtomicReference<Position2> foundPos = new AtomicReference<>();
            PrecisionStopwatch px = PrecisionStopwatch.start();
            LocatorCanceller.cancel = () -> stop.set(true);
            AtomicReference<Position2> next = new AtomicReference<>(cursor);
            Spiraler s = new Spiraler(100000, 100000, (x, z) -> next.set(new Position2(x, z)));
            s.setOffset(cursor.getX(), cursor.getZ());
            s.next();
            while (!found.get() && !stop.get() && px.getMilliseconds() < timeout) {
                BurstExecutor e = burst.burst(tc);

                for (int i = 0; i < tc; i++) {
                    Position2 p = next.get();
                    s.next();
                    e.queue(() -> {
                        if (matches(engine, p)) {
                            if (foundPos.get() == null) {
                                foundPos.set(p);
                            }

                            found.set(true);
                        }
                        searched.incrementAndGet();
                    });
                }

                e.complete();
                checks.accept(searched.get());
            }

            LocatorCanceller.cancel = null;

            if (found.get() && foundPos.get() != null) {
                return foundPos.get();
            }

            return null;
        });
    }
}
