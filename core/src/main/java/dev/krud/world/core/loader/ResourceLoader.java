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

package dev.krud.world.core.loader;

import com.google.common.util.concurrent.AtomicDouble;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.project.SchemaBuilder;
import dev.krud.world.core.service.PreservationSVC;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.MeteredCache;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.format.C;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.io.CustomOutputStream;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONArray;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.parallel.MultiBurst;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Data
@EqualsAndHashCode(exclude = "manager")
@ToString(exclude = "manager")
public class ResourceLoader<T extends KrudWorldRegistrant> implements MeteredCache {
    public static final AtomicDouble tlt = new AtomicDouble(0);
    private static final int CACHE_SIZE = 100000;
    protected final AtomicCache<KList<File>> folderCache;
    protected KSet<String> firstAccess;
    protected File root;
    protected String folderName;
    protected String resourceTypeName;
    protected KCache<String, T> loadCache;
    protected Class<? extends T> objectClass;
    protected String cname;
    protected String[] possibleKeys = null;
    protected KrudWorldData manager;
    protected AtomicInteger loads;
    protected ChronoLatch sec;

    public ResourceLoader(File root, KrudWorldData manager, String folderName, String resourceTypeName, Class<? extends T> objectClass) {
        this.manager = manager;
        firstAccess = new KSet<>();
        folderCache = new AtomicCache<>();
        sec = new ChronoLatch(5000);
        loads = new AtomicInteger();
        this.objectClass = objectClass;
        cname = objectClass.getCanonicalName();
        this.resourceTypeName = resourceTypeName;
        this.root = root;
        this.folderName = folderName;
        loadCache = new KCache<>(this::loadRaw, KrudWorldSettings.get().getPerformance().getResourceLoaderCacheSize());
        KrudWorld.debug("Loader<" + C.GREEN + resourceTypeName + C.LIGHT_PURPLE + "> created in " + C.RED + "IDM/" + manager.getId() + C.LIGHT_PURPLE + " on " + C.GRAY + manager.getDataFolder().getPath());
        KrudWorld.service(PreservationSVC.class).registerCache(this);
    }

    public JSONObject buildSchema() {
        KrudWorld.debug("Building Schema " + objectClass.getSimpleName() + " " + root.getPath());
        JSONObject o = new JSONObject();
        KList<String> fm = new KList<>();

        for (int g = 1; g < 8; g++) {
            fm.add("/" + folderName + Form.repeat("/*", g) + ".json");
        }

        o.put("fileMatch", new JSONArray(fm.toArray()));
        o.put("url", "./.iris/schema/" + getFolderName() + "-schema.json");
        File a = new File(getManager().getDataFolder(), ".iris/schema/" + getFolderName() + "-schema.json");
        J.attemptAsync(() -> IO.writeAll(a, new SchemaBuilder(objectClass, manager).construct().toString(4)));

        return o;
    }

