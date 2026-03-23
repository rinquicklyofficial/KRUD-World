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

package dev.krud.world.util.data.registry;

import org.bukkit.Particle;

import static dev.krud.world.util.data.registry.RegistryUtil.find;

public class Particles {
    public static final Particle CRIT_MAGIC = find(Particle.class, "crit_magic", "crit");
    public static final Particle REDSTONE = find(Particle.class,  "redstone", "dust");
    public static final Particle ITEM = find(Particle.class,  "item_crack", "item");
}
