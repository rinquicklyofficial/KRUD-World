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

import com.google.gson.Gson;
import dev.krud.world.KrudWorld;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONException;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.misc.getHardware;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("SynchronizeOnNonFinalField")
@Data
public class KrudWorldSettings {
    public static KrudWorldSettings settings;
    private KrudWorldSettingsGeneral general = new KrudWorldSettingsGeneral();
    private KrudWorldSettingsWorld world = new KrudWorldSettingsWorld();
    private KrudWorldSettingsGUI gui = new KrudWorldSettingsGUI();
    private KrudWorldSettingsAutoconfiguration autoConfiguration = new KrudWorldSettingsAutoconfiguration();
    private KrudWorldSettingsGenerator generator = new KrudWorldSettingsGenerator();
    private KrudWorldSettingsConcurrency concurrency = new KrudWorldSettingsConcurrency();
    private KrudWorldSettingsStudio studio = new KrudWorldSettingsStudio();
    private KrudWorldSettingsPerformance performance = new KrudWorldSettingsPerformance();
    private KrudWorldSettingsUpdater updater = new KrudWorldSettingsUpdater();
    private KrudWorldSettingsPregen pregen = new KrudWorldSettingsPregen();
    private KrudWorldSettingsSentry sentry = new KrudWorldSettingsSentry();

    public static int getThreadCount(int c) {
        return Math.max(switch (c) {
            case -1, -2, -4 -> Runtime.getRuntime().availableProcessors() / -c;
            default -> Math.max(c, 2);
        }, 1);
    }

