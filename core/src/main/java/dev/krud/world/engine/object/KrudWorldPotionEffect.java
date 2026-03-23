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

package dev.krud.world.engine.object;

import dev.krud.world.KrudWorld;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Snippet("potion-effect")
@Desc("An iris potion effect")
@Data
public class KrudWorldPotionEffect {
    private final transient AtomicCache<PotionEffectType> pt = new AtomicCache<>();
    @Required
    @Desc("The potion effect to apply in this area")
    private String potionEffect = "";
    @Required
    @MinNumber(-1)
    @MaxNumber(1024)
    @Desc("The Potion Strength or -1 to disable")
    private int strength = -1;
    @Required
    @MinNumber(1)
    @Desc("The time the potion will last for")
    private int ticks = 200;
    @Desc("Is the effect ambient")
    private boolean ambient = false;
    @Desc("Is the effect showing particles")
    private boolean particles = true;

    public PotionEffectType getRealType() {
        return pt.aquire(() ->
        {
            PotionEffectType t = PotionEffectType.LUCK;

            if (getPotionEffect().isEmpty()) {
                return t;
            }

            try {
                for (PotionEffectType i : PotionEffectType.values()) {
                    if (i.getName().toUpperCase().replaceAll("\\Q \\E", "_").equals(getPotionEffect())) {
                        t = i;

                        return t;
                    }
                }
            } catch (Throwable e) {
                KrudWorld.reportError(e);

            }

            KrudWorld.warn("Unknown Potion Effect Type: " + getPotionEffect());

            return t;
        });
    }

    public void apply(LivingEntity p) {
        if (strength > -1) {
            if (p.hasPotionEffect(getRealType())) {
                PotionEffect e = p.getPotionEffect(getRealType());
                if (e.getAmplifier() > strength) {
                    return;
                }

                p.removePotionEffect(getRealType());
            }

            p.addPotionEffect(new PotionEffect(getRealType(), ticks, strength, ambient, particles, false));
        }
    }
}
