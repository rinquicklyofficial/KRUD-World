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
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("tree-size")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Sapling override object picking options")
@Data
public class KrudWorldTreeSize {

    @Required
    @Desc("The width of the sapling area")
    int width = 1;

    @Required
    @Desc("The depth of the sapling area")
    int depth = 1;

    /**
     * Does the size match
     *
     * @param size the size to check match
     * @return true if it matches (fits within width and depth)
     */
    public boolean doesMatch(KrudWorldTreeSize size) {
        return (width == size.getWidth() && depth == size.getDepth()) || (depth == size.getWidth() && width == size.getDepth());
    }
}
