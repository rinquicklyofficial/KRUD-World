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

package dev.krud.world.core.wand;

import dev.krud.world.util.data.Cuboid;
import dev.krud.world.util.math.M;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.awt.*;

import static dev.krud.world.util.data.registry.Particles.REDSTONE;

public class WandSelection {
    private final Cuboid c;
    private final Player p;
    private static final double STEP = 0.10;

    public WandSelection(Cuboid c, Player p) {
        this.c = c;
        this.p = p;
    }

    public void draw() {
        Location playerLoc = p.getLocation();
        double maxDistanceSquared = 256 * 256;
        int particleCount = 0;

        // cube!
        Location[][] edges = {
                {c.getLowerNE(), new Location(c.getWorld(), c.getUpperX() + 1, c.getLowerY(), c.getLowerZ())},
                {c.getLowerNE(), new Location(c.getWorld(), c.getLowerX(), c.getUpperY() + 1, c.getLowerZ())},
                {c.getLowerNE(), new Location(c.getWorld(), c.getLowerX(), c.getLowerY(), c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getUpperX() + 1, c.getLowerY(), c.getLowerZ()), new Location(c.getWorld(), c.getUpperX() + 1, c.getUpperY() + 1, c.getLowerZ())},
                {new Location(c.getWorld(), c.getUpperX() + 1, c.getLowerY(), c.getLowerZ()), new Location(c.getWorld(), c.getUpperX() + 1, c.getLowerY(), c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getLowerX(), c.getUpperY() + 1, c.getLowerZ()), new Location(c.getWorld(), c.getUpperX() + 1, c.getUpperY() + 1, c.getLowerZ())},
                {new Location(c.getWorld(), c.getLowerX(), c.getUpperY() + 1, c.getLowerZ()), new Location(c.getWorld(), c.getLowerX(), c.getUpperY() + 1, c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getLowerX(), c.getLowerY(), c.getUpperZ() + 1), new Location(c.getWorld(), c.getUpperX() + 1, c.getLowerY(), c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getLowerX(), c.getLowerY(), c.getUpperZ() + 1), new Location(c.getWorld(), c.getLowerX(), c.getUpperY() + 1, c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getUpperX() + 1, c.getUpperY() + 1, c.getLowerZ()), new Location(c.getWorld(), c.getUpperX() + 1, c.getUpperY() + 1, c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getLowerX(), c.getUpperY() + 1, c.getUpperZ() + 1), new Location(c.getWorld(), c.getUpperX() + 1, c.getUpperY() + 1, c.getUpperZ() + 1)},
                {new Location(c.getWorld(), c.getUpperX() + 1, c.getLowerY(), c.getUpperZ() + 1), new Location(c.getWorld(), c.getUpperX() + 1, c.getUpperY() + 1, c.getUpperZ() + 1)}
        };

        for (Location[] edge : edges) {
            Vector direction = edge[1].toVector().subtract(edge[0].toVector());
            double length = direction.length();
            direction.normalize();

            for (double d = 0; d <= length; d += STEP) {
                Location particleLoc = edge[0].clone().add(direction.clone().multiply(d));

                if (playerLoc.distanceSquared(particleLoc) > maxDistanceSquared) {
                    continue;
                }

                spawnParticle(particleLoc, playerLoc);
                particleCount++;
            }
        }
    }

    private void spawnParticle(Location particleLoc, Location playerLoc) {
        double accuracy = M.lerpInverse(0, 64 * 64, playerLoc.distanceSquared(particleLoc));
        double dist = M.lerp(0.125, 3.5, accuracy);

        if (M.r(Math.min(dist * 5, 0.9D) * 0.995)) {
            return;
        }

        float hue = (float) (0.5f + (Math.sin((particleLoc.getX() + particleLoc.getY() + particleLoc.getZ() + (p.getTicksLived() / 2f)) / 20f) / 2));
        Color color = Color.getHSBColor(hue, 1, 1);

        p.spawnParticle(REDSTONE, particleLoc,
                0, 0, 0, 0, 1,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()),
                        (float) dist * 3f));
    }
}
