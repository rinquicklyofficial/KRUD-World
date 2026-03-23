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
import dev.krud.world.engine.object.KrudWorldScript;
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.scheduling.PrecisionStopwatch;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ScriptResourceLoader extends ResourceLoader<KrudWorldScript> {
    public ScriptResourceLoader(File root, KrudWorldData idm, String folderName, String resourceTypeName) {
        super(root, idm, folderName, resourceTypeName, KrudWorldScript.class);
        loadCache = new KCache<>(this::loadRaw, KrudWorldSettings.get().getPerformance().getScriptLoaderCacheSize());
    }

    public boolean supportsSchemas() {
        return false;
    }

    public long getSize() {
        return loadCache.getSize();
    }

    protected KrudWorldScript loadFile(File j, String name) {
        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            KrudWorldScript t = new KrudWorldScript(IO.readAll(j));
            t.setLoadKey(name);
            t.setLoader(manager);
            t.setLoadFile(j);
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
        Set<String> keys = new HashSet<>();

        for (File i : getFolders()) {
            if (i.isDirectory()) {
                keys.addAll(getKeysInDirectory(i));
            }
        }

        possibleKeys = keys.toArray(new String[0]);
        return possibleKeys;
    }

    private Set<String> getKeysInDirectory(File directory) {
        Set<String> keys = new HashSet<>();
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".kts")) {
                keys.add(file.getName().replaceAll("\\Q.kts\\E", ""));
            } else if (file.isDirectory()) {
                keys.addAll(getKeysInDirectory(file));
            }
        }
        return keys;
    }

//    public String[] getPossibleKeys() {
//        if (possibleKeys != null) {
//            return possibleKeys;
//        }
//
//        KrudWorld.debug("Building " + resourceTypeName + " Possibility Lists");
//        KSet<String> m = new KSet<>();
//
//        for (File i : getFolders()) {
//            for (File j : i.listFiles()) {
//                if (j.isFile() && j.getName().endsWith(".js")) {
//                    m.add(j.getName().replaceAll("\\Q.js\\E", ""));
//                } else if (j.isDirectory()) {
//                    for (File k : j.listFiles()) {
//                        if (k.isFile() && k.getName().endsWith(".js")) {
//                            m.add(j.getName() + "/" + k.getName().replaceAll("\\Q.js\\E", ""));
//                        } else if (k.isDirectory()) {
//                            for (File l : k.listFiles()) {
//                                if (l.isFile() && l.getName().endsWith(".js")) {
//                                    m.add(j.getName() + "/" + k.getName() + "/" + l.getName().replaceAll("\\Q.js\\E", ""));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        KList<String> v = new KList<>(m);
//        possibleKeys = v.toArray(new String[0]);
//        return possibleKeys;
//    }

    public File findFile(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".kts") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return j;
                }
            }

            File file = new File(i, name + ".kts");

            if (file.exists()) {
                return file;
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    private KrudWorldScript loadRaw(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".kts") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return loadFile(j, name);
                }
            }

            File file = new File(i, name + ".kts");

            if (file.exists()) {
                return loadFile(file, name);
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    public KrudWorldScript load(String name, boolean warn) {
        return loadCache.get(name);
    }
}
