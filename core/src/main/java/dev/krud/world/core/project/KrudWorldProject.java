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

package dev.krud.world.core.project;

import com.google.gson.Gson;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.core.loader.ResourceLoader;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.object.*;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.exceptions.KrudWorldException;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONArray;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.M;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.O;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import dev.krud.world.util.scheduling.jobs.Job;
import dev.krud.world.util.scheduling.jobs.JobCollection;
import dev.krud.world.util.scheduling.jobs.ParallelQueueJob;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.dom4j.Document;
import org.dom4j.Element;
import org.zeroturnaround.zip.ZipUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
@Data
public class KrudWorldProject {
    private File path;
    private String name;
    private PlatformChunkGenerator activeProvider;

    public KrudWorldProject(File path) {
        this.path = path;
        this.name = path.getName();
    }

    public static int clean(VolmitSender s, File clean) {
        int c = 0;
        if (clean.isDirectory()) {
            for (File i : clean.listFiles()) {
                c += clean(s, i);
            }
        } else if (clean.getName().endsWith(".json")) {
            try {
                clean(clean);
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                KrudWorld.error("Failed to beautify " + clean.getAbsolutePath() + " You may have errors in your json!");
            }

            c++;
        }

        return c;
    }

    public static void clean(File clean) throws IOException {
        JSONObject obj = new JSONObject(IO.readAll(clean));
        fixBlocks(obj, clean);

        IO.writeAll(clean, obj.toString(4));
    }

    public static void fixBlocks(JSONObject obj, File f) {
        for (String i : obj.keySet()) {
            Object o = obj.get(i);

            if (i.equals("block") && o instanceof String && !o.toString().trim().isEmpty() && !o.toString().contains(":")) {
                obj.put(i, "minecraft:" + o);
                KrudWorld.debug("Updated Block Key: " + o + " to " + obj.getString(i) + " in " + f.getPath());
            }

            if (o instanceof JSONObject) {
                fixBlocks((JSONObject) o, f);
            } else if (o instanceof JSONArray) {
                fixBlocks((JSONArray) o, f);
            }
        }
    }

    public static void fixBlocks(JSONArray obj, File f) {
        for (int i = 0; i < obj.length(); i++) {
            Object o = obj.get(i);

            if (o instanceof JSONObject) {
                fixBlocks((JSONObject) o, f);
            } else if (o instanceof JSONArray) {
                fixBlocks((JSONArray) o, f);
            }
        }
    }

    public boolean isOpen() {
        return activeProvider != null;
    }

    public KList<File> collectFiles(File f, String fileExtension) {
        KList<File> l = new KList<>();

        if (f.isDirectory()) {
            for (File i : f.listFiles()) {
                l.addAll(collectFiles(i, fileExtension));
            }
        } else if (f.getName().endsWith("." + fileExtension)) {
            l.add(f);
        }

        return l;
    }

    public KList<File> collectFiles(String json) {
        return collectFiles(path, json);
    }

    public void open(VolmitSender sender) throws KrudWorldException {
        open(sender, 1337, (w) ->
        {
        });
    }

