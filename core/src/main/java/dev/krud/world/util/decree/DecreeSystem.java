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

package dev.krud.world.util.decree;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.virtual.VirtualDecreeCommand;
import dev.krud.world.util.format.C;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DecreeSystem extends CommandExecutor, TabCompleter {
    KList<DecreeParameterHandler<?>> handlers = KrudWorld.initialize("dev.krud.world.util.decree.handlers", null).convert((i) -> (DecreeParameterHandler<?>) i);

    static KList<String> enhanceArgs(String[] args) {
        return enhanceArgs(args, true);
    }

    static KList<String> enhanceArgs(String[] args, boolean trim) {
        KList<String> a = new KList<>();

        if (args.length == 0) {
            return a;
        }

        StringBuilder flat = new StringBuilder();
        for (String i : args) {
            if (trim) {
                if (i.trim().isEmpty()) {
                    continue;
                }

                flat.append(" ").append(i.trim());
            } else {
                if (i.endsWith(" ")) {
                    flat.append(" ").append(i.trim()).append(" ");
                }
            }
        }

        flat = new StringBuilder(flat.length() > 0 ? trim ? flat.toString().trim().length() > 0 ? flat.substring(1).trim() : flat.toString().trim() : flat.substring(1) : flat);
        StringBuilder arg = new StringBuilder();
        boolean quoting = false;

        for (int x = 0; x < flat.length(); x++) {
            char i = flat.charAt(x);
            char j = x < flat.length() - 1 ? flat.charAt(x + 1) : i;
            boolean hasNext = x < flat.length();

            if (i == ' ' && !quoting) {
                if (!arg.toString().trim().isEmpty() && trim) {
                    a.add(arg.toString().trim());
                    arg = new StringBuilder();
                }
            } else if (i == '"') {
                if (!quoting && (arg.length() == 0)) {
                    quoting = true;
                } else if (quoting) {
                    quoting = false;

                    if (hasNext && j == ' ') {
                        if (!arg.toString().trim().isEmpty() && trim) {
                            a.add(arg.toString().trim());
                            arg = new StringBuilder();
                        }
                    } else if (!hasNext) {
                        if (!arg.toString().trim().isEmpty() && trim) {
                            a.add(arg.toString().trim());
                            arg = new StringBuilder();
                        }
                    }
                }
            } else {
                arg.append(i);
            }
        }

        if (!arg.toString().trim().isEmpty() && trim) {
            a.add(arg.toString().trim());
        }

        return a;
    }

    /**
     * Get the handler for the specified type
     *
     * @param type The type to handle
     * @return The corresponding {@link DecreeParameterHandler}, or null
     */
    static DecreeParameterHandler<?> getHandler(Class<?> type) {
        for (DecreeParameterHandler<?> i : handlers) {
            if (i.supports(type)) {
                return i;
            }
        }
        KrudWorld.error("Unhandled type in Decree Parameter: " + type.getName() + ". This is bad!");
        return null;
    }

    /**
     * The root class to start command searching from
     */
    VirtualDecreeCommand getRoot();

    default boolean call(VolmitSender sender, String[] args) {
        DecreeContext.touch(sender);
        try {
            return getRoot().invoke(sender, enhanceArgs(args));
        } finally {
            DecreeContext.remove();
        }
    }

    @Nullable
    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        DecreeContext.touch(new VolmitSender(sender));
        try {
            KList<String> enhanced = new KList<>(args);
            KList<String> v = getRoot().tabComplete(enhanced, enhanced.toString(" "));
            v.removeDuplicates();

            if (sender instanceof Player) {
                if (KrudWorldSettings.get().getGeneral().isCommandSounds()) {
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.25f, RNG.r.f(0.125f, 1.95f));
                }
            }

            return v;
        } finally {
            DecreeContext.remove();
        }
    }

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iris.all")) {
            sender.sendMessage("You lack the Permission 'iris.all'");
            return true;
        }

        J.aBukkit(() -> {
            var volmit = new VolmitSender(sender);
            if (!call(volmit, args)) {

                if (KrudWorldSettings.get().getGeneral().isCommandSounds()) {
                    if (sender instanceof Player) {
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 0.77f, 0.25f);
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.2f, 0.45f);
                    }
                }

                volmit.sendMessage(C.RED + "Unknown KrudWorld Command");
            } else {
                if (KrudWorldSettings.get().getGeneral().isCommandSounds()) {
                    if (sender instanceof Player) {
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 0.77f, 1.65f);
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.125f, 2.99f);
                    }
                }
            }
        });
        return true;
    }
}
