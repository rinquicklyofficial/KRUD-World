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

import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.TreeType;

@Snippet("tree")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Desc("Tree replace options for this object placer")
@Data
public class KrudWorldTree {
    @Required
    @Desc("The types of trees overwritten by this object")
    @ArrayType(min = 1, type = TreeType.class)
    private KList<TreeType> treeTypes;

    @Desc("If enabled, overrides any TreeType")
    private boolean anyTree = false;

    @Required
    @Desc("The size of the square of saplings this applies to (2 means a 2 * 2 sapling area)")
    @ArrayType(min = 1, type = KrudWorldTreeSize.class)
    private KList<KrudWorldTreeSize> sizes = new KList<>();

    @Desc("If enabled, overrides trees of any size")
    private boolean anySize;

    public boolean matches(KrudWorldTreeSize size, TreeType type) {
        if (!matchesSize(size)) {
            return false;
        }

        return matchesType(type);
    }

    private boolean matchesSize(KrudWorldTreeSize size) {
        for (KrudWorldTreeSize i : getSizes()) {
            if ((i.getDepth() == size.getDepth() && i.getWidth() == size.getWidth()) || (i.getDepth() == size.getWidth() && i.getWidth() == size.getDepth())) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesType(TreeType type) {
        return getTreeTypes().contains(type);
    }
}