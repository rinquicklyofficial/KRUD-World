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

package dev.krud.world.core.commands;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.service.StudioSVC;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.DecreeExecutor;
import dev.krud.world.util.decree.DecreeOrigin;
import dev.krud.world.util.decree.annotations.Decree;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.decree.specialhandlers.NullablePlayerHandler;
import dev.krud.world.util.format.C;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.misc.ServerProperties;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.krud.world.core.service.EditSVC.deletingWorld;
import static dev.krud.world.util.misc.ServerProperties.BUKKIT_YML;
import static org.bukkit.Bukkit.getServer;

@Decree(name = "kworld", aliases = {"kw", "iris", "ir", "irs"}, description = "Basic Command")
public class CommandKrudWorld implements DecreeExecutor {
    private CommandUpdater updater;
    private CommandStudio studio;
    private CommandPregen pregen;
    private CommandSettings settings;
    private CommandObject object;
    private CommandJigsaw jigsaw;
    private CommandWhat what;
    private CommandEdit edit;
    private CommandFind find;
    private CommandDeveloper developer;
    public static boolean worldCreation = false;
    private static final AtomicReference<Thread> mainWorld = new AtomicReference<>();
    String WorldEngine;
    String worldNameToCheck = "YourWorldName";
    VolmitSender sender = KrudWorld.getSender();

    @Decree(description = "Create a new world", aliases = {"+", "c"})
    public void create(
            @Param(aliases = "world-name", description = "The name of the world to create")
            String name,
            @Param(aliases = "dimension", description = "The dimension type to create the world with", defaultValue = "default")
            KrudWorldDimension type,
            @Param(description = "The seed to generate the world with", defaultValue = "1337")
            long seed,
            @Param(aliases = "main-world", description = "Whether or not to automatically use this world as the main world", defaultValue = "false")
            boolean main
    ) {
        if (name.equalsIgnoreCase("iris") || name.equalsIgnoreCase("kworld")) {
            sender().sendMessage(C.RED + "You cannot use that world name for creating worlds as KrudWorld uses this directory for studio worlds.");
            sender().sendMessage(C.RED + "May we suggest the name \"KrudWorld_World\" instead?");
            return;
        }

        if (name.equalsIgnoreCase("benchmark")) {
            sender().sendMessage(C.RED + "You cannot use the world name \"benchmark\" for creating worlds as KrudWorld uses this directory for Benchmarking Packs.");
            sender().sendMessage(C.RED + "May we suggest the name \"KrudWorld_World\" instead?");
            return;
        }

        if (new File(Bukkit.getWorldContainer(), name).exists()) {
            sender().sendMessage(C.RED + "That folder already exists!");
            return;
        }

        try {
            worldCreation = true;
            KrudWorldToolbelt.createWorld()
                    .dimension(type.getLoadKey())
                    .name(name)
                    .seed(seed)
                    .sender(sender())
                    .studio(false)
                    .create();
            if (main) {
                Runtime.getRuntime().addShutdownHook(mainWorld.updateAndGet(old -> {
                    if (old != null) Runtime.getRuntime().removeShutdownHook(old);
                    return new Thread(() -> updateMainWorld(name));
                }));
            }
        } catch (Throwable e) {
            sender().sendMessage(C.RED + "Exception raised during creation. See the console for more details.");
            KrudWorld.error("Exception raised during world creation: " + e.getMessage());
            KrudWorld.reportError(e);
            worldCreation = false;
            return;
        }
        worldCreation = false;
        
        sender().sendMessage("<#7c3aed>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                " <gradient:#a855f7:#f59e0b><bold>[ᴋʀᴜᴅ ᴡᴏʀʟᴅ] 🌍 World Created!</bold></gradient>\n" +
                "<#7c3aed>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                "<gray>🌍 World: <gradient:#a855f7:#f59e0b>" + name + "</gradient>\n" +
                "<gray>🗺 Dimension: <white>" + type.getLoadKey() + "\n" +
                "<gray>🌱 Seed: <white>" + seed + "\n" +
                "<#7c3aed>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        if (main) sender().sendMessage(C.GREEN + "Your world will automatically be set as the main world when the server restarts.");
    }

    @SneakyThrows
    private void updateMainWorld(String newName) {
        File worlds = Bukkit.getWorldContainer();
        var data = ServerProperties.DATA;
        try (var in = new FileInputStream(ServerProperties.SERVER_PROPERTIES)) {
            data.load(in);
        }
        for (String sub : List.of("datapacks", "playerdata", "advancements", "stats")) {
            IO.copyDirectory(new File(worlds, ServerProperties.LEVEL_NAME + "/" + sub).toPath(), new File(worlds, newName + "/" + sub).toPath());
        }

        data.setProperty("level-name", newName);
        try (var out = new FileOutputStream(ServerProperties.SERVER_PROPERTIES)) {
            data.store(out, null);
        }
    }

    @Decree(description = "Teleport to another world", aliases = {"tp"}, sync = true)
    public void teleport(
            @Param(description = "World to teleport to")
            World world,
            @Param(description = "Player to teleport", defaultValue = "---", customHandler = NullablePlayerHandler.class)
            Player player
    ) {
        if (player == null && sender().isPlayer())
            player = sender().player();

        final Player target = player;
        if (target == null) {
            sender().sendMessage(C.RED + "The specified player does not exist.");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                target.teleport(world.getSpawnLocation());
                new VolmitSender(target).sendMessage(C.GREEN + "You have been teleported to " + world.getName() + ".");
            }
        }.runTask(KrudWorld.instance);
    }