    public static KrudWorldSettings get() {
        if (settings != null) {
            return settings;
        }

        settings = new KrudWorldSettings();

        File s = KrudWorld.instance.getDataFile("settings.json");

        if (!s.exists()) {
            try {
                IO.writeAll(s, new JSONObject(new Gson().toJson(settings)).toString(4));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                KrudWorld.reportError(e);
            }
        } else {
            try {
                String ss = IO.readAll(s);
                settings = new Gson().fromJson(ss, KrudWorldSettings.class);
                try {
                    IO.writeAll(s, new JSONObject(new Gson().toJson(settings)).toString(4));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Throwable ee) {
                // KrudWorld.reportError(ee); causes a self-reference & stackoverflow
                KrudWorld.error("Configuration Error in settings.json! " + ee.getClass().getSimpleName() + ": " + ee.getMessage());
            }
        }

        return settings;
    }

    public static void invalidate() {
        synchronized (settings) {
            settings = null;
        }
    }

    public void forceSave() {
        File s = KrudWorld.instance.getDataFile("settings.json");

        try {
            IO.writeAll(s, new JSONObject(new Gson().toJson(settings)).toString(4));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            KrudWorld.reportError(e);
        }
    }

    @Data
    public static class KrudWorldSettingsAutoconfiguration {
        public boolean configureSpigotTimeoutTime = true;
        public boolean configurePaperWatchdogDelay = true;
        public boolean autoRestartOnCustomBiomeInstall = true;
    }

    @Data
    public static class KrudWorldAsyncTeleport {
        public boolean enabled = false;
        public int loadViewDistance = 2;
        public boolean urgent = false;
    }

    @Data
    public static class KrudWorldSettingsWorld {
        public KrudWorldAsyncTeleport asyncTeleport = new KrudWorldAsyncTeleport();
        public boolean postLoadBlockUpdates = true;
        public boolean forcePersistEntities = true;
        public boolean anbientEntitySpawningSystem = true;
        public long asyncTickIntervalMS = 700;
        public double targetSpawnEntitiesPerChunk = 0.95;
        public boolean markerEntitySpawningSystem = true;
        public boolean effectSystem = true;
        public boolean worldEditWandCUI = true;
        public boolean globalPregenCache = false;
    }

    @Data
    public static class KrudWorldSettingsConcurrency {
        public int parallelism = -1;
        public int ioParallelism = -2;
        public int worldGenParallelism = -1;

        public int getWorldGenThreads() {
            return getThreadCount(worldGenParallelism);
        }
    }

    @Data
    public static class KrudWorldSettingsPregen {
        public boolean useCacheByDefault = true;
        public boolean useHighPriority = false;
        public boolean useVirtualThreads = false;
        public boolean useTicketQueue = true;
        public int maxConcurrency = 256;
    }

    @Data
    public static class KrudWorldSettingsPerformance {
        private KrudWorldSettingsEngineSVC engineSVC = new KrudWorldSettingsEngineSVC();
        public boolean trimMantleInStudio = false; 
        public int mantleKeepAlive = 30;
        public int noiseCacheSize = 1_024;
        public int resourceLoaderCacheSize = 1_024;
        public int objectLoaderCacheSize = 4_096;
        public int scriptLoaderCacheSize = 512;
        public int tectonicPlateSize = -1;
        public int mantleCleanupDelay = 200;

        public int getTectonicPlateSize() {
            if (tectonicPlateSize > 0)
                return tectonicPlateSize;

            return (int) (getHardware.getProcessMemory() / 512L);
        }
    }

    @Data
    public static class KrudWorldSettingsUpdater {
        public int maxConcurrency = 256;
        public boolean nativeThreads = false;
        public double threadMultiplier = 2;

        public double chunkLoadSensitivity = 0.7;
        public MsRange emptyMsRange = new MsRange(80, 100);
        public MsRange defaultMsRange = new MsRange(20, 40);

        public int getMaxConcurrency() {
            return Math.max(Math.abs(maxConcurrency), 1);
        }

        public double getThreadMultiplier() {
            return Math.min(Math.abs(threadMultiplier), 0.1);
        }

        public double getChunkLoadSensitivity() {
            return Math.min(chunkLoadSensitivity, 0.9);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsRange {
        public int min = 20;
        public int max = 40;
    }

    @Data
    public static class KrudWorldSettingsGeneral {
        public boolean DoomsdayAnnihilationSelfDestructMode = false;
        public boolean commandSounds = true;
        public boolean debug = false;
        public boolean dumpMantleOnError = false;
        public boolean disableNMS = false;
        public boolean pluginMetrics = true;
        public boolean splashLogoStartup = true;
        public boolean useConsoleCustomColors = true;
        public boolean useCustomColorsIngame = true;
        public boolean adjustVanillaHeight = false;
        public String forceMainWorld = "";
        public int spinh = -20;
        public int spins = 7;
        public int spinb = 8;
        public String cartographerMessage = "KrudWorld does not allow cartographers in its world due to crashes.";


        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean canUseCustomColors(VolmitSender volmitSender) {
            return volmitSender.isPlayer() ? useCustomColorsIngame : useConsoleCustomColors;
        }
    }

    @Data
    public static class KrudWorldSettingsSentry {
        public boolean includeServerId = true;
        public boolean disableAutoReporting = false;
        public boolean debug = false;
    }

    @Data
    public static class KrudWorldSettingsGUI {
        public boolean useServerLaunchedGuis = true;
        public boolean maximumPregenGuiFPS = false;
        public boolean colorMode = true;
    }

    @Data
    public static class KrudWorldSettingsGenerator {
        public String defaultWorldType = "overworld";
        public int maxBiomeChildDepth = 4;
        public boolean preventLeafDecay = true;
        public boolean useMulticore = false;
        public boolean useMulticoreMantle = false;
        public boolean offsetNoiseTypes = false;
        public boolean earlyCustomBlocks = false;
    }

    @Data
    public static class KrudWorldSettingsStudio {
        public boolean studio = true;
        public boolean openVSCode = true;
        public boolean disableTimeAndWeather = true;
        public boolean autoStartDefaultStudio = false;
    }

    @Data
    public static class KrudWorldSettingsEngineSVC {
        public boolean useVirtualThreads = true;
        public boolean forceMulticoreWrite = false;
        public int priority = Thread.NORM_PRIORITY;

        public int getPriority() {
            return Math.max(Math.min(priority, Thread.MAX_PRIORITY), Thread.MIN_PRIORITY);
        }
    }
}
