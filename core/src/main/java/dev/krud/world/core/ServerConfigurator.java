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

package dev.krud.world.core;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.nms.datapack.DataVersion;
import dev.krud.world.core.nms.datapack.IDataFixer;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.format.C;
import dev.krud.world.util.misc.ServerProperties;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Stream;

public class ServerConfigurator {
    public static void configure() {
        KrudWorldSettings.KrudWorldSettingsAutoconfiguration s = KrudWorldSettings.get().getAutoConfiguration();
        if (s.isConfigureSpigotTimeoutTime()) {
            J.attempt(ServerConfigurator::increaseKeepAliveSpigot);
        }

        if (s.isConfigurePaperWatchdogDelay()) {
            J.attempt(ServerConfigurator::increasePaperWatchdog);
        }

        installDataPacks(true);
    }

    private static void increaseKeepAliveSpigot() throws IOException, InvalidConfigurationException {
        File spigotConfig = new File("spigot.yml");
        FileConfiguration f = new YamlConfiguration();
        f.load(spigotConfig);
        long tt = f.getLong("settings.timeout-time");

        long spigotTimeout = TimeUnit.MINUTES.toSeconds(5);

        if (tt < spigotTimeout) {
            KrudWorld.warn("Updating spigot.yml timeout-time: " + tt + " -> " + spigotTimeout + " (5 minutes)");
            KrudWorld.warn("You can disable this change (autoconfigureServer) in KrudWorld settings, then change back the value.");
            f.set("settings.timeout-time", spigotTimeout);
            f.save(spigotConfig);
        }
    }
    private static void increasePaperWatchdog() throws IOException, InvalidConfigurationException {
        File spigotConfig = new File("config/paper-global.yml");
        FileConfiguration f = new YamlConfiguration();
        f.load(spigotConfig);
        long tt = f.getLong("watchdog.early-warning-delay");

        long watchdog = TimeUnit.MINUTES.toMillis(3);
        if (tt < watchdog) {
            KrudWorld.warn("Updating paper.yml watchdog early-warning-delay: " + tt + " -> " + watchdog + " (3 minutes)");
            KrudWorld.warn("You can disable this change (autoconfigureServer) in KrudWorld settings, then change back the value.");
            f.set("watchdog.early-warning-delay", watchdog);
            f.save(spigotConfig);
        }
    }

    private static KList<File> getDatapacksFolder() {
        if (!KrudWorldSettings.get().getGeneral().forceMainWorld.isEmpty()) {
            return new KList<File>().qadd(new File(Bukkit.getWorldContainer(), KrudWorldSettings.get().getGeneral().forceMainWorld + "/datapacks"));
        }
        KList<File> worlds = new KList<>();
        Bukkit.getServer().getWorlds().forEach(w -> worlds.add(new File(w.getWorldFolder(), "datapacks")));
        if (worlds.isEmpty()) worlds.add(new File(Bukkit.getWorldContainer(), ServerProperties.LEVEL_NAME + "/datapacks"));
        return worlds;
    }

    public static boolean installDataPacks(boolean fullInstall) {
        return installDataPacks(DataVersion.getDefault(), fullInstall);
    }

    public static boolean installDataPacks(IDataFixer fixer, boolean fullInstall) {
        if (fixer == null) {
            KrudWorld.error("Unable to install datapacks, fixer is null!");
            return false;
        }
        KrudWorld.info("Checking Data Packs...");
        DimensionHeight height = new DimensionHeight(fixer);
        KList<File> folders = getDatapacksFolder();
        KMap<String, KSet<String>> biomes = new KMap<>();

        try (Stream<KrudWorldData> stream = allPacks()) {
            stream.flatMap(height::merge)
                    .parallel()
                    .forEach(dim -> {
                        KrudWorld.verbose("  Checking Dimension " + dim.getLoadFile().getPath());
                        dim.installBiomes(fixer, dim::getLoader, folders, biomes.computeIfAbsent(dim.getLoadKey(), k -> new KSet<>()));
                        dim.installDimensionType(fixer, folders);
                    });
        }
        KrudWorldDimension.writeShared(folders, height);
        KrudWorld.info("Data Packs Setup!");

        return fullInstall && verifyDataPacksPost(KrudWorldSettings.get().getAutoConfiguration().isAutoRestartOnCustomBiomeInstall());
    }

    private static boolean verifyDataPacksPost(boolean allowRestarting) {
        try (Stream<KrudWorldData> stream = allPacks()) {
            boolean bad = stream
                    .map(data -> {
                        KrudWorld.verbose("Checking Pack: " + data.getDataFolder().getPath());
                        var loader = data.getDimensionLoader();
                        return loader.loadAll(loader.getPossibleKeys())
                                .stream()
                                .filter(Objects::nonNull)
                                .map(ServerConfigurator::verifyDataPackInstalled)
                                .toList()
                                .contains(false);
                    })
                    .toList()
                    .contains(true);
            if (!bad) return false;
        }


        if (allowRestarting) {
            restart();
        } else if (INMS.get().supportsDataPacks()) {
            KrudWorld.error("============================================================================");
            KrudWorld.error(C.ITALIC + "You need to restart your server to properly generate custom biomes.");
            KrudWorld.error(C.ITALIC + "By continuing, KrudWorld will use backup biomes in place of the custom biomes.");
            KrudWorld.error("----------------------------------------------------------------------------");
            KrudWorld.error(C.UNDERLINE + "IT IS HIGHLY RECOMMENDED YOU RESTART THE SERVER BEFORE GENERATING!");
            KrudWorld.error("============================================================================");

            for (Player i : Bukkit.getOnlinePlayers()) {
                if (i.isOp() || i.hasPermission("iris.all")) {
                    VolmitSender sender = new VolmitSender(i, KrudWorld.instance.getTag("WARNING"));
                    sender.sendMessage("There are some KrudWorld Packs that have custom biomes in them");
                    sender.sendMessage("You need to restart your server to use these packs.");
                }
            }

            J.sleep(3000);
        }
        return true;
    }