    @Decree(description = "Print version information")
    public void version() {
        sender().sendMessage(C.GREEN + "KrudWorld v" + KrudWorld.instance.getDescription().getVersion() + " by Volmit Software");
    }

    /*
    /todo
    @Decree(description = "Benchmark a pack", origin = DecreeOrigin.CONSOLE)
    public void packbenchmark(
            @Param(description = "Dimension to benchmark")
            KrudWorldDimension type
    ) throws InterruptedException {

         BenchDimension = type.getLoadKey();

        KrudWorldPackBenchmarking.runBenchmark();
    } */

    @Decree(description = "Print world height information", origin = DecreeOrigin.PLAYER)
    public void height() {
        if (sender().isPlayer()) {
            sender().sendMessage(C.GREEN + "" + sender().player().getWorld().getMinHeight() + " to " + sender().player().getWorld().getMaxHeight());
            sender().sendMessage(C.GREEN + "Total Height: " + (sender().player().getWorld().getMaxHeight() - sender().player().getWorld().getMinHeight()));
        } else {
            World mainWorld = getServer().getWorlds().get(0);
            KrudWorld.info(C.GREEN + "" + mainWorld.getMinHeight() + " to " + mainWorld.getMaxHeight());
            KrudWorld.info(C.GREEN + "Total Height: " + (mainWorld.getMaxHeight() - mainWorld.getMinHeight()));
        }
    }

    @Decree(description = "QOL command to open a overworld studio world.", sync = true)
    public void so() {
        sender().sendMessage(C.GREEN + "Opening studio for the \"Overworld\" pack (seed: 1337)");
        KrudWorld.service(StudioSVC.class).open(sender(), 1337, "overworld");
    }

    @Decree(description = "Check access of all worlds.", aliases = {"accesslist"})
    public void worlds() {
        KList<World> KrudWorldWorlds = new KList<>();
        KList<World> BukkitWorlds = new KList<>();

        for (World w : Bukkit.getServer().getWorlds()) {
            try {
                Engine engine = KrudWorldToolbelt.access(w).getEngine();
                if (engine != null) {
                    KrudWorldWorlds.add(w);
                }
            } catch (Exception e) {
                BukkitWorlds.add(w);
            }
        }

        if (sender().isPlayer()) {
            sender().sendMessage(C.BLUE + "KrudWorld Worlds: ");
            for (World KrudWorldWorld : KrudWorldWorlds.copy()) {
                sender().sendMessage(C.IRIS + "- " +KrudWorldWorld.getName());
            }
            sender().sendMessage(C.GOLD + "Bukkit Worlds: ");
            for (World BukkitWorld : BukkitWorlds.copy()) {
                sender().sendMessage(C.GRAY + "- " +BukkitWorld.getName());
            }
        } else {
            KrudWorld.info(C.BLUE + "KrudWorld Worlds: ");
            for (World KrudWorldWorld : KrudWorldWorlds.copy()) {
                KrudWorld.info(C.IRIS + "- " +KrudWorldWorld.getName());
            }
            KrudWorld.info(C.GOLD + "Bukkit Worlds: ");
            for (World BukkitWorld : BukkitWorlds.copy()) {
                KrudWorld.info(C.GRAY + "- " +BukkitWorld.getName());
            }
            
        }
    }

