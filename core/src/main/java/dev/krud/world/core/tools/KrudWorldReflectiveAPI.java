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

package dev.krud.world.core.tools;

import dev.krud.world.util.data.B;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

/**
 * This class is used by an external KrudWorldLib for other plugins to interact with KrudWorld. Do not change
 * existing methods or their parameters as it will break the library that uses these methods
 * feel free to add more methods so long as you also add the reflective methods to the library
 */
public class KrudWorldReflectiveAPI {
    public static boolean isKrudWorldWorld(World world) {
        return KrudWorldToolbelt.isKrudWorldWorld(world);
    }

    public static boolean isKrudWorldStudioWorld(World world) {
        return KrudWorldToolbelt.isKrudWorldStudioWorld(world);
    }

    public static void registerCustomBlockData(String namespace, String key, BlockData blockData) {
        B.registerCustomBlockData(namespace, key, blockData);
    }

    public static void retainMantleData(String classname) {
        KrudWorldToolbelt.retainMantleDataForSlice(classname);
    }

    public static void setMantleData(World world, int x, int y, int z, Object data) {
        KrudWorldToolbelt.access(world).getEngine().getMantle().getMantle().set(x, y, z, data);
    }

    public static void deleteMantleData(World world, int x, int y, int z, Class c) {
        KrudWorldToolbelt.access(world).getEngine().getMantle().getMantle().remove(x, y, z, c);
    }

    public static Object getMantleData(World world, int x, int y, int z, Class c) {
        return KrudWorldToolbelt.access(world).getEngine().getMantle().getMantle().get(x, y, z, c);
    }
}
