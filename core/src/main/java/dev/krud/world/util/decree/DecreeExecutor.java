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

package dev.krud.world.util.decree;

import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.plugin.VolmitSender;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface DecreeExecutor {
    default VolmitSender sender() {
        return DecreeContext.get();
    }

    default Player player() {
        return sender().player();
    }

    default KrudWorldData data() {
        var access = access();
        if (access != null) {
            return access.getData();
        }
        return null;
    }

    default Engine engine() {
        if (sender().isPlayer() && KrudWorldToolbelt.access(sender().player().getWorld()) != null) {
            PlatformChunkGenerator gen = KrudWorldToolbelt.access(sender().player().getWorld());
            if (gen != null) {
                return gen.getEngine();
            }
        }

        return null;
    }

    default PlatformChunkGenerator access() {
        if (sender().isPlayer()) {
            return KrudWorldToolbelt.access(world());
        }
        return null;
    }

    default World world() {
        if (sender().isPlayer()) {
            return sender().player().getWorld();
        }
        return null;
    }

    default <T> T get(T v, T ifUndefined) {
        return v == null ? ifUndefined : v;
    }
}
