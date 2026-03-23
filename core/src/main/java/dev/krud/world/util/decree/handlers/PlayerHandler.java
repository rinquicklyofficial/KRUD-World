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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PlayerHandler implements DecreeParameterHandler<Player> {
    @Override
    public KList<Player> getPossibilities() {
        return new KList<>(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    @Override
    public String toString(Player player) {
        return player.getName();
    }

    @Override
    public Player parse(String in, boolean force) throws DecreeParsingException {
        KList<Player> options = getPossibilities(in);

        if (options.isEmpty()) {
            throw new DecreeParsingException("Unable to find Player \"" + in + "\"");
        }
        try {
            return options.stream().filter((i) -> toString(i).equalsIgnoreCase(in)).collect(Collectors.toList()).get(0);
        } catch (Throwable e) {
            throw new DecreeParsingException("Unable to filter which Player \"" + in + "\"");
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Player.class);
    }

    @Override
    public String getRandomDefault() {
        return "playername";
    }
}