    public void openVSCode(VolmitSender sender) {

        KrudWorldDimension d = KrudWorldData.loadAnyDimension(getName(), null);
        J.attemptAsync(() ->
        {
            try {
                if (d.getLoader() == null) {
                    sender.sendMessage("Could not get dimension loader");
                    return;
                }
                File f = d.getLoader().getDataFolder();

                if (!doOpenVSCode(f)) {
                    File ff = new File(d.getLoader().getDataFolder(), d.getLoadKey() + ".code-workspace");
                    KrudWorld.warn("Project missing code-workspace: " + ff.getAbsolutePath() + " Re-creating code workspace.");

                    try {
                        IO.writeAll(ff, createCodeWorkspaceConfig());
                    } catch (IOException e1) {
                        KrudWorld.reportError(e1);
                        e1.printStackTrace();
                    }
                    updateWorkspace();
                    if (!doOpenVSCode(f)) {
                        KrudWorld.warn("Tried creating code workspace but failed a second time. Your project is likely corrupt.");
                    }
                }
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
        });
    }

    private boolean doOpenVSCode(File f) throws IOException {
        boolean foundWork = false;
        for (File i : Objects.requireNonNull(f.listFiles())) {
            if (i.getName().endsWith(".code-workspace")) {
                foundWork = true;
                J.a(() ->
                {
                    updateWorkspace();
                });

                if (KrudWorldSettings.get().getStudio().isOpenVSCode()) {
                    if (!GraphicsEnvironment.isHeadless()) {
                        KrudWorld.msg("Opening VSCode. You may see the output from VSCode.");
                        KrudWorld.msg("VSCode output always starts with: '(node:#####) electron'");
                        Desktop.getDesktop().open(i);
                    }
                }

                break;
            }
        }
        return foundWork;
    }

    public void open(VolmitSender sender, long seed, Consumer<World> onDone) throws KrudWorldException {
        if (isOpen()) {
            close();
        }

        J.a(() -> {
            KrudWorldDimension d = KrudWorldData.loadAnyDimension(getName(), null);
            if (d == null) {
                sender.sendMessage("Can't find dimension: " + getName());
                return;
            } else if (sender.isPlayer()) {
                J.s(() -> sender.player().setGameMode(GameMode.SPECTATOR));
            }

            try {
                activeProvider = (PlatformChunkGenerator) KrudWorldToolbelt.createWorld()
                        .seed(seed)
                        .sender(sender)
                        .studio(true)
                        .name("iris/" + UUID.randomUUID())
                        .dimension(d.getLoadKey())
                        .create().getGenerator();
                onDone.accept(activeProvider.getTarget().getWorld().realWorld());
            } catch (KrudWorldException e) {
                e.printStackTrace();
            }

            openVSCode(sender);
        });
    }

    public void close() {
        KrudWorld.debug("Closing Active Provider");
        KrudWorldToolbelt.evacuate(activeProvider.getTarget().getWorld().realWorld());
        activeProvider.close();
        File folder = activeProvider.getTarget().getWorld().worldFolder();
        KrudWorld.linkMultiverseCore.removeFromConfig(activeProvider.getTarget().getWorld().name());
        Bukkit.unloadWorld(activeProvider.getTarget().getWorld().name(), false);
        J.attemptAsync(() -> IO.delete(folder));
        KrudWorld.debug("Closed Active Provider " + activeProvider.getTarget().getWorld().name());
        activeProvider = null;
    }

    public File getCodeWorkspaceFile() {
        return new File(path, getName() + ".code-workspace");
    }

    public boolean updateWorkspace() {
        getPath().mkdirs();
        File ws = getCodeWorkspaceFile();

        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            JSONObject j = createCodeWorkspaceConfig();
            IO.writeAll(ws, j.toString(4));
            p.end();
            return true;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            KrudWorld.warn("Project invalid: " + ws.getAbsolutePath() + " Re-creating. You may loose some vs-code workspace settings! But not your actual project!");
            ws.delete();
            try {
                IO.writeAll(ws, createCodeWorkspaceConfig());
            } catch (IOException e1) {
                KrudWorld.reportError(e1);
                e1.printStackTrace();
            }
        }

        return false;
    }

