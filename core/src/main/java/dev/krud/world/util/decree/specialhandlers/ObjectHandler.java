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
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.DecreeParameterHandler;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;

import java.io.File;
import java.util.stream.Collectors;

public class ObjectHandler implements DecreeParameterHandler<String> {
    @Override
    public KList<String> getPossibilities() {
        KList<String> p = new KList<>();
        KrudWorldData data = data();
        if (data != null) {
            return new KList<>(data.getObjectLoader().getPossibleKeys());
        }

        //noinspection ConstantConditions
        for (File i : KrudWorld.instance.getDataFolder("packs").listFiles()) {
            if (i.isDirectory()) {
                data = KrudWorldData.get(i);
                p.add(data.getObjectLoader().getPossibleKeys());
            }
        }

        return p;
    }

    @Override
    public String toString(String irisObject) {
        return irisObject;
    }

    @Override
    public String parse(String in, boolean force) throws DecreeParsingException {
        KList<String> options = getPossibilities(in);

        if (options.isEmpty()) {
            throw new DecreeParsingException("Unable to find Object \"" + in + "\"");
        }
        try {
            return options.stream().filter((i) -> toString(i).equalsIgnoreCase(in)).collect(Collectors.toList()).get(0);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to filter which Object \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(String.class);
    }

    @Override
    public String getRandomDefault() {
        String f = getPossibilities().getRandom();

        return f == null ? "object" : f;
    }
}
