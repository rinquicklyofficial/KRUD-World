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

package dev.krud.world.engine.mantle

import dev.krud.world.core.KrudWorldSettings
import dev.krud.world.core.nms.container.Pair
import dev.krud.world.engine.framework.Engine
import dev.krud.world.util.context.ChunkContext
import dev.krud.world.util.documentation.ChunkCoordinates
import dev.krud.world.util.mantle.Mantle
import dev.krud.world.util.mantle.flag.MantleFlag
import dev.krud.world.util.parallel.MultiBurst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

interface MatterGenerator {
    val engine: Engine
    val mantle: Mantle
    val radius: Int
    val realRadius: Int
    val components: List<Pair<List<MantleComponent>, Int>>

    @ChunkCoordinates
    fun generateMatter(x: Int, z: Int, multicore: Boolean, context: ChunkContext) {
        if (!engine.dimension.isUseMantle) return
        val multicore = multicore || KrudWorldSettings.get().generator.isUseMulticoreMantle

        mantle.write(engine.mantle, x, z, radius, multicore).use { writer ->
            for (pair in components) {
                runBlocking {
                    radius(x, z, pair.b) { x, z ->
                        val mc = writer.acquireChunk(x, z)
                        if (mc.isFlagged(MantleFlag.PLANNED))
                            return@radius

                        for (c in pair.a) {
                            if (mc.isFlagged(c.flag))
                                continue

                            launch(multicore) {
                                mc.raiseFlagSuspend(c.flag) {
                                    c.generateLayer(writer, x, z, context)
                                }
                            }
                        }
                    }
                }
            }

            radius(x, z, realRadius) { x, z ->
                writer.acquireChunk(x, z)
                    .flag(MantleFlag.PLANNED, true)
            }
        }
    }

    private inline fun radius(x: Int, z: Int, radius: Int, crossinline task: (Int, Int) -> Unit) {
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                task(x + i, z + j)
            }
        }
    }

    companion object {
        private val dispatcher = MultiBurst.burst.dispatcher//.limitedParallelism(128, "Mantle")
        private fun CoroutineScope.launch(multicore: Boolean, block: suspend CoroutineScope.() -> Unit) =
            launch(if (multicore) dispatcher else EmptyCoroutineContext, block = block)
    }
}