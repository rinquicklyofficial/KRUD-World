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

import dev.krud.world.util.documentation.BlockCoordinates
import dev.krud.world.util.stream.ProceduralStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext

class ChunkedDataCache<T> private constructor(
    private val x: Int,
    private val z: Int,
    private val stream: ProceduralStream<T?>,
    private val cache: Boolean
) {
    private val data = arrayOfNulls<Any>(if (cache) 256 else 0)

    @JvmOverloads
    @BlockCoordinates
    constructor(stream: ProceduralStream<T?>, x: Int, z: Int, cache: Boolean = true) : this(x, z, stream, cache)

    suspend fun fill(context: CoroutineContext = Dispatchers.Default) {
        if (!cache) return

        supervisorScope {
            for (i in 0 until 16) {
                for (j in 0 until 16) {
                    launch(context) {
                        val t = stream.get((x + i).toDouble(), (z + j).toDouble())
                        data[(j * 16) + i] = t
                    }
                }
            }
        }
    }

    @BlockCoordinates
    @Suppress("UNCHECKED_CAST")
    fun get(x: Int, z: Int): T? {
        if (!cache) {
            return stream.get((this.x + x).toDouble(), (this.z + z).toDouble())
        }

        val t = data[(z * 16) + x] as? T
        return t ?: stream.get((this.x + x).toDouble(), (this.z + z).toDouble())
    }
}
