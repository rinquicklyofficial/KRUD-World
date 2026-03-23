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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.engine.object.KrudWorldObject;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.scheduling.PrecisionStopwatch;

import java.io.File;

public class ObjectResourceLoader extends ResourceLoader<KrudWorldObject> {
    public ObjectResourceLoader(File root, KrudWorldData idm, String folderName, String resourceTypeName) {
        super(root, idm, folderName, resourceTypeName, KrudWorldObject.class);
        loadCache = new KCache<>(this::loadRaw, KrudWorldSettings.get().getPerformance().getObjectLoaderCacheSize());
    }

    public boolean supportsSchemas() {
        return false;
    }

    public long getSize() {
        return loadCache.getSize();
    }

    public long getTotalStorage() {
        return getSize();
    }

    protected KrudWorldObject loadFile(File j, String name) {
        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            KrudWorldObject t = new KrudWorldObject(0, 0, 0);
            t.setLoadKey(name);
            t.setLoader(manager);
            t.setLoadFile(j);
            t.read(j);
            logLoad(j, t);
            tlt.addAndGet(p.getMilliseconds());
            return t;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            KrudWorld.warn("Couldn't read " + resourceTypeName + " file: " + j.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    public String[] getPossibleKeys() {
        if (possibleKeys != null) {
            return possibleKeys;
        }
        KrudWorld.debug("Building " + resourceTypeName + " Possibility Lists");
        KSet<String> m = new KSet<>();
        for (File i : getFolders()) {
            m.addAll(getFiles(i, ".iob", true));
        }
        possibleKeys = m.toArray(new String[0]);
        return possibleKeys;
    }

    private KList<String> getFiles(File dir, String ext, boolean skipDirName) {
        KList<String> paths = new KList<>();
        String name = skipDirName ? "" : dir.getName() + "/";
        for (File f : dir.listFiles()) {
            if (f.isFile() && f.getName().endsWith(ext)) {
                paths.add(name + f.getName().replaceAll("\\Q" + ext + "\\E", ""));
            } else if (f.isDirectory()) {
                getFiles(f, ext, false).forEach(e -> paths.add(name + e));
            }
        }
        return paths;
    }

    public File findFile(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".iob") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return j;
                }
            }

            File file = new File(i, name + ".iob");

            if (file.exists()) {
                return file;
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    public KrudWorldObject load(String name) {
        return load(name, true);
    }

    private KrudWorldObject loadRaw(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".iob") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return loadFile(j, name);
                }
            }

            File file = new File(i, name + ".iob");

            if (file.exists()) {
                return loadFile(file, name);
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    public KrudWorldObject load(String name, boolean warn) {
        return loadCache.get(name);
    }
}
