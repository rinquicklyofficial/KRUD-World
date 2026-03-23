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
import dev.krud.world.engine.object.KrudWorldImage;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.data.KCache;
import dev.krud.world.util.scheduling.PrecisionStopwatch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

public class ImageResourceLoader extends ResourceLoader<KrudWorldImage> {
    public ImageResourceLoader(File root, KrudWorldData idm, String folderName, String resourceTypeName) {
        super(root, idm, folderName, resourceTypeName, KrudWorldImage.class);
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

    protected KrudWorldImage loadFile(File j, String name) {
        try {
            PrecisionStopwatch p = PrecisionStopwatch.start();
            BufferedImage bu = ImageIO.read(j);
            KrudWorldImage img = new KrudWorldImage(bu);
            img.setLoadFile(j);
            img.setLoader(manager);
            img.setLoadKey(name);
            logLoad(j, img);
            tlt.addAndGet(p.getMilliseconds());
            return img;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            KrudWorld.warn("Couldn't read " + resourceTypeName + " file: " + j.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    void getPNGFiles(File directory, Set<String> m) {
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                m.add(file.getName().replaceAll("\\Q.png\\E", ""));
            } else if (file.isDirectory()) {
                getPNGFiles(file, m);
            }
        }
    }


    public String[] getPossibleKeys() {
        if (possibleKeys != null) {
            return possibleKeys;
        }

        KrudWorld.debug("Building " + resourceTypeName + " Possibility Lists");
        KSet<String> m = new KSet<>();


        for (File i : getFolders()) {
            getPNGFiles(i, m);
        }

//        for (File i : getFolders()) {
//            for (File j : i.listFiles()) {
//                if (j.isFile() && j.getName().endsWith(".png")) {
//                    m.add(j.getName().replaceAll("\\Q.png\\E", ""));
//                } else if (j.isDirectory()) {
//                    for (File k : j.listFiles()) {
//                        if (k.isFile() && k.getName().endsWith(".png")) {
//                            m.add(j.getName() + "/" + k.getName().replaceAll("\\Q.png\\E", ""));
//                        } else if (k.isDirectory()) {
//                            for (File l : k.listFiles()) {
//                                if (l.isFile() && l.getName().endsWith(".png")) {
//                                    m.add(j.getName() + "/" + k.getName() + "/" + l.getName().replaceAll("\\Q.png\\E", ""));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

        KList<String> v = new KList<>(m);
        possibleKeys = v.toArray(new String[0]);
        return possibleKeys;
    }

    public File findFile(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".png") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return j;
                }
            }

            File file = new File(i, name + ".png");

            if (file.exists()) {
                return file;
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    public KrudWorldImage load(String name) {
        return load(name, true);
    }

    private KrudWorldImage loadRaw(String name) {
        for (File i : getFolders(name)) {
            for (File j : i.listFiles()) {
                if (j.isFile() && j.getName().endsWith(".png") && j.getName().split("\\Q.\\E")[0].equals(name)) {
                    return loadFile(j, name);
                }
            }

            File file = new File(i, name + ".png");

            if (file.exists()) {
                return loadFile(file, name);
            }
        }

        KrudWorld.warn("Couldn't find " + resourceTypeName + ": " + name);

        return null;
    }

    public KrudWorldImage load(String name, boolean warn) {
        return loadCache.get(name);
    }
}
