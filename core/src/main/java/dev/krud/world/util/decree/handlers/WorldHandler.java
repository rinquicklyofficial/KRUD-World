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

package dev.krud.world.util.decree.handlers;

import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.DecreeParameterHandler;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.stream.Collectors;

public class WorldHandler implements DecreeParameterHandler<World> {
    @Override
    public KList<World> getPossibilities() {
        KList<World> options = new KList<>();
        for (World world : Bukkit.getWorlds()) {
            if (!world.getName().toLowerCase().startsWith("iris/")) {
                options.add(world);
            }
        }
        return options;
    }

    @Override
    public String toString(World world) {
        return world.getName();
    }

    @Override
    public World parse(String in, boolean force) throws DecreeParsingException {
        KList<World> options = getPossibilities(in);

        if (options.isEmpty()) {
            throw new DecreeParsingException("Unable to find World \"" + in + "\"");
        }
        try {
            return options.stream().filter((i) -> toString(i).equalsIgnoreCase(in)).collect(Collectors.toList()).get(0);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to filter which World \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(World.class);
    }

    @Override
    public String getRandomDefault() {
        return "world";
    }
}
