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

package dev.krud.world.util.io;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarScanner {
    private final KSet<Class<?>> classes;
    private final File jar;
    private final String superPackage;
    private final boolean report;

    /**
     * Create a scanner
     *
     * @param jar the path to the jar
     */
    public JarScanner(File jar, String superPackage, boolean report) {
        this.jar = jar;
        this.classes = new KSet<>();
        this.superPackage = superPackage;
        this.report = report;
    }

    public JarScanner(File jar, String superPackage) {
        this(jar, superPackage, true);
    }

    /**
     * Scan the jar
     *
     * @throws IOException bad things happen
     */
    public void scan() throws IOException {
        classes.clear();
        FileInputStream fin = new FileInputStream(jar);
        ZipInputStream zip = new ZipInputStream(fin);

        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                if (entry.getName().contains("$")) {
                    continue;
                }

                String c = entry.getName().replaceAll("/", ".").replace(".class", "");

                if (c.startsWith(superPackage)) {
                    try {
                        Class<?> clazz = Class.forName(c);
                        classes.add(clazz);
                    } catch (Throwable e) {
                        if (!report) continue;
                        KrudWorld.reportError(e);
                        e.printStackTrace();
                    }
                }
            }
        }

        zip.close();
    }

    public void scanAll() throws IOException {
        classes.clear();
        FileInputStream fin = new FileInputStream(jar);
        ZipInputStream zip = new ZipInputStream(fin);
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                String c = entry.getName().replaceAll("/", ".").replace(".class", "");

                if (c.startsWith(superPackage)) {
                    try {
                        Class<?> clazz = Class.forName(c);
                        classes.add(clazz);
                    } catch (Throwable e) {
                        if (!report) continue;
                        KrudWorld.reportError(e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Get the scanned clases
     *
     * @return a gset of classes
     */
    public KSet<Class<?>> getClasses() {
        return classes;
    }

    /**
     * Get the file object for the jar
     *
     * @return a file object representing the jar
     */
    public File getJar() {
        return jar;
    }
}