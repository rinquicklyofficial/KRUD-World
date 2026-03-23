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

import dev.krud.world.core.loader.KrudWorldData
import dev.krud.world.core.scripting.environment.EngineEnvironment
import dev.krud.world.core.scripting.environment.PackEnvironment
import dev.krud.world.core.scripting.kotlin.base.DataScript
import dev.krud.world.core.scripting.kotlin.base.NoiseScript
import dev.krud.world.core.scripting.kotlin.runner.Script
import dev.krud.world.core.scripting.kotlin.runner.ScriptRunner
import dev.krud.world.core.scripting.kotlin.runner.valueOrThrow
import dev.krud.world.engine.framework.Engine
import dev.krud.world.util.math.RNG
import kotlin.reflect.KClass

open class KrudWorldPackExecutionEnvironment internal constructor(
    private val data: KrudWorldData,
    parent: ScriptRunner?
) : KrudWorldSimpleExecutionEnvironment(data.dataFolder, parent), PackEnvironment {
    constructor(data: KrudWorldData) : this(data, null)

    override fun getData() = data

    override fun compile(script: String, type: KClass<*>): Script {
        val loaded = data.scriptLoader.load(script)
        return compileCache.get(script)
            .computeIfAbsent(type) { _ -> runner.compile(type, loaded.loadFile, loaded.source) }
            .valueOrThrow("Failed to compile script $script")
    }

    override fun execute(script: String) =
        execute(script, DataScript::class.java, data.parameters())

    override fun evaluate(script: String) =
        evaluate(script, DataScript::class.java, data.parameters())

    override fun createNoise(script: String, rng: RNG) =
        evaluate(script, NoiseScript::class.java, data.parameters("rng" to rng))

    override fun with(engine: Engine) =
        KrudWorldExecutionEnvironment(engine, runner)

    private fun KrudWorldData.parameters(vararg values: Pair<String, Any?>): Map<String, Any?> {
        return mapOf(
            "data" to this,
            *values,
        )
    }
}