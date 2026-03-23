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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Assistive command router
 *
 * @author cyberpwn
 */
public class RouterCommand extends org.bukkit.command.Command {
    private final CommandExecutor ex;
    private String usage;

    /**
     * The router command routes commands to bukkit executors
     *
     * @param realCommand the real command
     * @param ex          the executor
     */
    public RouterCommand(ICommand realCommand, CommandExecutor ex) {
        super(realCommand.getNode().toLowerCase());
        setAliases(realCommand.getNodes());

        this.ex = ex;
    }


    @Override
    public Command setUsage(String u) {
        this.usage = u;
        return this;
    }


    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return ex.onCommand(sender, this, commandLabel, args);
    }
}
