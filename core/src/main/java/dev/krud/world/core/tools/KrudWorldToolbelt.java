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

package dev.krud.world.core.tools;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.gui.PregeneratorJob;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.pregenerator.PregenTask;
import dev.krud.world.core.pregenerator.PregeneratorMethod;
import dev.krud.world.core.pregenerator.methods.CachedPregenMethod;
import dev.krud.world.core.pregenerator.methods.HybridPregenMethod;
import dev.krud.world.core.service.StudioSVC;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.plugin.VolmitSender;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Something you really want to wear if working on KrudWorld. Shit gets pretty hectic down there.
 * Hope you packed snacks & road sodas.
 */
public class KrudWorldToolbelt {
    @ApiStatus.Internal
    public static Map<String, Boolean> toolbeltConfiguration = new HashMap<>();

    /**
     * Will find / download / search for the dimension or return null
     * <p>
     * - You can provide a dimenson in the packs folder by the folder name
     * - You can provide a github repo by using (assumes branch is master unless specified)
     * - GithubUsername/repository
     * - GithubUsername/repository/branch
     *
     * @param dimension the dimension id such as overworld or flat
     * @return the KrudWorldDimension or null
     */
    public static KrudWorldDimension getDimension(String dimension) {
        File pack = KrudWorld.instance.getDataFolder("packs", dimension);

        if (!pack.exists()) {
            KrudWorld.service(StudioSVC.class).downloadSearch(new VolmitSender(Bukkit.getConsoleSender(), KrudWorld.instance.getTag()), dimension, false, false);
        }

        if (!pack.exists()) {
            return null;
        }

        return KrudWorldData.get(pack).getDimensionLoader().load(dimension);
    }

    /**
     * Create a world with plenty of options
     *
     * @return the creator builder
     */
    public static KrudWorldCreator createWorld() {
        return new KrudWorldCreator();
    }

    /**
     * Checks if the given world is an KrudWorld World (same as access(world) != null)
     *
     * @param world the world
     * @return true if it is an KrudWorld Access world
     */
    public static boolean isKrudWorldWorld(World world) {
        if (world == null) {
            return false;
        }

        if (world.getGenerator() instanceof PlatformChunkGenerator f) {
            f.touch(world);
            return true;
        }

        return false;
    }

    public static boolean isKrudWorldStudioWorld(World world) {
        return isKrudWorldWorld(world) && access(world).isStudio();
    }

    /**
     * Get the KrudWorld generator for the given world
     *
     * @param world the given world
     * @return the KrudWorldAccess or null if it's not an KrudWorld World
     */
    public static PlatformChunkGenerator access(World world) {
        if (isKrudWorldWorld(world)) {
            return ((PlatformChunkGenerator) world.getGenerator());
        } /*else {
            KrudWorld.warn("""
                    "---------- No World? ---------------
                    ⠀⣞⢽⢪⢣⢣⢣⢫⡺⡵⣝⡮⣗⢷⢽⢽⢽⣮⡷⡽⣜⣜⢮⢺⣜⢷⢽⢝⡽⣝
                    ⠸⡸⠜⠕⠕⠁⢁⢇⢏⢽⢺⣪⡳⡝⣎⣏⢯⢞⡿⣟⣷⣳⢯⡷⣽⢽⢯⣳⣫⠇
                    ⠀⠀⢀⢀⢄⢬⢪⡪⡎⣆⡈⠚⠜⠕⠇⠗⠝⢕⢯⢫⣞⣯⣿⣻⡽⣏⢗⣗⠏⠀
                    ⠀⠪⡪⡪⣪⢪⢺⢸⢢⢓⢆⢤⢀⠀⠀⠀⠀⠈⢊⢞⡾⣿⡯⣏⢮⠷⠁⠀⠀
                    ⠀⠀⠀⠈⠊⠆⡃⠕⢕⢇⢇⢇⢇⢇⢏⢎⢎⢆⢄⠀⢑⣽⣿⢝⠲⠉⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠀⡿⠂⠠⠀⡇⢇⠕⢈⣀⠀⠁⠡⠣⡣⡫⣂⣿⠯⢪⠰⠂⠀⠀⠀⠀
                    ⠀⠀⠀⠀⡦⡙⡂⢀⢤⢣⠣⡈⣾⡃⠠⠄⠀⡄⢱⣌⣶⢏⢊⠂⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⢝⡲⣜⡮⡏⢎⢌⢂⠙⠢⠐⢀⢘⢵⣽⣿⡿⠁⠁⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠨⣺⡺⡕⡕⡱⡑⡆⡕⡅⡕⡜⡼⢽⡻⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⣼⣳⣫⣾⣵⣗⡵⡱⡡⢣⢑⢕⢜⢕⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⣴⣿⣾⣿⣿⣿⡿⡽⡑⢌⠪⡢⡣⣣⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⡟⡾⣿⢿⢿⢵⣽⣾⣼⣘⢸⢸⣞⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠁⠇⠡⠩⡫⢿⣝⡻⡮⣒⢽⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    """);
        }*/
        return null;
    }