    public static void restart() {
        J.s(() -> {
            KrudWorld.warn("New data pack entries have been installed in KrudWorld! Restarting server!");
            KrudWorld.warn("This will only happen when your pack changes (updates/first time setup)");
            KrudWorld.warn("(You can disable this auto restart in iris settings)");
            J.s(() -> {
                KrudWorld.warn("Looks like the restart command didn't work. Stopping the server instead!");
                Bukkit.shutdown();
            }, 100);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        });
    }

    public static boolean verifyDataPackInstalled(KrudWorldDimension dimension) {
        KSet<String> keys = new KSet<>();
        boolean warn = false;

        for (KrudWorldBiome i : dimension.getAllBiomes(dimension::getLoader)) {
            if (i.isCustom()) {
                for (KrudWorldBiomeCustom j : i.getCustomDerivitives()) {
                    keys.add(dimension.getLoadKey() + ":" + j.getId());
                }
            }
        }
        String key = getWorld(dimension.getLoader());
        if (key == null) key = dimension.getLoadKey();
        else key += "/" + dimension.getLoadKey();

        if (!INMS.get().supportsDataPacks()) {
            if (!keys.isEmpty()) {
                KrudWorld.warn("===================================================================================");
                KrudWorld.warn("Pack " + key + " has " + keys.size() + " custom biome(s). ");
                KrudWorld.warn("Your server version does not yet support datapacks for iris.");
                KrudWorld.warn("The world will generate these biomes as backup biomes.");
                KrudWorld.warn("====================================================================================");
            }

            return true;
        }

        for (String i : keys) {
            Object o = INMS.get().getCustomBiomeBaseFor(i);

            if (o == null) {
                KrudWorld.warn("The Biome " + i + " is not registered on the server.");
                warn = true;
            }
        }

        if (INMS.get().missingDimensionTypes(dimension.getDimensionTypeKey())) {
            KrudWorld.warn("The Dimension Type for " + dimension.getLoadFile() + " is not registered on the server.");
            warn = true;
        }

        if (warn) {
            KrudWorld.error("The Pack " + key + " is INCAPABLE of generating custom biomes");
            KrudWorld.error("If not done automatically, restart your server before generating with this pack!");
        }

        return !warn;
    }

    public static Stream<KrudWorldData> allPacks() {
        return Stream.concat(listFiles(KrudWorld.instance.getDataFolder("packs"))
                .filter(File::isDirectory)
                .filter( base -> {
                    var content = new File(base, "dimensions").listFiles();
                    return content != null && content.length > 0;
                })
                .map(KrudWorldData::get), KrudWorldWorlds.get().getPacks());
    }

    @Nullable
    public static String getWorld(@NonNull KrudWorldData data) {
        String worldContainer = Bukkit.getWorldContainer().getAbsolutePath();
        if (!worldContainer.endsWith(File.separator)) worldContainer += File.separator;
        
        String path = data.getDataFolder().getAbsolutePath();
        if (!path.startsWith(worldContainer)) return null;
        int l = path.endsWith(File.separator) ? 11 : 10;
        return path.substring(worldContainer.length(), path.length() - l);
    }

    @SneakyThrows
    private static Stream<File> listFiles(File parent) {
        if (!parent.isDirectory()) return Stream.empty();
        return Files.walk(parent.toPath()).map(Path::toFile);
    }

    public static class DimensionHeight {
        private final IDataFixer fixer;
        private final AtomicIntegerArray[] dimensions = new AtomicIntegerArray[3];

        public DimensionHeight(IDataFixer fixer) {
            this.fixer = fixer;
            for (int i = 0; i < 3; i++) {
                dimensions[i] = new AtomicIntegerArray(new int[]{
                        Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
                });
            }
        }

        public Stream<KrudWorldDimension> merge(KrudWorldData data) {
            KrudWorld.verbose("Checking Pack: " + data.getDataFolder().getPath());
            var loader = data.getDimensionLoader();
            return loader.loadAll(loader.getPossibleKeys())
                    .stream()
                    .filter(Objects::nonNull)
                    .peek(this::merge);
        }

        public void merge(KrudWorldDimension dimension) {
            AtomicIntegerArray array = dimensions[dimension.getBaseDimension().ordinal()];
            array.updateAndGet(0, min -> Math.min(min, dimension.getMinHeight()));
            array.updateAndGet(1, max -> Math.max(max, dimension.getMaxHeight()));
            array.updateAndGet(2, logical -> Math.max(logical, dimension.getLogicalHeight()));
        }

        public String[] jsonStrings() {
            var dims = IDataFixer.Dimension.values();
            var arr = new String[3];
            for (int i = 0; i < 3; i++) {
                arr[i] = jsonString(dims[i]);
            }
            return arr;
        }

        public String jsonString(IDataFixer.Dimension dimension) {
            var data = dimensions[dimension.ordinal()];
            int minY = data.get(0);
            int maxY = data.get(1);
            int logicalHeight = data.get(2);
            if (minY == Integer.MAX_VALUE || maxY == Integer.MIN_VALUE || Integer.MIN_VALUE == logicalHeight)
                return null;
            return fixer.createDimension(dimension, minY, maxY - minY, logicalHeight, null).toString(4);
        }
    }
}
