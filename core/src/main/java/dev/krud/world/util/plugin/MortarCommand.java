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

package dev.krud.world.util.plugin;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.format.C;
import org.bukkit.Sound;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

/**
 * Represents a pawn command
 *
 * @author cyberpwn
 */
public abstract class MortarCommand implements ICommand {
    private final KList<MortarCommand> children;
    private final KList<String> nodes;
    private final KList<String> requiredPermissions;
    private final String node;
    private String category;
    private String description;

    /**
     * Override this with a super constructor as most commands shouldn't change
     * these parameters
     *
     * @param node  the node (primary node) i.e. volume
     * @param nodes the aliases. i.e. v, vol, bile
     */
    public MortarCommand(String node, String... nodes) {
        category = "";
        this.node = node;
        this.nodes = new KList<>(nodes);
        requiredPermissions = new KList<>();
        children = buildChildren();
        description = "No Description";
    }

    @Override
    public KList<String> handleTab(VolmitSender sender, String[] args) {
        KList<String> v = new KList<>();
        if (args.length == 0) {
            for (MortarCommand i : getChildren()) {
                v.add(i.getNode());
            }
        }

        addTabOptions(sender, args, v);

        if (v.isEmpty()) {
            return null;
        }

        if (sender.isPlayer() && KrudWorldSettings.get().getGeneral().isCommandSounds()) {
            sender.playSound(Sound.ENTITY_ITEM_FRAME_ROTATE_ITEM, 0.25f, 1.7f);
        }

        return v;
    }

    public abstract void addTabOptions(VolmitSender sender, String[] args, KList<String> list);

    public void printHelp(VolmitSender sender) {
        boolean b = false;

        for (MortarCommand i : getChildren()) {
            for (String j : i.getRequiredPermissions()) {
                if (!sender.hasPermission(j)) {
                }
            }

            b = true;

            sender.sendMessage("" + C.GREEN + i.getNode() + " " + "<font:minecraft:uniform>" + (getArgsUsage().trim().isEmpty() ? "" : (C.WHITE + i.getArgsUsage())) + C.GRAY + " - " + i.getDescription());
        }

        if (!b) {
            sender.sendMessage("There are either no sub-commands or you do not have permission to use them.");
        }

        if (sender.isPlayer() && KrudWorldSettings.get().getGeneral().isCommandSounds()) {
            sender.playSound(Sound.ITEM_BOOK_PAGE_TURN, 0.28f, 1.4f);
            sender.playSound(Sound.ITEM_AXE_STRIP, 0.35f, 1.7f);
        }
    }

    protected abstract String getArgsUsage();

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void requiresPermission(MortarPermission node) {
        if (node == null) {
            return;
        }

        requiresPermission(node.toString());
    }

    protected void requiresPermission(String node) {
        if (node == null) {
            return;
        }

        requiredPermissions.add(node);
    }

    public void rejectAny(int past, VolmitSender sender, String[] a) {
        if (a.length > past) {
            int p = past;

            StringBuilder m = new StringBuilder();

            for (String i : a) {
                p--;
                if (p < 0) {
                    m.append(i).append(", ");
                }
            }

            if (!m.toString().trim().isEmpty()) {
                sender.sendMessage("Parameters Ignored: " + m);
            }
        }
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public KList<String> getNodes() {
        return nodes;
    }

    @Override
    public KList<String> getAllNodes() {
        return getNodes().copy().qadd(getNode());
    }

    @Override
    public void addNode(String node) {
        getNodes().add(node);
    }

    public KList<MortarCommand> getChildren() {
        return children;
    }

    private KList<MortarCommand> buildChildren() {
        KList<MortarCommand> p = new KList<>();

        for (Field i : getClass().getDeclaredFields()) {
            if (i.isAnnotationPresent(Command.class)) {
                try {
                    i.setAccessible(true);
                    MortarCommand pc = (MortarCommand) i.getType().getConstructor().newInstance();
                    Command c = i.getAnnotation(Command.class);

                    if (!c.value().trim().isEmpty()) {
                        pc.setCategory(c.value().trim());
                    } else {
                        pc.setCategory(getCategory());
                    }

                    p.add(pc);
                } catch (IllegalArgumentException | IllegalAccessException | InstantiationException |
                         InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    KrudWorld.reportError(e);
                    e.printStackTrace();
                }
            }
        }

        p.sort(Comparator.comparing(MortarCommand::getNode));

        return p;
    }

    @Override
    public KList<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
