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

package dev.krud.world.core.scripting.kotlin.environment

import dev.krud.world.core.loader.KrudWorldRegistrant
import dev.krud.world.core.scripting.environment.EngineEnvironment
import dev.krud.world.core.scripting.func.BiomeLookup
import dev.krud.world.core.scripting.func.UpdateExecutor
import dev.krud.world.core.scripting.kotlin.base.ChunkUpdateScript
import dev.krud.world.core.scripting.kotlin.base.EngineScript
import dev.krud.world.core.scripting.kotlin.base.MobSpawningScript
import dev.krud.world.core.scripting.kotlin.base.PostMobSpawningScript
import dev.krud.world.core.scripting.kotlin.base.PreprocessorScript
import dev.krud.world.core.scripting.kotlin.environment.KrudWorldSimpleExecutionEnvironment
import dev.krud.world.core.scripting.kotlin.runner.ScriptRunner
import dev.krud.world.engine.framework.Engine
import dev.krud.world.util.mantle.MantleChunk
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import java.io.File

class KrudWorldExecutionEnvironment internal constructor(
    private val engine: Engine,
    parent: ScriptRunner?,
) : KrudWorldPackExecutionEnvironment(engine.data, parent), EngineEnvironment {
    constructor(engine: Engine) : this(engine, null)
    override fun getEngine() = engine

    override fun execute(script: String) =
        execute(script, EngineScript::class.java, engine.parameters())

    override fun evaluate(script: String) =
        evaluate(script, EngineScript::class.java, engine.parameters())

    override fun spawnMob(script: String, location: Location) =
        evaluate(script, MobSpawningScript::class.java, engine.parameters("location" to location))

    override fun postSpawnMob(script: String, location: Location, mob: Entity) =
        execute(script, PostMobSpawningScript::class.java, engine.parameters("location" to location, "entity" to mob))

    override fun preprocessObject(script: String, `object`: KrudWorldRegistrant) =
        execute(script, PreprocessorScript::class.java, engine.limitedParameters("object" to `object`))

    override fun updateChunk(script: String, mantleChunk: MantleChunk, chunk: Chunk, executor: UpdateExecutor) =
        execute(script, ChunkUpdateScript::class.java, engine.parameters("mantleChunk" to mantleChunk, "chunk" to chunk, "executor" to executor))

    private fun Engine.limitedParameters(vararg values: Pair<String, Any?>): Map<String, Any?> {
        return mapOf(
            "data" to data,
            "engine" to this,
            "seed" to seedManager.seed,
            "dimension" to dimension,
            *values,
        )
    }

    private fun Engine.parameters(vararg values: Pair<String, Any?>): Map<String, Any?> {
        return limitedParameters(
            "complex" to complex,
            "biome" to BiomeLookup(::getSurfaceBiome),
            *values,
        )
    }
}