    @Decree(description = "Remove an KrudWorld world", aliases = {"del", "rm", "delete"}, sync = true)
    public void remove(
            @Param(description = "The world to remove")
            World world,
            @Param(description = "Whether to also remove the folder (if set to false, just does not load the world)", defaultValue = "true")
            boolean delete
    ) {
        if (!KrudWorldToolbelt.isKrudWorldWorld(world)) {
            sender().sendMessage(C.RED + "This is not an KrudWorld world. KrudWorld worlds: " + String.join(", ", getServer().getWorlds().stream().filter(KrudWorldToolbelt::isKrudWorldWorld).map(World::getName).toList()));
            return;
        }
        sender().sendMessage(C.GREEN + "Removing world: " + world.getName());

        if (!KrudWorldToolbelt.evacuate(world)) {
            sender().sendMessage(C.RED + "Failed to evacuate world: " + world.getName());
            return;
        }

        if (!Bukkit.unloadWorld(world, false)) {
            sender().sendMessage(C.RED + "Failed to unload world: " + world.getName());
            return;
        }

        try {
            if (KrudWorldToolbelt.removeWorld(world)) {
                sender().sendMessage(C.GREEN + "Successfully removed " + world.getName() + " from bukkit.yml");
            } else {
                sender().sendMessage(C.YELLOW + "Looks like the world was already removed from bukkit.yml");
            }
        } catch (IOException e) {
            sender().sendMessage(C.RED + "Failed to save bukkit.yml because of " + e.getMessage());
            e.printStackTrace();
        }
        KrudWorldToolbelt.evacuate(world, "Deleting world");
        deletingWorld = true;
        if (!delete) {
            deletingWorld = false;
            return;
        }
        VolmitSender sender = sender();
        J.a(() -> {
            int retries = 12;

            if (deleteDirectory(world.getWorldFolder())) {
                sender.sendMessage(C.GREEN + "Successfully removed world folder");
            } else {
                while(true){
                    if (deleteDirectory(world.getWorldFolder())){
                        sender.sendMessage(C.GREEN + "Successfully removed world folder");
                        break;
                    }
                    retries--;
                    if (retries == 0){
                        sender.sendMessage(C.RED + "Failed to remove world folder");
                        break;
                    }
                    J.sleep(3000);
                }
            }
            deletingWorld = false;
        });
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Decree(description = "Set aura spins")
    public void aura(
            @Param(description = "The h color value", defaultValue = "-20")
            int h,
            @Param(description = "The s color value", defaultValue = "7")
            int s,
            @Param(description = "The b color value", defaultValue = "8")
            int b
    ) {
        KrudWorldSettings.get().getGeneral().setSpinh(h);
        KrudWorldSettings.get().getGeneral().setSpins(s);
        KrudWorldSettings.get().getGeneral().setSpinb(b);
        KrudWorldSettings.get().forceSave();
        sender().sendMessage("<rainbow>Aura Spins updated to " + h + " " + s + " " + b);
    }

    @Decree(description = "Bitwise calculations")
    public void bitwise(
            @Param(description = "The first value to run calculations on")
            int value1,
            @Param(description = "The operator: | & ^ ≺≺ ≻≻ ％")
            String operator,
            @Param(description = "The second value to run calculations on")
            int value2
    ) {
        Integer v = null;
        switch (operator) {
            case "|" -> v = value1 | value2;
            case "&" -> v = value1 & value2;
            case "^" -> v = value1 ^ value2;
            case "%" -> v = value1 % value2;
            case ">>" -> v = value1 >> value2;
            case "<<" -> v = value1 << value2;
        }
        if (v == null) {
            sender().sendMessage(C.RED + "The operator you entered: (" + operator + ") is invalid!");
            return;
        }
        sender().sendMessage(C.GREEN + "" + value1 + " " + C.GREEN + operator.replaceAll("<", "≺").replaceAll(">", "≻").replaceAll("%", "％") + " " + C.GREEN + value2 + C.GREEN + " returns " + C.GREEN + v);
    }

    @Decree(description = "Toggle debug")
    public void debug(
            @Param(name = "on", description = "Whether or not debug should be on", defaultValue = "other")
            Boolean on
    ) {
        boolean to = on == null ? !KrudWorldSettings.get().getGeneral().isDebug() : on;
        KrudWorldSettings.get().getGeneral().setDebug(to);
        KrudWorldSettings.get().forceSave();
        sender().sendMessage(C.GREEN + "Set debug to: " + to);
    }

    //TODO fix pack trimming
    @Decree(description = "Download a project.", aliases = "dl")
    public void download(
            @Param(name = "pack", description = "The pack to download", defaultValue = "overworld", aliases = "project")
            String pack,
            @Param(name = "branch", description = "The branch to download from", defaultValue = "main")
            String branch,
            //@Param(name = "trim", description = "Whether or not to download a trimmed version (do not enable when editing)", defaultValue = "false")
            //boolean trim,
            @Param(name = "overwrite", description = "Whether or not to overwrite the pack with the downloaded one", aliases = "force", defaultValue = "false")
            boolean overwrite
    ) {
        boolean trim = false;
        sender().sendMessage(C.GREEN + "Downloading pack: " + pack + "/" + branch + (trim ? " trimmed" : "") + (overwrite ? " overwriting" : ""));
        if (pack.equals("overworld")) {
            String url = "https://github.com/KrudWorldDimensions/overworld/releases/download/" + INMS.OVERWORLD_TAG + "/overworld.zip";
            KrudWorld.service(StudioSVC.class).downloadRelease(sender(), url, trim, overwrite);
        } else {
            KrudWorld.service(StudioSVC.class).downloadSearch(sender(), "KrudWorldDimensions/" + pack + "/" + branch, trim, overwrite);
        }
    }

    @Decree(description = "Get metrics for your world", aliases = "measure", origin = DecreeOrigin.PLAYER)
    public void metrics() {
        if (!KrudWorldToolbelt.isKrudWorldWorld(world())) {
            sender().sendMessage(C.RED + "You must be in an KrudWorld world");
            return;
        }
        sender().sendMessage(C.GREEN + "Sending metrics...");
        engine().printMetrics(sender());
    }

    @Decree(description = "Reload configuration file (this is also done automatically)")
    public void reload() {
        KrudWorldSettings.invalidate();
        KrudWorldSettings.get();
        sender().sendMessage(C.GREEN + "Hotloaded settings");
    }

    @Decree(description = "Update the pack of a world (UNSAFE!)", name = "^world", aliases = "update-world")
    public void updateWorld(
            @Param(description = "The world to update", contextual = true)
            World world,
            @Param(description = "The pack to install into the world", contextual = true, aliases = "dimension")
            KrudWorldDimension pack,
            @Param(description = "Make sure to make a backup & read the warnings first!", defaultValue = "false", aliases = "c")
            boolean confirm,
            @Param(description = "Should KrudWorld download the pack again for you", defaultValue = "false", name = "fresh-download", aliases = {"fresh", "new"})
            boolean freshDownload
    ) {
        if (!confirm) {
            sender().sendMessage(new String[]{
                    C.RED + "You should always make a backup before using this",
                    C.YELLOW + "Issues caused by this can be, but are not limited to:",
                    C.YELLOW + " - Broken chunks (cut-offs) between old and new chunks (before & after the update)",
                    C.YELLOW + " - Regenerated chunks that do not fit in with the old chunks",
                    C.YELLOW + " - Structures not spawning again when regenerating",
                    C.YELLOW + " - Caves not lining up",
                    C.YELLOW + " - Terrain layers not lining up",
                    C.RED + "Now that you are aware of the risks, and have made a back-up:",
                    C.RED + "/iris ^world " + world.getName() + " " + pack.getLoadKey() + " confirm=true"
            });
            return;
        }

        File folder = world.getWorldFolder();
        folder.mkdirs();

        if (freshDownload) {
            KrudWorld.service(StudioSVC.class).downloadSearch(sender(), pack.getLoadKey(), false, true);
        }

        KrudWorld.service(StudioSVC.class).installIntoWorld(sender(), pack.getLoadKey(), folder);
    }

    @Decree(description = "Unload an KrudWorld World", origin = DecreeOrigin.PLAYER, sync = true)
    public void unloadWorld(
            @Param(description = "The world to unload")
            World world
    ) {
        if (!KrudWorldToolbelt.isKrudWorldWorld(world)) {
            sender().sendMessage(C.RED + "This is not an KrudWorld world. KrudWorld worlds: " + String.join(", ", getServer().getWorlds().stream().filter(KrudWorldToolbelt::isKrudWorldWorld).map(World::getName).toList()));
            return;
        }
        sender().sendMessage(C.GREEN + "Unloading world: " + world.getName());
        try {
            KrudWorldToolbelt.evacuate(world);
            Bukkit.unloadWorld(world, false);
            sender().sendMessage(C.GREEN + "World unloaded successfully.");
        } catch (Exception e) {
            sender().sendMessage(C.RED + "Failed to unload the world: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Decree(description = "Load an KrudWorld World", origin = DecreeOrigin.PLAYER, sync = true, aliases = {"import"})
    public void loadWorld(
            @Param(description = "The name of the world to load")
            String world
    ) {
        World worldloaded = Bukkit.getWorld(world);
        worldNameToCheck = world;
        boolean worldExists = doesWorldExist(worldNameToCheck);
        WorldEngine = world;

        if (!worldExists) {
            sender().sendMessage(C.YELLOW + world + " Doesnt exist on the server.");
            return;
        }

        String pathtodim = world + File.separator +"iris"+File.separator +"pack"+File.separator +"dimensions"+File.separator;
        File directory = new File(Bukkit.getWorldContainer(), pathtodim);

        String dimension = null;
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        if (fileName.endsWith(".json")) {
                            dimension = fileName.substring(0, fileName.length() - 5);
                            sender().sendMessage(C.BLUE + "Generator: " + dimension);
                        }
                    }
                }
            }
        } else {
            sender().sendMessage(C.GOLD + world + " is not an iris world.");
            return;
        }
        sender().sendMessage(C.GREEN + "Loading world: " + world);

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(BUKKIT_YML);
        String gen = "KrudWorld:" + dimension;
        ConfigurationSection section = yml.contains("worlds") ? yml.getConfigurationSection("worlds") : yml.createSection("worlds");
        if (!section.contains(world)) {
            section.createSection(world).set("generator", gen);
            try {
                yml.save(BUKKIT_YML);
                KrudWorld.info("Registered \"" + world + "\" in bukkit.yml");
            } catch (IOException e) {
                KrudWorld.error("Failed to update bukkit.yml!");
                e.printStackTrace();
                return;
            }
        }
        KrudWorld.instance.checkForBukkitWorlds(world::equals);
        sender().sendMessage(C.GREEN + world + " loaded successfully.");
    }
    @Decree(description = "Evacuate an iris world", origin = DecreeOrigin.PLAYER, sync = true)
    public void evacuate(
            @Param(description = "Evacuate the world")
            World world
    ) {
        if (!KrudWorldToolbelt.isKrudWorldWorld(world)) {
            sender().sendMessage(C.RED + "This is not an KrudWorld world. KrudWorld worlds: " + String.join(", ", getServer().getWorlds().stream().filter(KrudWorldToolbelt::isKrudWorldWorld).map(World::getName).toList()));
            return;
        }
        sender().sendMessage(C.GREEN + "Evacuating world" + world.getName());
        KrudWorldToolbelt.evacuate(world);
    }

    boolean doesWorldExist(String worldName) {
        File worldContainer = Bukkit.getWorldContainer();
        File worldDirectory = new File(worldContainer, worldName);
        return worldDirectory.exists() && worldDirectory.isDirectory();
    }
}