    public JSONObject createCodeWorkspaceConfig() {
        JSONObject ws = new JSONObject();
        JSONArray folders = new JSONArray();
        JSONObject folder = new JSONObject();
        folder.put("path", ".");
        folders.put(folder);
        ws.put("folders", folders);
        JSONObject settings = new JSONObject();
        settings.put("workbench.colorTheme", "Monokai");
        settings.put("workbench.preferredDarkColorTheme", "Solarized Dark");
        settings.put("workbench.tips.enabled", false);
        settings.put("workbench.tree.indent", 24);
        settings.put("files.autoSave", "onFocusChange");
        JSONObject jc = new JSONObject();
        jc.put("editor.autoIndent", "brackets");
        jc.put("editor.acceptSuggestionOnEnter", "smart");
        jc.put("editor.cursorSmoothCaretAnimation", true);
        jc.put("editor.dragAndDrop", false);
        jc.put("files.trimTrailingWhitespace", true);
        jc.put("diffEditor.ignoreTrimWhitespace", true);
        jc.put("files.trimFinalNewlines", true);
        jc.put("editor.suggest.showKeywords", false);
        jc.put("editor.suggest.showSnippets", false);
        jc.put("editor.suggest.showWords", false);
        JSONObject st = new JSONObject();
        st.put("strings", true);
        jc.put("editor.quickSuggestions", st);
        jc.put("editor.suggest.insertMode", "replace");
        settings.put("[json]", jc);
        settings.put("json.maxItemsComputed", 30000);
        JSONArray schemas = new JSONArray();
        KrudWorldData dm = KrudWorldData.get(getPath());

        for (ResourceLoader<?> r : dm.getLoaders().v()) {
            if (r.supportsSchemas()) {
                schemas.put(r.buildSchema());
            }
        }

        for (Class<?> i : dm.resolveSnippets()) {
            try {
                String snipType = i.getDeclaredAnnotation(Snippet.class).value();
                JSONObject o = new JSONObject();
                KList<String> fm = new KList<>();

                for (int g = 1; g < 8; g++) {
                    fm.add("/snippet/" + snipType + Form.repeat("/*", g) + ".json");
                }

                o.put("fileMatch", new JSONArray(fm.toArray()));
                o.put("url", "./.iris/schema/snippet/" + snipType + "-schema.json");
                schemas.put(o);
                File a = new File(dm.getDataFolder(), ".iris/schema/snippet/" + snipType + "-schema.json");
                J.attemptAsync(() -> {
                    try {
                        IO.writeAll(a, new SchemaBuilder(i, dm).construct().toString(4));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        settings.put("json.schemas", schemas);
        ws.put("settings", settings);

        dm.getEnvironment().configureProject();
        File schemasFile = new File(path, ".idea" + File.separator + "jsonSchemas.xml");
        Document doc = IO.read(schemasFile);
        Element mappings = (Element) doc.selectSingleNode("//component[@name='JsonSchemaMappingsProjectConfiguration']");
        if (mappings == null) {
            mappings = doc.getRootElement()
                    .addElement("component")
                    .addAttribute("name", "JsonSchemaMappingsProjectConfiguration");
        }

        Element state = (Element) mappings.selectSingleNode("state");
        if (state == null) state = mappings.addElement("state");

        Element map = (Element) state.selectSingleNode("map");
        if (map == null) map = state.addElement("map");
        var schemaMap = new KMap<String, String>();
        schemas.forEach(element -> {
            if (!(element instanceof JSONObject obj))
                return;

            String url = obj.getString("url");
            String dir = obj.getJSONArray("fileMatch").getString(0);
            schemaMap.put(url, dir.substring(1, dir.indexOf("/*")));
        });

        map.selectNodes("entry/value/SchemaInfo/option[@name='relativePathToSchema']")
                .stream()
                .map(node -> node.valueOf("@value"))
                .forEach(schemaMap::remove);

        var ideaSchemas = map;
        schemaMap.forEach((url, dir) -> {
            var genName = UUID.randomUUID().toString();

            var info = ideaSchemas.addElement("entry")
                    .addAttribute("key", genName)
                    .addElement("value")
                    .addElement("SchemaInfo");
            info.addElement("option")
                    .addAttribute("name", "generatedName")
                    .addAttribute("value", genName);
            info.addElement("option")
                    .addAttribute("name", "name")
                    .addAttribute("value", dir);
            info.addElement("option")
                    .addAttribute("name", "relativePathToSchema")
                    .addAttribute("value", url);


            var item = info.addElement("option")
                    .addAttribute("name", "patterns")
                    .addElement("list")
                    .addElement("Item");
            item.addElement("option")
                    .addAttribute("name", "directory")
                    .addAttribute("value", "true");
            item.addElement("option")
                    .addAttribute("name", "path")
                    .addAttribute("value", dir);
            item.addElement("option")
                    .addAttribute("name", "mappingKind")
                    .addAttribute("value", "Directory");
        });
        if (!schemaMap.isEmpty()) {
            IO.write(schemasFile, doc);
        }
        Gradle.wrapper(path);

        return ws;
    }

    public File compilePackage(VolmitSender sender, boolean obfuscate, boolean minify) {
        String dimm = getName();
        KrudWorldData dm = KrudWorldData.get(path);
        KrudWorldDimension dimension = dm.getDimensionLoader().load(dimm);
        File folder = new File(KrudWorld.instance.getDataFolder(), "exports/" + dimension.getLoadKey());
        folder.mkdirs();
        KrudWorld.info("Packaging Dimension " + dimension.getName() + " " + (obfuscate ? "(Obfuscated)" : ""));
        KSet<KrudWorldRegion> regions = new KSet<>();
        KSet<KrudWorldBiome> biomes = new KSet<>();
        KSet<KrudWorldEntity> entities = new KSet<>();
        KSet<KrudWorldSpawner> spawners = new KSet<>();
        KSet<KrudWorldGenerator> generators = new KSet<>();
        KSet<KrudWorldLootTable> loot = new KSet<>();
        KSet<KrudWorldBlockData> blocks = new KSet<>();

        for (String i : dm.getBlockLoader().getPossibleKeys()) {
            blocks.add(dm.getBlockLoader().load(i));
        }

        dimension.getRegions().forEach((i) -> regions.add(dm.getRegionLoader().load(i)));
        dimension.getLoot().getTables().forEach((i) -> loot.add(dm.getLootLoader().load(i)));
        regions.forEach((i) -> biomes.addAll(i.getAllBiomes(() -> dm)));
        regions.forEach((r) -> r.getLoot().getTables().forEach((i) -> loot.add(dm.getLootLoader().load(i))));
        regions.forEach((r) -> r.getEntitySpawners().forEach((sp) -> spawners.add(dm.getSpawnerLoader().load(sp))));
        dimension.getEntitySpawners().forEach((sp) -> spawners.add(dm.getSpawnerLoader().load(sp)));
        biomes.forEach((i) -> i.getGenerators().forEach((j) -> generators.add(j.getCachedGenerator(() -> dm))));
        biomes.forEach((r) -> r.getLoot().getTables().forEach((i) -> loot.add(dm.getLootLoader().load(i))));
        biomes.forEach((r) -> r.getEntitySpawners().forEach((sp) -> spawners.add(dm.getSpawnerLoader().load(sp))));
        spawners.forEach((i) -> i.getSpawns().forEach((j) -> entities.add(dm.getEntityLoader().load(j.getEntity()))));
        KMap<String, String> renameObjects = new KMap<>();
        String a;
        StringBuilder b = new StringBuilder();
        StringBuilder c = new StringBuilder();
        sender.sendMessage("Serializing Objects");

        for (KrudWorldBiome i : biomes) {
            for (KrudWorldObjectPlacement j : i.getObjects()) {
                b.append(j.hashCode());
                KList<String> newNames = new KList<>();

                for (String k : j.getPlace()) {
                    if (renameObjects.containsKey(k)) {
                        newNames.add(renameObjects.get(k));
                        continue;
                    }

                    String name = !obfuscate ? k : UUID.randomUUID().toString().replaceAll("-", "");
                    b.append(name);
                    newNames.add(name);
                    renameObjects.put(k, name);
                }

                j.setPlace(newNames);
            }
        }

        KMap<String, KList<String>> lookupObjects = renameObjects.flip();
        StringBuilder gb = new StringBuilder();
        ChronoLatch cl = new ChronoLatch(1000);
        O<Integer> ggg = new O<>();
        ggg.set(0);
        biomes.forEach((i) -> i.getObjects().forEach((j) -> j.getPlace().forEach((k) ->
        {
            try {
                File f = dm.getObjectLoader().findFile(lookupObjects.get(k).get(0));
                IO.copyFile(f, new File(folder, "objects/" + k + ".iob"));
                gb.append(IO.hash(f));
                ggg.set(ggg.get() + 1);

                if (cl.flip()) {
                    int g = ggg.get();
                    ggg.set(0);
                    sender.sendMessage("Wrote another " + g + " Objects");
                }
            } catch (Throwable e) {
                KrudWorld.reportError(e);
            }
        })));

        b.append(IO.hash(gb.toString()));
        c.append(IO.hash(b.toString()));
        b = new StringBuilder();

        KrudWorld.info("Writing Dimensional Scaffold");

        try {
            a = new JSONObject(new Gson().toJson(dimension)).toString(minify ? 0 : 4);
            IO.writeAll(new File(folder, "dimensions/" + dimension.getLoadKey() + ".json"), a);
            b.append(IO.hash(a));

            for (KrudWorldGenerator i : generators) {
                a = new JSONObject(new Gson().toJson(i)).toString(minify ? 0 : 4);
                IO.writeAll(new File(folder, "generators/" + i.getLoadKey() + ".json"), a);
                b.append(IO.hash(a));
            }

            c.append(IO.hash(b.toString()));
            b = new StringBuilder();

            for (KrudWorldRegion i : regions) {
                a = new JSONObject(new Gson().toJson(i)).toString(minify ? 0 : 4);
                IO.writeAll(new File(folder, "regions/" + i.getLoadKey() + ".json"), a);
                b.append(IO.hash(a));
            }

            for (KrudWorldBlockData i : blocks) {
                a = new JSONObject(new Gson().toJson(i)).toString(minify ? 0 : 4);
                IO.writeAll(new File(folder, "blocks/" + i.getLoadKey() + ".json"), a);
                b.append(IO.hash(a));
            }

            for (KrudWorldBiome i : biomes) {
                a = new JSONObject(new Gson().toJson(i)).toString(minify ? 0 : 4);
                IO.writeAll(new File(folder, "biomes/" + i.getLoadKey() + ".json"), a);
                b.append(IO.hash(a));
            }

            for (KrudWorldEntity i : entities) {
                a = new JSONObject(new Gson().toJson(i)).toString(minify ? 0 : 4);
                IO.writeAll(new File(folder, "entities/" + i.getLoadKey() + ".json"), a);
                b.append(IO.hash(a));
            }

            for (KrudWorldLootTable i : loot) {
                a = new JSONObject(new Gson().toJson(i)).toString(minify ? 0 : 4);
                IO.writeAll(new File(folder, "loot/" + i.getLoadKey() + ".json"), a);
                b.append(IO.hash(a));
            }

            c.append(IO.hash(b.toString()));
            String finalHash = IO.hash(c.toString());
            JSONObject meta = new JSONObject();
            meta.put("hash", finalHash);
            meta.put("time", M.ms());
            meta.put("version", dimension.getVersion());
            IO.writeAll(new File(folder, "package.json"), meta.toString(minify ? 0 : 4));
            File p = new File(KrudWorld.instance.getDataFolder(), "exports/" + dimension.getLoadKey() + ".iris");
            KrudWorld.info("Compressing Package");
            ZipUtil.pack(folder, p, 9);
            IO.delete(folder);

            sender.sendMessage("Package Compiled!");
            return p;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }
        sender.sendMessage("Failed!");
        return null;
    }

    public void compile(VolmitSender sender) {
        KrudWorldData data = KrudWorldData.get(getPath());
        KList<Job> jobs = new KList<>();
        KList<File> files = new KList<>();
        KList<File> objects = new KList<>();
        files(getPath(), files);
        filesObjects(getPath(), objects);

        jobs.add(new ParallelQueueJob<File>() {
            @Override
            public void execute(File f) {
                try {
                    KrudWorldObject o = new KrudWorldObject(0, 0, 0);
                    o.read(f);

                    if (o.getBlocks().isEmpty()) {
                        sender.sendMessageRaw("<hover:show_text:'Error:\n" +
                                "<yellow>" + f.getPath() +
                                "'><red>- IOB " + f.getName() + " has 0 blocks!");
                    }

                    if (o.getW() == 0 || o.getH() == 0 || o.getD() == 0) {
                        sender.sendMessageRaw("<hover:show_text:'Error:\n" +
                                "<yellow>" + f.getPath() + "\n<red>The width height or depth has a zero in it (bad format)" +
                                "'><red>- IOB " + f.getName() + " is not 3D!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getName() {
                return "IOB";
            }
        }.queue(objects));

        jobs.add(new ParallelQueueJob<File>() {
            @Override
            public void execute(File f) {
                try {
                    JSONObject p = new JSONObject(IO.readAll(f));
                    fixBlocks(p);
                    scanForErrors(data, f, p, sender);
                    IO.writeAll(f, p.toString(4));

                } catch (Throwable e) {
                    sender.sendMessageRaw("<hover:show_text:'Error:\n" +
                            "<yellow>" + f.getPath() +
                            "\n<red>" + e.getMessage() +
                            "'><red>- JSON Error " + f.getName());
                }
            }

            @Override
            public String getName() {
                return "JSON";
            }
        }.queue(files));

        new JobCollection("Compile", jobs).execute(sender);
    }

    private void scanForErrors(KrudWorldData data, File f, JSONObject p, VolmitSender sender) {
        String key = data.toLoadKey(f);
        ResourceLoader<?> loader = data.getTypedLoaderFor(f);

        if (loader == null) {
            sender.sendMessageBasic("Can't find loader for " + f.getPath());
            return;
        }

        KrudWorldRegistrant load = loader.load(key);
        compare(load.getClass(), p, sender, new KList<>());
        load.scanForErrors(p, sender);
    }

    public void compare(Class<?> c, JSONObject j, VolmitSender sender, KList<String> path) {
        try {
            Object o = c.getClass().getConstructor().newInstance();
        } catch (Throwable e) {

        }
    }

    public void files(File clean, KList<File> files) {
        if (clean.isDirectory()) {
            for (File i : clean.listFiles()) {
                files(i, files);
            }
        } else if (clean.getName().endsWith(".json")) {
            try {
                files.add(clean);
            } catch (Throwable e) {
                KrudWorld.reportError(e);
            }
        }
    }

    public void filesObjects(File clean, KList<File> files) {
        if (clean.isDirectory()) {
            for (File i : clean.listFiles()) {
                filesObjects(i, files);
            }
        } else if (clean.getName().endsWith(".iob")) {
            try {
                files.add(clean);
            } catch (Throwable e) {
                KrudWorld.reportError(e);
            }
        }
    }

    private void fixBlocks(JSONObject obj) {
        for (String i : obj.keySet()) {
            Object o = obj.get(i);

            if (i.equals("block") && o instanceof String && !o.toString().trim().isEmpty() && !o.toString().contains(":")) {
                obj.put(i, "minecraft:" + o);
            }

            if (o instanceof JSONObject) {
                fixBlocks((JSONObject) o);
            } else if (o instanceof JSONArray) {
                fixBlocks((JSONArray) o);
            }
        }
    }

    private void fixBlocks(JSONArray obj) {
        for (int i = 0; i < obj.length(); i++) {
            Object o = obj.get(i);

            if (o instanceof JSONObject) {
                fixBlocks((JSONObject) o);
            } else if (o instanceof JSONArray) {
                fixBlocks((JSONArray) o);
            }
        }
    }
}
