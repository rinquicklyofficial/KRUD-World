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

import com.google.gson.JsonSyntaxException;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.ServerConfigurator;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.pack.KrudWorldPack;
import dev.krud.world.core.project.KrudWorldProject;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.exceptions.KrudWorldException;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONException;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

public class StudioSVC implements KrudWorldService {
    public static final String LISTING = "https://raw.githubusercontent.com/KrudWorldDimensions/_listing/main/listing-v2.json";
    public static final String WORKSPACE_NAME = "packs";
    private static final AtomicCache<Integer> counter = new AtomicCache<>();
    private final KMap<String, String> cacheListing = null;
    private KrudWorldProject activeProject;

    @Override
    public void onEnable() {
        J.s(() -> {
            String pack = KrudWorldSettings.get().getGenerator().getDefaultWorldType();
            File f = KrudWorldPack.packsPack(pack);

            if (!f.exists()) {
                KrudWorld.info("Downloading Default Pack " + pack);
                if (pack.equals("overworld")) {
                    String url = "https://github.com/KrudWorldDimensions/overworld/releases/download/" + INMS.OVERWORLD_TAG + "/overworld.zip";
                    KrudWorld.service(StudioSVC.class).downloadRelease(KrudWorld.getSender(), url, false, false);
                } else {
                    downloadSearch(KrudWorld.getSender(), pack, false);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        KrudWorld.debug("Studio Mode Active: Closing Projects");

        for (World i : Bukkit.getWorlds()) {
            if (KrudWorldToolbelt.isKrudWorldWorld(i)) {
                if (KrudWorldToolbelt.isStudio(i)) {
                    KrudWorldToolbelt.evacuate(i);
                    KrudWorldToolbelt.access(i).close();
                }
            }
        }
    }

    public KrudWorldDimension installIntoWorld(VolmitSender sender, String type, File folder) {
        return installInto(sender, type, new File(folder, "iris/pack"));
    }

    public KrudWorldDimension installInto(VolmitSender sender, String type, File folder) {
        sender.sendMessage("Looking for Package: " + type);
        KrudWorldDimension dim = KrudWorldData.loadAnyDimension(type, null);

        if (dim == null) {
            for (File i : getWorkspaceFolder().listFiles()) {
                if (i.isFile() && i.getName().equals(type + ".iris")) {
                    sender.sendMessage("Found " + type + ".iris in " + WORKSPACE_NAME + " folder");
                    ZipUtil.unpack(i, folder);
                    break;
                }
            }
        } else {
            sender.sendMessage("Found " + type + " dimension in " + WORKSPACE_NAME + " folder. Repackaging");
            File f = new KrudWorldProject(new File(getWorkspaceFolder(), type)).getPath();

            try {
                FileUtils.copyDirectory(f, folder);
            } catch (IOException e) {
                KrudWorld.reportError(e);
            }
        }

        File dimensionFile = new File(folder, "dimensions/" + type + ".json");

        if (!dimensionFile.exists() || !dimensionFile.isFile()) {
            downloadSearch(sender, type, false);
            File downloaded = getWorkspaceFolder(type);

            for (File i : downloaded.listFiles()) {
                if (i.isFile()) {
                    try {
                        FileUtils.copyFile(i, new File(folder, i.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        KrudWorld.reportError(e);
                    }
                } else {
                    try {
                        FileUtils.copyDirectory(i, new File(folder, i.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        KrudWorld.reportError(e);
                    }
                }
            }

            IO.delete(downloaded);
        }

        if (!dimensionFile.exists() || !dimensionFile.isFile()) {
            sender.sendMessage("Can't find the " + dimensionFile.getName() + " in the dimensions folder of this pack! Failed!");
            return null;
        }

        KrudWorldData dm = KrudWorldData.get(folder);
        dm.hotloaded();
        dim = dm.getDimensionLoader().load(type);

        if (dim == null) {
            sender.sendMessage("Can't load the dimension! Failed!");
            return null;
        }

        sender.sendMessage(folder.getName() + " type installed. ");
        return dim;
    }

    public void downloadSearch(VolmitSender sender, String key, boolean trim) {
        downloadSearch(sender, key, trim, false);
    }

    public void downloadSearch(VolmitSender sender, String key, boolean trim, boolean forceOverwrite) {
        String url = "?";

        try {
            url = getListing(false).get(key);

            if (url == null) {
                KrudWorld.warn("ITS ULL for " + key);
            }

            url = url == null ? key : url;
            KrudWorld.info("Assuming URL " + url);
            String branch = "master";
            String[] nodes = url.split("\\Q/\\E");
            String repo = nodes.length == 1 ? "KrudWorldDimensions/" + nodes[0] : nodes[0] + "/" + nodes[1];
            branch = nodes.length > 2 ? nodes[2] : branch;
            download(sender, repo, branch, trim, forceOverwrite, false);
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
            sender.sendMessage("Failed to download '" + key + "' from " + url + ".");
        }
    }

    public void downloadRelease(VolmitSender sender, String url, boolean trim, boolean forceOverwrite) {
        try {
            download(sender, "KrudWorldDimensions", url, trim, forceOverwrite, true);
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
            sender.sendMessage("Failed to download 'KrudWorldDimensions/overworld' from " + url + ".");
        }
    }

    public void download(VolmitSender sender, String repo, String branch, boolean trim) throws JsonSyntaxException, IOException {
        download(sender, repo, branch, trim, false, false);
    }

    public void download(VolmitSender sender, String repo, String branch, boolean trim, boolean forceOverwrite, boolean directUrl) throws JsonSyntaxException, IOException {
        String url = directUrl ? branch : "https://codeload.github.com/" + repo + "/zip/refs/heads/" + branch;
        sender.sendMessage("Downloading " + url + " "); //The extra space stops a bug in adventure API from repeating the last letter of the URL
        File zip = KrudWorld.getNonCachedFile("pack-" + trim + "-" + repo, url);
        File temp = KrudWorld.getTemp();
        File work = new File(temp, "dl-" + UUID.randomUUID());
        File packs = getWorkspaceFolder();

        if (zip == null || !zip.exists()) {
            sender.sendMessage("Failed to find pack at " + url);
            sender.sendMessage("Make sure you specified the correct repo and branch!");
            sender.sendMessage("For example: /iris download KrudWorldDimensions/overworld branch=master");
            return;
        }
        sender.sendMessage("Unpacking " + repo);
        try {
            ZipUtil.unpack(zip, work);
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
            sender.sendMessage(
                    """
                            Issue when unpacking. Please check/do the following:
                            1. Do you have a functioning internet connection?
                            2. Did the download corrupt?
                            3. Try deleting the */plugins/iris/packs folder and re-download.
                            4. Download the pack from the GitHub repo: https://github.com/KrudWorldDimensions/overworld
                            5. Contact support (if all other options do not help)"""
            );
        }
        File dir = null;
        File[] zipFiles = work.listFiles();

        if (zipFiles == null) {
            sender.sendMessage("No files were extracted from the zip file.");
            return;
        }

        try {
            dir = zipFiles.length > 1 ? work : zipFiles[0].isDirectory() ? zipFiles[0] : null;
        } catch (NullPointerException e) {
            KrudWorld.reportError(e);
            sender.sendMessage("Error when finding home directory. Are there any non-text characters in the file name?");
            return;
        }

        if (dir == null) {
            sender.sendMessage("Invalid Format. Missing root folder or too many folders!");
            return;
        }

        KrudWorldData data = KrudWorldData.get(dir);
        String[] dimensions = data.getDimensionLoader().getPossibleKeys();

        if (dimensions == null || dimensions.length == 0) {
            sender.sendMessage("No dimension file found in the extracted zip file.");
            sender.sendMessage("Check it is there on GitHub and report this to staff!");
        } else if (dimensions.length != 1) {
            sender.sendMessage("Dimensions folder must have 1 file in it");
            return;
        }

        KrudWorldDimension d = data.getDimensionLoader().load(dimensions[0]);
        data.close();

        if (d == null) {
            sender.sendMessage("Invalid dimension (folder) in dimensions folder");
            return;
        }

        String key = d.getLoadKey();
        sender.sendMessage("Importing " + d.getName() + " (" + key + ")");
        File packEntry = new File(packs, key);

        if (forceOverwrite) {
            IO.delete(packEntry);
        }

        if (KrudWorldData.loadAnyDimension(key, null) != null) {
            sender.sendMessage("Another dimension in the packs folder is already using the key " + key + " IMPORT FAILED!");
            return;
        }

        if (packEntry.exists() && packEntry.listFiles().length > 0) {
            sender.sendMessage("Another pack is using the key " + key + ". IMPORT FAILED!");
            return;
        }

        FileUtils.copyDirectory(dir, packEntry);

        if (trim) {
            sender.sendMessage("Trimming " + key);
            File cp = compilePackage(sender, key, false, false);
            IO.delete(packEntry);
            packEntry.mkdirs();
            ZipUtil.unpack(cp, packEntry);
        }
        KrudWorldData.getLoaded(packEntry)
                .ifPresent(KrudWorldData::hotloaded);

        sender.sendMessage("Successfully Aquired " + d.getName());
        ServerConfigurator.installDataPacks(true);
    }

    public KMap<String, String> getListing(boolean cached) {
        JSONObject a;

        if (cached) {
            a = new JSONObject(KrudWorld.getCached("cachedlisting", LISTING));
        } else {
            a = new JSONObject(KrudWorld.getNonCached(true + "listing", LISTING));
        }

        KMap<String, String> l = new KMap<>();

        for (String i : a.keySet()) {
            if (a.get(i) instanceof String)
                l.put(i, a.getString(i));
        }

        // TEMP FIX
        l.put("KrudWorldDimensions/overworld/master", "KrudWorldDimensions/overworld/stable");
        l.put("overworld", "KrudWorldDimensions/overworld/stable");
        return l;
    }

    public boolean isProjectOpen() {
        return activeProject != null && activeProject.isOpen();
    }

    public void open(VolmitSender sender, String dimm) {
        open(sender, 1337, dimm);
    }

    public void open(VolmitSender sender, long seed, String dimm) {
        try {
            open(sender, seed, dimm, (w) -> {
            });
        } catch (Exception e) {
            KrudWorld.reportError(e);
            sender.sendMessage("Error when creating studio world:");
            e.printStackTrace();
        }
    }

    public void open(VolmitSender sender, long seed, String dimm, Consumer<World> onDone) throws KrudWorldException {
        if (isProjectOpen()) {
            close();
        }

        KrudWorldProject project = new KrudWorldProject(new File(getWorkspaceFolder(), dimm));
        activeProject = project;
        project.open(sender, seed, onDone);
    }

    public void openVSCode(VolmitSender sender, String dim) {
        new KrudWorldProject(new File(getWorkspaceFolder(), dim)).openVSCode(sender);
    }

    public File getWorkspaceFolder(String... sub) {
        return KrudWorld.instance.getDataFolderList(WORKSPACE_NAME, sub);
    }

    public File getWorkspaceFile(String... sub) {
        return KrudWorld.instance.getDataFileList(WORKSPACE_NAME, sub);
    }

    public void close() {
        if (isProjectOpen()) {
            KrudWorld.debug("Closing Active Project");
            activeProject.close();
            activeProject = null;
        }
    }

    public File compilePackage(VolmitSender sender, String d, boolean obfuscate, boolean minify) {
        return new KrudWorldProject(new File(getWorkspaceFolder(), d)).compilePackage(sender, obfuscate, minify);
    }

    public void createFrom(String existingPack, String newName) {
        File importPack = getWorkspaceFolder(existingPack);
        File newPack = getWorkspaceFolder(newName);

        if (importPack.listFiles().length == 0) {
            KrudWorld.warn("Couldn't find the pack to create a new dimension from.");
            return;
        }

        try {
            FileUtils.copyDirectory(importPack, newPack, pathname -> !pathname.getAbsolutePath().contains(".git"), false);
        } catch (IOException e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }

        new File(importPack, existingPack + ".code-workspace").delete();
        File dimFile = new File(importPack, "dimensions/" + existingPack + ".json");
        File newDimFile = new File(newPack, "dimensions/" + newName + ".json");

        try {
            FileUtils.copyFile(dimFile, newDimFile);
        } catch (IOException e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }

        new File(newPack, "dimensions/" + existingPack + ".json").delete();

        try {
            JSONObject json = new JSONObject(IO.readAll(newDimFile));

            if (json.has("name")) {
                json.put("name", Form.capitalizeWords(newName.replaceAll("\\Q-\\E", " ")));
                IO.writeAll(newDimFile, json.toString(4));
            }
        } catch (JSONException | IOException e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }

        try {
            KrudWorldProject p = new KrudWorldProject(getWorkspaceFolder(newName));
            JSONObject ws = p.createCodeWorkspaceConfig();
            IO.writeAll(getWorkspaceFile(newName, newName + ".code-workspace"), ws.toString(0));
        } catch (JSONException | IOException e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }
    }

    public void create(VolmitSender sender, String s, String downloadable) {
        boolean shouldDelete = false;
        File importPack = getWorkspaceFolder(downloadable);

        if (importPack.listFiles().length == 0) {
            downloadSearch(sender, downloadable, false);

            if (importPack.listFiles().length > 0) {
                shouldDelete = true;
            }
        }

        if (importPack.listFiles().length == 0) {
            sender.sendMessage("Couldn't find the pack to create a new dimension from.");
            return;
        }

        File importDimensionFile = new File(importPack, "dimensions/" + downloadable + ".json");

        if (!importDimensionFile.exists()) {
            sender.sendMessage("Missing Imported Dimension File");
            return;
        }

        sender.sendMessage("Importing " + downloadable + " into new Project " + s);
        createFrom(downloadable, s);
        if (shouldDelete) {
            importPack.delete();
        }
        open(sender, s);
    }

    public void create(VolmitSender sender, String s) {
        create(sender, s, "example");
    }

    public KrudWorldProject getActiveProject() {
        return activeProject;
    }

    public void updateWorkspace() {
        if (isProjectOpen()) {
            activeProject.updateWorkspace();
        }
    }
}
