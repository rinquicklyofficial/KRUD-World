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
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.service.ExternalDataSVC;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.format.C;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.Chunks;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.*;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.krud.world.util.data.registry.Particles.ITEM;

@SuppressWarnings("ALL")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Desc("Represents an iris entity.")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldEntity extends KrudWorldRegistrant {
    @Required
    @Desc("The type of entity to spawn. To spawn a mythic mob, set this type to unknown and define mythic type.")
    private EntityType type = EntityType.UNKNOWN;

    @Desc("The SpawnReason to spawn the entity with.")
    private CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason.NATURAL;

    @Desc("The custom name of this entity")
    private String customName = "";

    @Desc("Should the name on this entity be visible even if you arent looking at it.")
    private boolean customNameVisible = false;

    @Desc("If this entity type is a mob, should it be aware of it's surroundings & interact with the world.")
    private boolean aware = true;

    @Desc("If this entity type is a creature, should it have ai goals.")
    private boolean ai = true;

    @Desc("Should this entity be glowing")
    private boolean glowing = false;

    @Desc("Should gravity apply to this entity")
    private boolean gravity = true;

    @Desc("When an entity is invulnerable it can only be damaged by players increative mode.")
    private boolean invulnerable = false;

    @Desc("When an entity is silent it will not produce any sound.")
    private boolean silent = false;

    @Desc("Should this entity be allowed to pickup items")
    private boolean pickupItems = false;

    @Desc("Should this entity be removed when far away")
    private boolean removable = false;

    @Desc("Entity helmet equipment")
    private KrudWorldLoot helmet = null;

    @Desc("Entity chestplate equipment")
    private KrudWorldLoot chestplate = null;

    @Desc("Entity boots equipment")
    private KrudWorldLoot boots = null;

    @Desc("Entity leggings equipment")
    private KrudWorldLoot leggings = null;

    @Desc("Entity main hand equipment")
    private KrudWorldLoot mainHand = null;

    @Desc("Entity off hand equipment")
    private KrudWorldLoot offHand = null;

    @Desc("Make other entities ride this entity")
    @ArrayType(min = 1, type = KrudWorldEntity.class)
    private KList<KrudWorldEntity> passengers = new KList<>();

    @Desc("Attribute modifiers for this entity")
    @ArrayType(min = 1, type = KrudWorldAttributeModifier.class)
    private KList<KrudWorldAttributeModifier> attributes = new KList<>();

    @Desc("Loot tables for drops")
    private KrudWorldLootReference loot = new KrudWorldLootReference();

    @Desc("If specified, this entity will be leashed by this entity. I.e. THIS ENTITY Leashed by SPECIFIED. This has no effect on EnderDragons, Withers, Players, or Bats.Non-living entities excluding leashes will not persist as leashholders.")
    private KrudWorldEntity leashHolder = null;

    @Desc("If specified, this entity will spawn with an effect")
    private KrudWorldEffect spawnEffect = null;

    @Desc("Simply moves the entity from below the surface slowly out of the ground as a spawn-in effect")
    private boolean spawnEffectRiseOutOfGround = false;

    @Desc("The main gene for a panda if the entity type is a panda")
    private Gene pandaMainGene = Gene.NORMAL;

    @Desc("The hidden gene for a panda if the entity type is a panda")
    private Gene pandaHiddenGene = Gene.NORMAL;

    @Desc("The this entity is ageable, set it's baby status")
    private boolean baby = false;

    @Desc("If the entity should never be culled. Useful for Jigsaws")
    private boolean keepEntity = false;

    @Desc("The surface type to spawn this mob on")
    private KrudWorldSurface surface = KrudWorldSurface.LAND;

    @RegistryListSpecialEntity
    @Desc("Create a mob from another plugin, such as Mythic Mobs. Should be in the format of a namespace of PluginName:MobName")
    private String specialType = "";

    @Desc("Set to true if you want to apply all of the settings here to the mob, even though an external plugin has already done so. Scripts are always applied.")
    private boolean applySettingsToCustomMobAnyways = false;

    @Desc("Set the entity type to UNKNOWN, then define a script here which ends with the entity variable (the result). You can use location to find the target location. You can spawn any entity this way.\nFile extension: .spawn.kts")
    @RegistryListResource(KrudWorldScript.class)
    private String spawnerScript = "";

    @ArrayType(min = 1, type = String.class)
    @Desc("Executed post spawn you can modify the entity however you want with it\nFile extension: .postspawn.kts")
    @RegistryListResource(KrudWorldScript.class)
    private KList<String> postSpawnScripts = new KList<>();

    @ArrayType(min = 1, type = KrudWorldCommand.class)
    @Desc("Run raw commands when this entity is spawned. Use {x}, {y}, and {z} for location. /summon pig {x} {y} {z}")
    private KList<KrudWorldCommand> rawCommands = new KList<>();

    public Entity spawn(Engine gen, Location at) {
        return spawn(gen, at, new RNG(at.hashCode()));
    }

    public Entity spawn(Engine gen, Location at, RNG rng) {
        if (!Chunks.isSafe(at)) {
            return null;
        }
        if (isSpawnEffectRiseOutOfGround()) {
            AtomicReference<Location> f = new AtomicReference<>(at);
            try {
                J.sfut(() -> {
                    if (Chunks.hasPlayersNearby(f.get())) {
                        Location b = f.get().clone();
                        Location start = new Location(b.getWorld(), b.getX(), b.getY() - 5, b.getZ());
                        f.set(start);
                    }
                }).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            at = f.get();
        }

        Entity ee = doSpawn(at);

        if (ee == null && !Chunks.isSafe(at)) {
            return null;
        }

        if (!spawnerScript.isEmpty() && ee == null) {
            synchronized (this) {
                try {
                    ee = (Entity) gen.getExecution().spawnMob(spawnerScript, at);
                } catch (Throwable ex) {
                    KrudWorld.error("You must return an Entity in your scripts to use entity scripts!");
                    ex.printStackTrace();
                }
            }
        }

        if (isSpecialType() && !applySettingsToCustomMobAnyways) {
            return ee;
        }

        if (ee == null) {
            return null;
        }

        Entity e = ee;
        e.setCustomName(getCustomName() != null ? C.translateAlternateColorCodes('&', getCustomName()) : null);
        e.setCustomNameVisible(isCustomNameVisible());
        e.setGlowing(isGlowing());
        e.setGravity(isGravity());
        e.setInvulnerable(isInvulnerable());
        e.setSilent(isSilent());
        e.setPersistent(isKeepEntity() || KrudWorldSettings.get().getWorld().isForcePersistEntities());

        int gg = 0;
        for (KrudWorldEntity i : passengers) {
            Entity passenger = i.spawn(gen, at, rng.nextParallelRNG(234858 + gg++));
            if (!Bukkit.isPrimaryThread()) {
                J.s(() -> e.addPassenger(passenger));
            }
        }

        if (e instanceof Attributable) {
            Attributable a = (Attributable) e;

            for (KrudWorldAttributeModifier i : getAttributes()) {
                i.apply(rng, a);
            }
        }

        if (e instanceof Lootable) {
            Lootable l = (Lootable) e;

            if (getLoot().getTables().isNotEmpty()) {
                Location finalAt = at;
                l.setLootTable(new LootTable() {
                    @Override
                    public NamespacedKey getKey() {
                        return new NamespacedKey(KrudWorld.instance, "loot-" + KrudWorldEntity.this.hashCode());
                    }

                    @Override
                    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
                        KList<ItemStack> items = new KList<>();

                        for (String fi : getLoot().getTables()) {
                            KrudWorldLootTable i = gen.getData().getLootLoader().load(fi);
                            items.addAll(i.getLoot(gen.isStudio(), rng.nextParallelRNG(345911), InventorySlotType.STORAGE, finalAt.getWorld(), finalAt.getBlockX(), finalAt.getBlockY(), finalAt.getBlockZ()));
                        }

                        return items;
                    }

                    @Override
                    public void fillInventory(Inventory inventory, Random random, LootContext context) {
                        for (ItemStack i : populateLoot(random, context)) {
                            inventory.addItem(i);
                        }

                        gen.scramble(inventory, rng);
                    }
                });
            }
        }

        if (e instanceof LivingEntity) {
            LivingEntity l = (LivingEntity) e;
            l.setAI(isAi());
            l.setCanPickupItems(isPickupItems());

            if (getLeashHolder() != null) {
                l.setLeashHolder(getLeashHolder().spawn(gen, at, rng.nextParallelRNG(234548)));
            }

            l.setRemoveWhenFarAway(isRemovable());

            if (getHelmet() != null && rng.i(1, getHelmet().getRarity()) == 1) {
                l.getEquipment().setHelmet(getHelmet().get(gen.isStudio(), rng));
            }

            if (getChestplate() != null && rng.i(1, getChestplate().getRarity()) == 1) {
                l.getEquipment().setChestplate(getChestplate().get(gen.isStudio(), rng));
            }

            if (getLeggings() != null && rng.i(1, getLeggings().getRarity()) == 1) {
                l.getEquipment().setLeggings(getLeggings().get(gen.isStudio(), rng));
            }

            if (getBoots() != null && rng.i(1, getBoots().getRarity()) == 1) {
                l.getEquipment().setBoots(getBoots().get(gen.isStudio(), rng));
            }

            if (getMainHand() != null && rng.i(1, getMainHand().getRarity()) == 1) {
                l.getEquipment().setItemInMainHand(getMainHand().get(gen.isStudio(), rng));
            }

            if (getOffHand() != null && rng.i(1, getOffHand().getRarity()) == 1) {
                l.getEquipment().setItemInOffHand(getOffHand().get(gen.isStudio(), rng));
            }
        }

        if (e instanceof Ageable && isBaby()) {
            ((Ageable) e).setBaby();
        }

        if (e instanceof Panda) {
            ((Panda) e).setMainGene(getPandaMainGene());
            ((Panda) e).setMainGene(getPandaHiddenGene());
        }

        if (e instanceof Villager) {
            Villager villager = (Villager) e;
            villager.setRemoveWhenFarAway(false);
            Bukkit.getScheduler().scheduleSyncDelayedTask(KrudWorld.instance, () -> {
                villager.setPersistent(true);
            }, 1);
        }

        if (e instanceof Mob) {
            Mob m = (Mob) e;
            m.setAware(isAware());
        }

        if (spawnEffect != null) {
            spawnEffect.apply(e);
        }

        if (postSpawnScripts.isNotEmpty()) {
            synchronized (this) {
                for (String i : postSpawnScripts) {
                    gen.getExecution().postSpawnMob(i, at, ee);
                }
            }
        }

        if (rawCommands.isNotEmpty()) {
            final Location fat = at;
            rawCommands.forEach(r -> r.run(fat));
        }

        Location finalAt1 = at;

        J.s(() -> {
            if (isSpawnEffectRiseOutOfGround() && e instanceof LivingEntity && Chunks.hasPlayersNearby(finalAt1)) {
                Location start = finalAt1.clone();
                e.setInvulnerable(true);
                ((LivingEntity) e).setAI(false);
                ((LivingEntity) e).setCollidable(false);
                ((LivingEntity) e).setNoDamageTicks(100000);
                AtomicInteger t = new AtomicInteger(0);
                AtomicInteger v = new AtomicInteger(0);
                v.set(J.sr(() -> {
                    if (t.get() > 100) {
                        J.csr(v.get());
                        return;
                    }

                    t.incrementAndGet();
                    if (e.getLocation().getBlock().getType().isSolid() || ((LivingEntity) e).getEyeLocation().getBlock().getType().isSolid()) {
                        e.teleport(start.add(new Vector(0, 0.1, 0)));
                        ItemStack itemCrackData = new ItemStack(((LivingEntity) e).getEyeLocation().clone().subtract(0, 2, 0).getBlock().getBlockData().getMaterial());
                        e.getWorld().spawnParticle(ITEM, ((LivingEntity) e).getEyeLocation(), 6, 0.2, 0.4, 0.2, 0.06f, itemCrackData);
                        if (M.r(0.2)) {
                            e.getWorld().playSound(e.getLocation(), Sound.BLOCK_CHORUS_FLOWER_GROW, 0.8f, 0.1f);
                        }
                    } else {
                        J.csr(v.get());
                        ((LivingEntity) e).setNoDamageTicks(0);
                        ((LivingEntity) e).setCollidable(true);
                        ((LivingEntity) e).setAI(true);
                        e.setInvulnerable(false);
                    }
                }, 0));
            }
        });


        return e;
    }

    private int surfaceY(Location l) {
        int m = l.getBlockY();

        while (m-- > 0) {
            Location ll = l.clone();
            ll.setY(m);

            if (ll.getBlock().getType().isSolid()) {
                return m;
            }
        }

        return 0;
    }

    private Entity doSpawn(Location at) {
        if (!Chunks.isSafe(at)) {
            return null;
        }

        if (type.equals(EntityType.UNKNOWN) && !isSpecialType()) {
            return null;
        }

        if (!Bukkit.isPrimaryThread()) {
            // Someone called spawn (worldedit maybe?) on a non server thread
            // Due to the structure of iris, we will call it sync and busy wait until it's done.
            AtomicReference<Entity> ae = new AtomicReference<>();

            try {
                J.s(() -> ae.set(doSpawn(at)));
            } catch (Throwable e) {
                return null;
            }
            PrecisionStopwatch p = PrecisionStopwatch.start();

            while (ae.get() == null) {
                J.sleep(25);

                if (p.getMilliseconds() > 500) {
                    return null;
                }
            }

            return ae.get();
        }

        if (isSpecialType()) {
            return KrudWorld.service(ExternalDataSVC.class).spawnMob(at, Identifier.fromString(specialType));
        }


        return INMS.get().spawnEntity(at, getType(), getReason());
    }

    public boolean isSpecialType() {
        return specialType != null && !specialType.equals("");
    }

    @Override
    public String getFolderName() {
        return "entities";
    }

    @Override
    public String getTypeName() {
        return "Entity";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