    /**
     * Start a pregenerator task
     *
     * @param task   the scheduled task
     * @param method the method to execute the task
     * @return the pregenerator job (already started)
     */
    public static PregeneratorJob pregenerate(PregenTask task, PregeneratorMethod method, Engine engine) {
        return pregenerate(task, method, engine, KrudWorldSettings.get().getPregen().useCacheByDefault);
    }

    /**
     * Start a pregenerator task
     *
     * @param task   the scheduled task
     * @param method the method to execute the task
     * @return the pregenerator job (already started)
     */
    public static PregeneratorJob pregenerate(PregenTask task, PregeneratorMethod method, Engine engine, boolean cached) {
        return new PregeneratorJob(task, cached && engine != null ? new CachedPregenMethod(method, engine.getWorld().name()) : method, engine);
    }

    /**
     * Start a pregenerator task. If the supplied generator is headless, headless mode is used,
     * otherwise Hybrid mode is used.
     *
     * @param task the scheduled task
     * @param gen  the KrudWorld Generator
     * @return the pregenerator job (already started)
     */
    public static PregeneratorJob pregenerate(PregenTask task, PlatformChunkGenerator gen) {
        return pregenerate(task, new HybridPregenMethod(gen.getEngine().getWorld().realWorld(),
                KrudWorldSettings.getThreadCount(KrudWorldSettings.get().getConcurrency().getParallelism())), gen.getEngine());
    }

    /**
     * Start a pregenerator task. If the supplied generator is headless, headless mode is used,
     * otherwise Hybrid mode is used.
     *
     * @param task  the scheduled task
     * @param world the World
     * @return the pregenerator job (already started)
     */
    public static PregeneratorJob pregenerate(PregenTask task, World world) {
        if (isKrudWorldWorld(world)) {
            return pregenerate(task, access(world));
        }

        return pregenerate(task, new HybridPregenMethod(world, KrudWorldSettings.getThreadCount(KrudWorldSettings.get().getConcurrency().getParallelism())), null);
    }

    /**
     * Evacuate all players from the world into literally any other world.
     * If there are no other worlds, kick them! Not the best but what's mine is mine sometimes...
     *
     * @param world the world to evac
     */
    public static boolean evacuate(World world) {
        for (World i : Bukkit.getWorlds()) {
            if (!i.getName().equals(world.getName())) {
                for (Player j : world.getPlayers()) {
                    new VolmitSender(j, KrudWorld.instance.getTag()).sendMessage("You have been evacuated from this world.");
                    j.teleport(i.getSpawnLocation());
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Evacuate all players from the world
     *
     * @param world the world to leave
     * @param m     the message
     * @return true if it was evacuated.
     */
    public static boolean evacuate(World world, String m) {
        for (World i : Bukkit.getWorlds()) {
            if (!i.getName().equals(world.getName())) {
                for (Player j : world.getPlayers()) {
                    new VolmitSender(j, KrudWorld.instance.getTag()).sendMessage("You have been evacuated from this world. " + m);
                    j.teleport(i.getSpawnLocation());
                }
                return true;
            }
        }

        return false;
    }

    public static boolean isStudio(World i) {
        return isKrudWorldWorld(i) && access(i).isStudio();
    }

    public static void retainMantleDataForSlice(String className) {
        toolbeltConfiguration.put("retain.mantle." + className, Boolean.TRUE);
    }

    public static boolean isRetainingMantleDataForSlice(String className) {
        return !toolbeltConfiguration.isEmpty() && toolbeltConfiguration.get("retain.mantle." + className) == Boolean.TRUE;
    }

    public static <T> T getMantleData(World world, int x, int y, int z, Class<T> of) {
        PlatformChunkGenerator e = access(world);
        if (e == null) {
            return null;
        }
        return e.getEngine().getMantle().getMantle().get(x, y - world.getMinHeight(), z, of);
    }

    public static <T> void deleteMantleData(World world, int x, int y, int z, Class<T> of) {
        PlatformChunkGenerator e = access(world);
        if (e == null) {
            return;
        }
        e.getEngine().getMantle().getMantle().remove(x, y - world.getMinHeight(), z, of);
    }

    public static boolean removeWorld(World world) throws IOException {
        return KrudWorldCreator.removeFromBukkitYml(world.getName());
    }
}
