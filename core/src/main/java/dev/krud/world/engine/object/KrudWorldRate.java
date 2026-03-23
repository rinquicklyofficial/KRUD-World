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

import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.scheduling.ChronoLatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Snippet("rate")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Desc("Represents a count of something per time duration")
public class KrudWorldRate {
    @Desc("The amount of things. Leave 0 for infinite (meaning always spawn whenever)")
    private int amount = 0;

    @Desc("The time interval. Leave blank for infinite 0 (meaning always spawn all the time)")
    private KrudWorldDuration per = new KrudWorldDuration();

    public String toString() {
        return Form.f(amount) + "/" + per;
    }

    public long getInterval() {
        long t = per.toMilliseconds() / (amount == 0 ? 1 : amount);
        return Math.abs(t <= 0 ? 1 : t);
    }

    public ChronoLatch toChronoLatch() {
        return new ChronoLatch(getInterval());
    }

    public boolean isInfinite() {
        return per.toMilliseconds() == 0;
    }
}
