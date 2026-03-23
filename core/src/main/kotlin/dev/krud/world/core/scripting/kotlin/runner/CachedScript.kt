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

package dev.krud.world.core.scripting.kotlin.runner

import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.createEvaluationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

data class CachedScript(
    private val base: CompiledScript,
    private val host: BasicJvmScriptingHost,
    private val hostConfig: ScriptingHostConfiguration
) : Script, CompiledScript {
    private val scripts = base.otherScripts.map { CachedScript(it, host, hostConfig) }
    private val evalConfig = createEvaluationConfiguration()
    private val lock = ReentrantLock()

    @Volatile
    private var value: ResultWithDiagnostics<KClass<*>>? = null

    override val otherScripts: List<CompiledScript>
        get() = scripts

    override val sourceLocationId: String?
        get() = base.sourceLocationId

    override val compilationConfiguration: ScriptCompilationConfiguration
        get() = base.compilationConfiguration

    override val resultField: Pair<String, KotlinType>?
        get() = base.resultField


    override suspend fun getClass(scriptEvaluationConfiguration: ScriptEvaluationConfiguration?) = value ?: run {
        lock.lock()
        try {
            value ?: base.getClass(scriptEvaluationConfiguration).also { value = it }
        } finally {
            lock.unlock()
        }
    }

    override fun evaluate(properties: Map<String, Any?>?) = host.runInCoroutineContext {
        host.evaluator(this, createEvaluationConfiguration(properties))
    }

    private fun createEvaluationConfiguration(properties: Map<String, Any?>?): ScriptEvaluationConfiguration {
        if (properties == null || properties.isEmpty())
            return evalConfig

        return evalConfig.with {
            providedProperties(properties)
        }
    }

    private fun createEvaluationConfiguration(): ScriptEvaluationConfiguration {
        val type = compilationConfiguration[ScriptCompilationConfiguration.baseClass]?.fromClass!!
        return createEvaluationConfigurationFromTemplate(
            KotlinType(type),
            hostConfig,
            type)
    }
}
