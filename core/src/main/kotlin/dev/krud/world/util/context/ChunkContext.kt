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

package dev.krud.world.util.context

import dev.krud.world.engine.KrudWorldComplex
import dev.krud.world.engine.`object`.KrudWorldBiome
import dev.krud.world.engine.`object`.KrudWorldRegion
import dev.krud.world.util.parallel.MultiBurst
import kotlinx.coroutines.*
import org.bukkit.block.data.BlockData

class ChunkContext @JvmOverloads constructor(
    val x: Int,
    val z: Int,
    c: KrudWorldComplex,
    cache: Boolean = true,
) {
    val height: ChunkedDataCache<Double> = ChunkedDataCache(c.heightStream, x, z, cache)
    val biome: ChunkedDataCache<KrudWorldBiome> = ChunkedDataCache(c.trueBiomeStream, x, z, cache)
    val cave: ChunkedDataCache<KrudWorldBiome> = ChunkedDataCache(c.caveBiomeStream, x, z, cache)
    val rock: ChunkedDataCache<BlockData> = ChunkedDataCache(c.rockStream, x, z, cache)
    val fluid: ChunkedDataCache<BlockData> = ChunkedDataCache(c.fluidStream, x, z, cache)
    val region: ChunkedDataCache<KrudWorldRegion> = ChunkedDataCache(c.regionStream, x, z, cache)

    init {
        if (cache) runBlocking {
            val dispatcher = MultiBurst.burst.dispatcher

            launch { height.fill(dispatcher) }
            launch { biome.fill(dispatcher) }
            launch { cave.fill(dispatcher) }
            launch { rock.fill(dispatcher) }
            launch { fluid.fill(dispatcher) }
            launch { region.fill(dispatcher) }
        }
    }
}
