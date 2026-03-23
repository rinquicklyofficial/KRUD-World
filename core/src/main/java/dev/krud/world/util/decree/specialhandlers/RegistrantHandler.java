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

package dev.krud.world.util.decree.specialhandlers;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.DecreeParameterHandler;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class RegistrantHandler<T extends KrudWorldRegistrant> implements DecreeParameterHandler<T> {
    private final Class<T> type;
    private final String name;
    private final boolean nullable;

    public RegistrantHandler(Class<T> type, boolean nullable) {
        this.type = type;
        this.name = type.getSimpleName().replaceFirst("KrudWorld", "");
        this.nullable = nullable;
    }

    @Override
    public KList<T> getPossibilities() {
        KList<T> p = new KList<>();
        Set<String> known = new HashSet<>();
        KrudWorldData data = data();
        if (data != null) {
            for (T j : data.getLoader(type).loadAll(data.getLoader(type).getPossibleKeys())) {
                known.add(j.getLoadKey());
                p.add(j);
            }
        }

        //noinspection ConstantConditions
        for (File i : KrudWorld.instance.getDataFolder("packs").listFiles()) {
            if (i.isDirectory()) {
                data = KrudWorldData.get(i);
                for (T j : data.getLoader(type).loadAll(data.getLoader(type).getPossibleKeys())) {
                    if (known.add(j.getLoadKey()))
                        p.add(j);
                }
            }
        }

        return p;
    }

    @Override
    public String toString(T t) {
        return t != null ? t.getLoadKey() : "null";
    }

    @Override
    public T parse(String in, boolean force) throws DecreeParsingException {
        if (in.equals("null") && nullable) {
            return null;
        }
        KList<T> options = getPossibilities(in);
        if (options.isEmpty()) {
            throw new DecreeParsingException("Unable to find " + name + " \"" + in + "\"");
        }

        return options.stream()
                .filter((i) -> toString(i).equalsIgnoreCase(in))
                .findFirst()
                .orElseThrow(() -> new DecreeParsingException("Unable to filter which " + name + " \"" + in + "\""));
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(this.type);
    }
}