    public File findFile(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".json") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return j;
                }
            }

            File file = new File(i, name + ".json");

            if (file.exists()) {
                return file;
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    public void logLoad(File path, T t) {
        loads.getAndIncrement();

        if (loads.get() == 1) {
            sec.flip();
        }

        if (sec.flip()) {
            J.a(() -> {
                KrudWorld.verbose("Loaded " + C.WHITE + loads.get() + " " + resourceTypeName + (loads.get() == 1 ? "" : "s") + C.GRAY + " (" + Form.f(getLoadCache().getSize()) + " " + resourceTypeName + (loadCache.getSize() == 1 ? "" : "s") + " Loaded)");
                loads.set(0);
            });
        }

        KrudWorld.debug("Loader<" + C.GREEN + resourceTypeName + C.LIGHT_PURPLE + "> iload " + C.YELLOW + t.getLoadKey() + C.LIGHT_PURPLE + " in " + C.GRAY + t.getLoadFile().getPath() + C.LIGHT_PURPLE + " TLT: " + C.RED + Form.duration(tlt.get(), 2));
    }

    public void failLoad(File path, Throwable e) {
        J.a(() -> KrudWorld.warn("Couldn't Load " + resourceTypeName + " file: " + path.getPath() + ": " + e.getMessage()));
    }

    private KList<File> matchAllFiles(File root, Predicate<File> f) {
        KList<File> fx = new KList<>();
        matchFiles(root, fx, f);
        return fx;
    }

    private void matchFiles(File at, KList<File> files, Predicate<File> f) {
        if (at.isDirectory()) {
            for (File i : at.listFiles()) {
                matchFiles(i, files, f);
            }
        } else {
            if (f.test(at)) {
                files.add(at);
            }
        }
    }

    public String[] getPossibleKeys() {
        if (possibleKeys != null) {
            return possibleKeys;
        }

        KList<File> files = getFolders();

        if (files == null) {
            possibleKeys = new String[0];
            return possibleKeys;
        }

        HashSet<String> m = new HashSet<>();
        for (File i : files) {
            for (File j : matchAllFiles(i, (f) -> f.getName().endsWith(".json"))) {
                m.add(i.toURI().relativize(j.toURI()).getPath().replaceAll("\\Q.json\\E", ""));
            }
        }

        KList<String> v = new KList<>(m);
        possibleKeys = v.toArray(new String[0]);
        return possibleKeys;
    }

    public long count() {
        return loadCache.getSize();
    }

    protected T loadFile(File j, String name) {
        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            T t = getManager().getGson()
                    .fromJson(preprocess(new JSONObject(IO.readAll(j))).toString(0), objectClass);
            t.setLoadKey(name);
            t.setLoadFile(j);
            t.setLoader(manager);
            getManager().preprocessObject(t);
            logLoad(j, t);
            tlt.addAndGet(p.getMilliseconds());
            return t;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            failLoad(j, e);
            return null;
        }
    }

    protected JSONObject preprocess(JSONObject j) {
        return j;
    }

    public Stream<T> streamAll() {
        return streamAll(Arrays.stream(getPossibleKeys()));
    }

    public Stream<T> streamAll(Stream<String> s) {
        return s.map(this::load);
    }

    public KList<T> loadAll(KList<String> s) {
        KList<T> m = new KList<>();

        for (String i : s) {
            T t = load(i);

            if (t != null) {
                m.add(t);
            }
        }

        return m;
    }

    public KList<T> loadAllParallel(KList<String> s) {
        KList<T> m = new KList<>();
        BurstExecutor burst = MultiBurst.ioBurst.burst(s.size());

        for (String i : s) {
            burst.queue(() -> {
                T t = load(i);
                if (t == null)
                    return;

                synchronized (m) {
                    m.add(t);
                }
            });
        }

        burst.complete();
        return m;
    }

    public KList<T> loadAll(KList<String> s, Consumer<T> postLoad) {
        KList<T> m = new KList<>();

        for (String i : s) {
            T t = load(i);

            if (t != null) {
                m.add(t);
                postLoad.accept(t);
            }
        }

        return m;
    }

    public KList<T> loadAll(String[] s) {
        KList<T> m = new KList<>();

        for (String i : s) {
            T t = load(i);

            if (t != null) {
                m.add(t);
            }
        }

        return m;
    }

    public T load(String name) {
        return load(name, true);
    }

    private T loadRaw(String name) {
        for (File i : getFolders(name)) {
            //noinspection ConstantConditions
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".json") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return loadFile(j, name);
                }
            }

            File file = new File(i, name + ".json");

            if (file.exists()) {
                return loadFile(file, name);
            }
        }

        return null;
    }

    public T load(String name, boolean warn) {
        if (name == null) {
            return null;
        }

        if (name.trim().isEmpty()) {
            return null;
        }

        var set = firstAccess;
        if (set != null) firstAccess.add(name);
        return loadCache.get(name);
    }

    public void loadFirstAccess(Engine engine) throws IOException {
        String id = "DIM" + Math.abs(engine.getSeedManager().getSeed() + engine.getDimension().getVersion() + engine.getDimension().getLoadKey().hashCode());
        File file = KrudWorld.instance.getDataFile("prefetch/" + id + "/" + Math.abs(getFolderName().hashCode()) + ".ipfch");

        if (!file.exists()) {
            return;
        }

        FileInputStream fin = new FileInputStream(file);
        GZIPInputStream gzi = new GZIPInputStream(fin);
        DataInputStream din = new DataInputStream(gzi);
        int m = din.readInt();
        KList<String> s = new KList<>();

        for (int i = 0; i < m; i++) {
            s.add(din.readUTF());
        }

        din.close();
        KrudWorld.info("Loading " + s.size() + " prefetch " + getFolderName());
        firstAccess = null;
        loadAllParallel(s);
    }

    public void saveFirstAccess(Engine engine) throws IOException {
        if (firstAccess == null) return;
        String id = "DIM" + Math.abs(engine.getSeedManager().getSeed() + engine.getDimension().getVersion() + engine.getDimension().getLoadKey().hashCode());
        File file = KrudWorld.instance.getDataFile("prefetch/" + id + "/" + Math.abs(getFolderName().hashCode()) + ".ipfch");
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        GZIPOutputStream gzo = new CustomOutputStream(fos, 9);
        DataOutputStream dos = new DataOutputStream(gzo);
        var set = firstAccess;
        firstAccess = null;
        dos.writeInt(set.size());

        for (String i : set) {
            dos.writeUTF(i);
        }

        dos.flush();
        dos.close();
    }

    public KList<File> getFolders() {
        return folderCache.aquire(() -> {
            KList<File> fc = new KList<>();

            File[] files = root.listFiles();
            if (files == null) {
                throw new IllegalStateException("Failed to list files in " + root);
            }

            for (File i : files) {
                if (i.isDirectory()) {
                    if (i.getName().equals(folderName)) {
                        fc.add(i);
                        break;
                    }
                }
            }
            return fc;
        });
    }

    public KList<File> getFolders(String rc) {
        KList<File> folders = getFolders().copy();

        if (rc.contains(":")) {
            for (File i : folders.copy()) {
                if (!rc.startsWith(i.getName() + ":")) {
                    folders.remove(i);
                }
            }
        }

        return folders;
    }

    public void clearCache() {
        possibleKeys = null;
        loadCache.invalidate();
        folderCache.reset();
    }

    public File fileFor(T b) {
        for (File i : getFolders()) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".json") && j.getName().split("\\Q.\\E")[0].equals(b.getLoadKey())) {
                    return j;
                }
            }

            File file = new File(i, b.getLoadKey() + ".json");

            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    public boolean isLoaded(String next) {
        return loadCache.contains(next);
    }

    public void clearList() {
        folderCache.reset();
        possibleKeys = null;
    }

    public KList<String> getPossibleKeys(String arg) {
        KList<String> f = new KList<>();

        for (String i : getPossibleKeys()) {
            if (i.equalsIgnoreCase(arg) || i.toLowerCase(Locale.ROOT).startsWith(arg.toLowerCase(Locale.ROOT)) || i.toLowerCase(Locale.ROOT).contains(arg.toLowerCase(Locale.ROOT)) || arg.toLowerCase(Locale.ROOT).contains(i.toLowerCase(Locale.ROOT))) {
                f.add(i);
            }
        }

        return f;
    }

    public boolean supportsSchemas() {
        return true;
    }

    public void clean() {

    }

    public long getSize() {
        return loadCache.getSize();
    }

    @Override
    public KCache<?, ?> getRawCache() {
        return loadCache;
    }

    @Override
    public long getMaxSize() {
        return loadCache.getMaxSize();
    }

    @Override
    public boolean isClosed() {
        return getManager().isClosed();
    }

    public long getTotalStorage() {
        return getSize();
    }
}
