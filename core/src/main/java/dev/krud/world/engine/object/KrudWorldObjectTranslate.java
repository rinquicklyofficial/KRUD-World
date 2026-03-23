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

import dev.krud.world.engine.object.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.util.BlockVector;

@Snippet("object-translator")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Translate objects")
@Data
public class KrudWorldObjectTranslate {
    @MinNumber(-128) // TODO: WARNING HEIGHT
    @MaxNumber(128) // TODO: WARNING HEIGHT
    @Desc("The x shift in blocks")
    private int x = 0;

    @Required
    @MinNumber(-128) // TODO: WARNING HEIGHT
    @MaxNumber(128) // TODO: WARNING HEIGHT
    @Desc("The y shift in blocks")
    private int y = 0;

    @MinNumber(-128) // TODO: WARNING HEIGHT
    @MaxNumber(128) // TODO: WARNING HEIGHT
    @Desc("Adds an additional amount of height randomly (translateY + rand(0 - yRandom))")
    private int yRandom = 0;

    @MinNumber(-128) // TODO: WARNING HEIGHT
    @MaxNumber(128) // TODO: WARNING HEIGHT
    @Desc("The z shift in blocks")
    private int z = 0;

    public boolean canTranslate() {
        return x != 0 || y != 0 || z != 0;
    }

    public BlockVector translate(BlockVector i) {
        if (canTranslate()) {
            return (BlockVector) i.clone().add(new BlockVector(x, y, z));
        }

        return i;
    }

    public BlockVector translate(BlockVector clone, KrudWorldObjectRotation rotation, int sx, int sy, int sz) {
        if (canTranslate()) {
            return (BlockVector) clone.clone().add(rotation.rotate(new BlockVector(x, y, z), sx, sy, sz));
        }

        return clone;
    }
}
