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

package dev.krud.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.KrudWorldWorlds;
import dev.krud.world.core.ServerConfigurator;
import dev.krud.world.core.link.KrudWorldPapiExpansion;
import dev.krud.world.core.link.MultiverseCoreLink;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.pregenerator.LazyPregenerator;
import dev.krud.world.core.service.StudioSVC;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.EnginePanic;
import dev.krud.world.engine.object.KrudWorldCompat;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.engine.object.KrudWorldWorld;
import dev.krud.world.engine.platform.BukkitChunkGenerator;
import dev.krud.world.core.safeguard.KrudWorldSafeguard;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.exceptions.KrudWorldException;
import dev.krud.world.util.format.C;
import dev.krud.world.util.function.NastyRunnable;
import dev.krud.world.util.io.FileWatcher;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.io.InstanceState;
import dev.krud.world.util.io.JarScanner;
import dev.krud.world.util.math.M;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.misc.Bindings;
import dev.krud.world.util.misc.SlimJar;
import dev.krud.world.util.parallel.MultiBurst;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.plugin.VolmitPlugin;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.plugin.chunk.ChunkTickets;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.Queue;
import dev.krud.world.util.scheduling.ShurikenQueue;
import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("CanBeFinal")
public class KrudWorld extends VolmitPlugin implements Listener {
    private static final Queue<Runnable> syncJobs = new ShurikenQueue<>();

    public static KrudWorld instance;
    public static Bindings.Adventure audiences;
    public static MultiverseCoreLink linkMultiverseCore;
    public static KrudWorldCompat compat;
    public static FileWatcher configWatcher;
    public static ChunkTickets tickets;
    private static VolmitSender sender;
    private static Thread shutdownHook;

    static {
        try {
            InstanceState.updateInstanceId();
        } catch (Throwable ignored) {

        }
    }

    private final KList<Runnable> postShutdown = new KList<>();
    private KMap<Class<? extends KrudWorldService>, KrudWorldService> services;

    public static VolmitSender getSender() {
        if (sender == null) {
            sender = new VolmitSender(Bukkit.getConsoleSender());
            sender.setTag(instance.getTag());
        }
        return sender;
    }

    @SuppressWarnings("unchecked")
    public static <T> T service(Class<T> c) {
        return (T) instance.services.get(c);
    }

    public static void callEvent(Event e) {
        if (!e.isAsynchronous()) {
            J.s(() -> Bukkit.getPluginManager().callEvent(e));
        } else {
            Bukkit.getPluginManager().callEvent(e);
        }
    }

    public static KList<Object> initialize(String s, Class<? extends Annotation> slicedClass) {
        JarScanner js = new JarScanner(instance.getJarFile(), s);
        KList<Object> v = new KList<>();
        J.attempt(js::scan);
        for (Class<?> i : js.getClasses()) {
            if (slicedClass == null || i.isAnnotationPresent(slicedClass)) {
                try {
                    v.add(i.getDeclaredConstructor().newInstance());
                } catch (Throwable ignored) {

                }
            }
        }

        return v;
    }

    public static KList<Class<?>> getClasses(String s, Class<? extends Annotation> slicedClass) {
        JarScanner js = new JarScanner(instance.getJarFile(), s);
        KList<Class<?>> v = new KList<>();
        J.attempt(js::scan);
        for (Class<?> i : js.getClasses()) {
            if (slicedClass == null || i.isAnnotationPresent(slicedClass)) {
                try {
                    v.add(i);
                } catch (Throwable ignored) {

                }
            }
        }

        return v;
    }

    public static KList<Object> initialize(String s) {
        return initialize(s, null);
    }

    public static void sq(Runnable r) {
        synchronized (syncJobs) {
            syncJobs.queue(r);
        }
    }

    public static File getTemp() {
        return instance.getDataFolder("cache", "temp");
    }

