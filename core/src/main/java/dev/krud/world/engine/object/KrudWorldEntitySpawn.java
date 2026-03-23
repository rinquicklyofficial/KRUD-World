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

package dev.krud.world.engine.object;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.format.C;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.math.Vector3d;
import dev.krud.world.util.matter.MatterMarker;
import dev.krud.world.util.matter.slices.MarkerMatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;

@Snippet("entity-spawn")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents an entity spawn during initial chunk generation")
@Data
public class KrudWorldEntitySpawn implements IRare {
    private final transient AtomicCache<RNG> rng = new AtomicCache<>();
    private final transient AtomicCache<KrudWorldEntity> ent = new AtomicCache<>();
    @RegistryListResource(KrudWorldEntity.class)
    @Required
    @Desc("The entity")
    private String entity = "";
    @Desc("The energy multiplier when calculating spawn energy usage")
    private double energyMultiplier = 1;
    @MinNumber(1)
    @Desc("The 1 in RARITY chance for this entity to spawn")
    private int rarity = 1;
    @MinNumber(1)
    @Desc("The minumum of this entity to spawn")
    private int minSpawns = 1;
    @MinNumber(1)
    @Desc("The max of this entity to spawn")
    private int maxSpawns = 1;
    private transient KrudWorldSpawner referenceSpawner;
    private transient KrudWorldMarker referenceMarker;

    public int spawn(Engine gen, Chunk c, RNG rng) {
        int spawns = minSpawns == maxSpawns ? minSpawns : rng.i(Math.min(minSpawns, maxSpawns), Math.max(minSpawns, maxSpawns));
        int s = 0;

        if (spawns > 0) {
            for (int id = 0; id < spawns; id++) {
                int x = (c.getX() * 16) + rng.i(15);
                int z = (c.getZ() * 16) + rng.i(15);
                int h = gen.getHeight(x, z, true) + (gen.getWorld().tryGetRealWorld() ? gen.getWorld().realWorld().getMinHeight() : -64);
                int hf = gen.getHeight(x, z, false) + (gen.getWorld().tryGetRealWorld() ? gen.getWorld().realWorld().getMinHeight() : -64);
                Location l = switch (getReferenceSpawner().getGroup()) {
                    case NORMAL -> new Location(c.getWorld(), x, hf + 1, z);
                    case CAVE -> gen.getMantle().findMarkers(c.getX(), c.getZ(), MarkerMatter.CAVE_FLOOR)
                            .convert((i) -> i.toLocation(c.getWorld()).add(0, 1, 0)).getRandom(rng);
                    case UNDERWATER, BEACH -> new Location(c.getWorld(), x, rng.i(h + 1, hf), z);
                };

                if (l != null) {
                    if (referenceSpawner.getAllowedLightLevels().getMin() > 0 || referenceSpawner.getAllowedLightLevels().getMax() < 15) {
                        if (referenceSpawner.getAllowedLightLevels().contains(l.getBlock().getLightLevel())) {
                            if (spawn100(gen, l) != null) {
                                s++;
                            }
                        }
                    } else {
                        if (spawn100(gen, l) != null) {
                            s++;
                        }
                    }
                }
            }
        }

        return s;
    }

    public int spawn(Engine gen, KrudWorldPosition c, RNG rng) {
        int spawns = minSpawns == maxSpawns ? minSpawns : rng.i(Math.min(minSpawns, maxSpawns), Math.max(minSpawns, maxSpawns));
        int s = 0;

        if (!gen.getWorld().tryGetRealWorld()) {
            return 0;
        }

        World world = gen.getWorld().realWorld();
        if (spawns > 0) {

            if (referenceMarker != null && referenceMarker.shouldExhaust()) {
                gen.getMantle().getMantle().remove(c.getX(), c.getY() - gen.getWorld().minHeight(), c.getZ(), MatterMarker.class);
            }

            for (int id = 0; id < spawns; id++) {
                Location l = c.toLocation(world).add(0, 1, 0);

                if (referenceSpawner.getAllowedLightLevels().getMin() > 0 || referenceSpawner.getAllowedLightLevels().getMax() < 15) {
                    if (referenceSpawner.getAllowedLightLevels().contains(l.getBlock().getLightLevel())) {
                        if (spawn100(gen, l, true) != null) {
                            s++;
                        }
                    }
                } else {
                    if (spawn100(gen, l, true) != null) {
                        s++;
                    }
                }
            }
        }

        return s;
    }

    public KrudWorldEntity getRealEntity(Engine g) {
        return ent.aquire(() -> g.getData().getEntityLoader().load(getEntity()));
    }

    public Entity spawn(Engine g, Location at) {
        if (getRealEntity(g) == null) {
            return null;
        }

        if (rng.aquire(() -> new RNG(g.getSeedManager().getEntity())).i(1, getRarity()) == 1) {
            return spawn100(g, at);
        }

        return null;
    }

    private Entity spawn100(Engine g, Location at) {
        return spawn100(g, at, false);
    }

    private Entity spawn100(Engine g, Location at, boolean ignoreSurfaces) {
        try {
            KrudWorldEntity irisEntity = getRealEntity(g);
            if (irisEntity == null) { // No entity
                KrudWorld.debug("      You are trying to spawn an entity that does not exist!");
                return null;
            }

            if (!ignoreSurfaces && !irisEntity.getSurface().matches(at.clone().subtract(0, 1, 0).getBlock())) {
                return null;
            }

            Vector3d boundingBox = INMS.get().getBoundingbox(irisEntity.getType());
            if (!ignoreSurfaces && boundingBox != null) {
                boolean isClearForSpawn = isAreaClearForSpawn(at, boundingBox);
                if (!isClearForSpawn) {
                    return null;
                }
            }

            Entity e = irisEntity.spawn(g, at.add(0.5, 0.5, 0.5), rng.aquire(() -> new RNG(g.getSeedManager().getEntity())));
            if (e != null) {
                KrudWorld.debug("Spawned " + C.DARK_AQUA + "Entity<" + getEntity() + "> " + C.GREEN + e.getType() + C.LIGHT_PURPLE + " @ " + C.GRAY + e.getLocation().getX() + ", " + e.getLocation().getY() + ", " + e.getLocation().getZ());
            }

            return e;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
            KrudWorld.error("      Failed to retrieve real entity @ " + at + " (entity: " + getEntity() + ")");
            return null;
        }
    }

    private boolean isAreaClearForSpawn(Location center, Vector3d boundingBox) {
        World world = center.getWorld();
        int startX = center.getBlockX() - (int) (boundingBox.x / 2);
        int endX = center.getBlockX() + (int) (boundingBox.x / 2);
        int startY = center.getBlockY();
        int endY = center.getBlockY() + (int) boundingBox.y;
        int startZ = center.getBlockZ() - (int) (boundingBox.z / 2);
        int endZ = center.getBlockZ() + (int) (boundingBox.z / 2);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