    public static void msg(String string) {
        try {
            getSender().sendMessage(string);
        } catch (Throwable e) {
            try {
                instance.getLogger().info(instance.getTag() + string.replaceAll("(<([^>]+)>)", ""));
            } catch (Throwable ignored1) {

            }
        }
    }

    public static File getCached(String name, String url) {
        String h = IO.hash(name + "@" + url);
        File f = KrudWorld.instance.getDataFile("cache", h.substring(0, 2), h.substring(3, 5), h);

        if (!f.exists()) {
            try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(f)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    KrudWorld.verbose("Aquiring " + name);
                }
            } catch (IOException e) {
                KrudWorld.reportError(e);
            }
        }

        return f.exists() ? f : null;
    }

    public static String getNonCached(String name, String url) {
        String h = IO.hash(name + "*" + url);
        File f = KrudWorld.instance.getDataFile("cache", h.substring(0, 2), h.substring(3, 5), h);

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            KrudWorld.reportError(e);
        }

        try {
            return IO.readAll(f);
        } catch (IOException e) {
            KrudWorld.reportError(e);
        }

        return "";
    }

    public static File getNonCachedFile(String name, String url) {
        String h = IO.hash(name + "*" + url);
        File f = KrudWorld.instance.getDataFile("cache", h.substring(0, 2), h.substring(3, 5), h);
        KrudWorld.verbose("Download " + name + " -> " + url);
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            KrudWorld.reportError(e);
        }

        return f;
    }

    public static void warn(String format, Object... objs) {
        msg(C.YELLOW + String.format(format, objs));
    }

    public static void error(String format, Object... objs) {
        msg(C.RED + String.format(format, objs));
    }

    public static void debug(String string) {
        if (!KrudWorldSettings.get().getGeneral().isDebug()) {
            return;
        }

        try {
            throw new RuntimeException();
        } catch (Throwable e) {
            try {
                String[] cc = e.getStackTrace()[1].getClassName().split("\\Q.\\E");

                if (cc.length > 5) {
                    debug(cc[3] + "/" + cc[4] + "/" + cc[cc.length - 1], e.getStackTrace()[1].getLineNumber(), string);
                } else {
                    debug(cc[3] + "/" + cc[4], e.getStackTrace()[1].getLineNumber(), string);
                }
            } catch (Throwable ex) {
                debug("Origin", -1, string);
            }
        }
    }

    public static void debug(String category, int line, String string) {
        if (!KrudWorldSettings.get().getGeneral().isDebug()) {
            return;
        }
        if (KrudWorldSettings.get().getGeneral().isUseConsoleCustomColors()) {
            msg("<gradient:#095fe0:#a848db>" + category + " <#bf3b76>" + line + "<reset> " + C.LIGHT_PURPLE + string.replaceAll("\\Q<\\E", "[").replaceAll("\\Q>\\E", "]"));
        } else {
            msg(C.BLUE + category + ":" + C.AQUA + line + C.RESET + C.LIGHT_PURPLE + " " + string.replaceAll("\\Q<\\E", "[").replaceAll("\\Q>\\E", "]"));

        }
    }

    public static void verbose(String string) {
        debug(string);
    }

    public static void success(String string) {
        msg(C.IRIS + string);
    }

    public static void info(String format, Object... args) {
        msg(C.WHITE + String.format(format, args));
    }

    @SuppressWarnings("deprecation")
    public static void later(NastyRunnable object) {
        try {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(instance, () ->
            {
                try {
                    object.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                    KrudWorld.reportError(e);
                }
            }, RNG.r.i(100, 1200));
        } catch (IllegalPluginAccessException ignored) {

        }
    }

    public static int jobCount() {
        return syncJobs.size();
    }

    public static void clearQueues() {
        synchronized (syncJobs) {
            syncJobs.clear();
        }
    }

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static String getJava() {
        String javaRuntimeName = System.getProperty("java.vm.name");
        String javaRuntimeVendor = System.getProperty("java.vendor");
        String javaRuntimeVersion = System.getProperty("java.vm.version");
        return String.format("%s %s (build %s)", javaRuntimeName, javaRuntimeVendor, javaRuntimeVersion);
    }

    public static void reportErrorChunk(int x, int z, Throwable e, String extra) {
        if (KrudWorldSettings.get().getGeneral().isDebug()) {
            File f = instance.getDataFile("debug", "chunk-errors", "chunk." + x + "." + z + ".txt");

            if (!f.exists()) {
                J.attempt(() -> {
                    PrintWriter pw = new PrintWriter(f);
                    pw.println("Thread: " + Thread.currentThread().getName());
                    pw.println("First: " + new Date(M.ms()));
                    e.printStackTrace(pw);
                    pw.close();
                });
            }

            KrudWorld.debug("Chunk " + x + "," + z + " Exception Logged: " + e.getClass().getSimpleName() + ": " + C.RESET + "" + C.LIGHT_PURPLE + e.getMessage());
        }
    }

    public static void reportError(Throwable e) {
        Bindings.capture(e);
        if (KrudWorldSettings.get().getGeneral().isDebug()) {
            String n = e.getClass().getCanonicalName() + "-" + e.getStackTrace()[0].getClassName() + "-" + e.getStackTrace()[0].getLineNumber();

            if (e.getCause() != null) {
                n += "-" + e.getCause().getStackTrace()[0].getClassName() + "-" + e.getCause().getStackTrace()[0].getLineNumber();
            }

            File f = instance.getDataFile("debug", "caught-exceptions", n + ".txt");

            if (!f.exists()) {
                J.attempt(() -> {
                    PrintWriter pw = new PrintWriter(f);
                    pw.println("Thread: " + Thread.currentThread().getName());
                    pw.println("First: " + new Date(M.ms()));
                    e.printStackTrace(pw);
                    pw.close();
                });
            }

            KrudWorld.debug("Exception Logged: " + e.getClass().getSimpleName() + ": " + C.RESET + "" + C.LIGHT_PURPLE + e.getMessage());
        }
    }

    public static void dump() {
        try {
            File fi = KrudWorld.instance.getDataFile("dump", "td-" + new java.sql.Date(M.ms()) + ".txt");
            FileOutputStream fos = new FileOutputStream(fi);
            Map<Thread, StackTraceElement[]> f = Thread.getAllStackTraces();
            PrintWriter pw = new PrintWriter(fos);
            for (Thread i : f.keySet()) {
                pw.println("========================================");
                pw.println("Thread: '" + i.getName() + "' ID: " + i.getId() + " STATUS: " + i.getState().name());

                for (StackTraceElement j : f.get(i)) {
                    pw.println("    @ " + j.toString());
                }

                pw.println("========================================");
                pw.println();
                pw.println();
            }
            pw.println("[%%__USER__%%,%%__RESOURCE__%%,%%__PRODUCT__%%,%%__BUILTBYBIT__%%]");

            pw.close();
            KrudWorld.info("DUMPED! See " + fi.getAbsolutePath());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void panic() {
        EnginePanic.panic();
    }

    public static void addPanic(String s, String v) {
        EnginePanic.add(s, v);
    }

    public KrudWorld() {
        instance = this;
        SlimJar.load();
    }

    private void enable() {
        services = new KMap<>();
        setupAudience();
        Bindings.setupSentry();
        initialize("dev.krud.world.core.service").forEach((i) -> services.put((Class<? extends KrudWorldService>) i.getClass(), (KrudWorldService) i));
        IO.delete(new File("iris"));
        compat = KrudWorldCompat.configured(getDataFile("compat.json"));
        ServerConfigurator.configure();
        KrudWorldSafeguard.execute();
        getSender().setTag(getTag());
        KrudWorldSafeguard.splash();
        tickets = new ChunkTickets();
        linkMultiverseCore = new MultiverseCoreLink();
        configWatcher = new FileWatcher(getDataFile("settings.json"));
        services.values().forEach(KrudWorldService::onEnable);
        services.values().forEach(this::registerListener);
        addShutdownHook();
        J.s(() -> {
            J.a(() -> IO.delete(getTemp()));
            J.a(LazyPregenerator::loadLazyGenerators, 100);
            J.a(this::bstats);
            J.ar(this::checkConfigHotload, 60);
            J.sr(this::tickQueue, 0);
            J.s(this::setupPapi);
            J.a(ServerConfigurator::configure, 20);

            autoStartStudio();
            checkForBukkitWorlds(s -> true);
            KrudWorldToolbelt.retainMantleDataForSlice(String.class.getCanonicalName());
            KrudWorldToolbelt.retainMantleDataForSlice(BlockData.class.getCanonicalName());
        });
    }

    public void addShutdownHook() {
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }
        shutdownHook = new Thread(() -> {
            Bukkit.getWorlds()
                    .stream()
                    .map(KrudWorldToolbelt::access)
                    .filter(Objects::nonNull)
                    .forEach(PlatformChunkGenerator::close);

            MultiBurst.burst.close();
            MultiBurst.ioBurst.close();
            services.clear();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void checkForBukkitWorlds(Predicate<String> filter) {
        try {
            KrudWorldWorlds.readBukkitWorlds().forEach((s, generator) -> {
                try {
                    if (Bukkit.getWorld(s) != null || !filter.test(s)) return;

                    KrudWorld.info("Loading World: %s | Generator: %s", s, generator);
                    var gen = getDefaultWorldGenerator(s, generator);
                    var dim = loadDimension(s, generator);
                    assert dim != null && gen != null;

                    KrudWorld.info(C.LIGHT_PURPLE + "Preparing Spawn for " + s + "' using KrudWorld:" + generator + "...");
                    WorldCreator c = new WorldCreator(s)
                            .generator(gen)
                            .environment(dim.getEnvironment());
                    INMS.get().createWorld(c);
                    KrudWorld.info(C.LIGHT_PURPLE + "Loaded " + s + "!");
                } catch (Throwable e) {
                    KrudWorld.error("Failed to load world " + s + "!");
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            reportError(e);
        }
    }

    private void autoStartStudio() {
        if (KrudWorldSettings.get().getStudio().isAutoStartDefaultStudio()) {
            KrudWorld.info("Starting up auto Studio!");
            try {
                Player r = new KList<>(getServer().getOnlinePlayers()).getRandom();
                KrudWorld.service(StudioSVC.class).open(r != null ? new VolmitSender(r) : getSender(), 1337, KrudWorldSettings.get().getGenerator().getDefaultWorldType(), (w) -> {
                    J.s(() -> {
                        var spawn = w.getSpawnLocation();
                        for (Player i : getServer().getOnlinePlayers()) {
                            i.setGameMode(GameMode.SPECTATOR);
                            i.teleport(spawn);
                        }
                    });
                });
            } catch (KrudWorldException e) {
                reportError(e);
            }
        }
    }

    private void setupAudience() {
        try {
            audiences = new Bindings.Adventure(this);
        } catch (Throwable e) {
            e.printStackTrace();
            KrudWorldSettings.get().getGeneral().setUseConsoleCustomColors(false);
            KrudWorldSettings.get().getGeneral().setUseCustomColorsIngame(false);
            KrudWorld.error("Failed to setup Adventure API... No custom colors :(");
        }
    }

    public void postShutdown(Runnable r) {
        postShutdown.add(r);
    }

    public void onEnable() {
        instance = this;
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Phnom_Penh"));
        printBanner();
        setupKhmerKingdom();
        enable();
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("[KRUD World] Enabled v" + getDescription().getVersion());
        getLogger().info("[KRUD World] Based on KrudWorld by VolmitSoftware (GPL-3.0)");
        getLogger().info("[KRUD World] Original: https://github.com/VolmitSoftware/KrudWorld");
    }

    private void printBanner() {
        String[] banner = {
            "",
            "  ██╗  ██╗██╗    ██╗ ██████╗ ██████╗ ██╗     ██████╗",
            "  ██║ ██╔╝██║    ██║██╔═══██╗██╔══██╗██║     ██╔══██╗",
            "  █████╔╝ ██║ █╗ ██║██║   ██║██████╔╝██║     ██║  ██║",
            "  ██╔═██╗ ██║███╗██║██║   ██║██╔══██╗██║     ██║  ██║",
            "  ██║  ██╗╚███╔███╔╝╚██████╔╝██║  ██║███████╗██████╔╝",
            "  ╚═╝  ╚═╝ ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═════╝",
            "  World Generator v1.0.0 — Krud Studio",
            "  Based on KrudWorld (GPL-3.0) by VolmitSoftware",
            "  github.com/VolmitSoftware/KrudWorld",
            ""
        };
        for (String line : banner) {
            getLogger().info(line);
        }
    }

    private void setupKhmerKingdom() {
        File packFolder = new File(getDataFolder(), "packs/krud");
        if (!packFolder.exists()) {
            packFolder.mkdirs();
            saveResource("packs/krud/dimensions/khmer-kingdom.json", false);
            saveResource("packs/krud/biomes/khmer-jungle.json", false);
            saveResource("packs/krud/biomes/khmer-temple-plains.json", false);
            saveResource("packs/krud/biomes/khmer-river-delta.json", false);
            saveResource("packs/krud/biomes/khmer-ancient-ruins.json", false);
        }
    }

    public void onDisable() {
        if (KrudWorldSafeguard.isForceShutdown()) return;
        services.values().forEach(KrudWorldService::onDisable);
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((Plugin) this);
        postShutdown.forEach(Runnable::run);
        super.onDisable();

        J.attempt(new JarScanner(instance.getJarFile(), "", false)::scanAll);
    }

    private void setupPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KrudWorldPapiExpansion().register();
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String getTag(String subTag) {
        return KrudWorldSafeguard.mode().tag(subTag);
    }

    private void checkConfigHotload() {
        if (configWatcher.checkModified()) {
            KrudWorldSettings.invalidate();
            KrudWorldSettings.get();
            configWatcher.checkModified();
            KrudWorld.info("Hotloaded settings.json ");
        }
    }

    private void tickQueue() {
        synchronized (KrudWorld.syncJobs) {
            if (!KrudWorld.syncJobs.hasNext()) {
                return;
            }

            long ms = M.ms();

            while (KrudWorld.syncJobs.hasNext() && M.ms() - ms < 25) {
                try {
                    KrudWorld.syncJobs.next().run();
                } catch (Throwable e) {
                    e.printStackTrace();
                    KrudWorld.reportError(e);
                }
            }
        }
    }

    private void bstats() {
        if (KrudWorldSettings.get().getGeneral().isPluginMetrics()) {
            Bindings.setupBstats(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }

    public void imsg(CommandSender s, String msg) {
        s.sendMessage(C.IRIS + "[" + C.DARK_GRAY + "KrudWorld" + C.IRIS + "]" + C.GRAY + ": " + msg);
    }

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull String worldName, @Nullable String id) {
        KrudWorld.debug("Biome Provider Called for " + worldName + " using ID: " + id);
        return super.getDefaultBiomeProvider(worldName, id);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        KrudWorld.debug("Default World Generator Called for " + worldName + " using ID: " + id);
        if (id == null || id.isEmpty()) id = KrudWorldSettings.get().getGenerator().getDefaultWorldType();
        KrudWorld.debug("Generator ID: " + id + " requested by bukkit/plugin");
        KrudWorldDimension dim = loadDimension(worldName, id);
        if (dim == null) {
            throw new RuntimeException("Can't find dimension " + id + "!");
        }

        KrudWorld.debug("Assuming KrudWorldDimension: " + dim.getName());

        KrudWorldWorld w = KrudWorldWorld.builder()
                .name(worldName)
                .seed(1337)
                .environment(dim.getEnvironment())
                .worldFolder(new File(Bukkit.getWorldContainer(), worldName))
                .minHeight(dim.getMinHeight())
                .maxHeight(dim.getMaxHeight())
                .build();

        KrudWorld.debug("Generator Config: " + w.toString());

        File ff = new File(w.worldFolder(), "iris/pack");
        var files = ff.listFiles();
        if (files == null || files.length == 0)
            IO.delete(ff);

        if (!ff.exists()) {
            ff.mkdirs();
            service(StudioSVC.class).installIntoWorld(getSender(), dim.getLoadKey(), w.worldFolder());
        }

        return new BukkitChunkGenerator(w, false, ff, dim.getLoadKey());
    }

    @Nullable
    public static KrudWorldDimension loadDimension(@NonNull String worldName, @NonNull String id) {
        File pack = new File(Bukkit.getWorldContainer(), String.join(File.separator, worldName, "iris", "pack"));
        var dimension = pack.isDirectory() ? KrudWorldData.get(pack).getDimensionLoader().load(id) : null;
        if (dimension == null) dimension = KrudWorldData.loadAnyDimension(id, null);
        if (dimension == null) {
            KrudWorld.warn("Unable to find dimension type " + id + " Looking for online packs...");
            KrudWorld.service(StudioSVC.class).downloadSearch(new VolmitSender(Bukkit.getConsoleSender()), id, false);
            dimension = KrudWorldData.loadAnyDimension(id, null);

            if (dimension != null) {
                KrudWorld.info("Resolved missing dimension, proceeding.");
            }
        }

        return dimension;
    }

    public void splash() {
        KrudWorld.info("Server type & version: " + Bukkit.getName() + " v" + Bukkit.getVersion());
        KrudWorld.info("Custom Biomes: " + INMS.get().countCustomBiomes());
        printPacks();

        KrudWorldSafeguard.mode().trySplash();
    }

    private void printPacks() {
        File packFolder = KrudWorld.service(StudioSVC.class).getWorkspaceFolder();
        File[] packs = packFolder.listFiles(File::isDirectory);
        if (packs == null || packs.length == 0)
            return;
        KrudWorld.info("Custom Dimensions: " + packs.length);
        for (File f : packs)
            printPack(f);
    }

    private void printPack(File pack) {
        String dimName = pack.getName();
        String version = "???";
        try (FileReader r = new FileReader(new File(pack, "dimensions/" + dimName + ".json"))) {
            JsonObject json = JsonParser.parseReader(r).getAsJsonObject();
            if (json.has("version"))
                version = json.get("version").getAsString();
        } catch (IOException | JsonParseException ignored) {
        }
        KrudWorld.info("  " + dimName + " v" + version);
    }

    public int getKrudWorldVersion() {
        String input = KrudWorld.instance.getDescription().getVersion();
        int hyphenIndex = input.indexOf('-');
        if (hyphenIndex != -1) {
            String result = input.substring(0, hyphenIndex);
            result = result.replaceAll("\\.", "");
            return Integer.parseInt(result);
        }
        return -1;
    }

    public int getMCVersion() {
        try {
            String version = Bukkit.getVersion();
            Matcher matcher = Pattern.compile("\\(MC: ([\\d.]+)\\)").matcher(version);
            if (matcher.find()) {
                version = matcher.group(1).replaceAll("\\.", "");
                long versionNumber = Long.parseLong(version);
                if (versionNumber > Integer.MAX_VALUE) {
                    return -1;
                }
                return (int) versionNumber;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
