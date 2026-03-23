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

package dev.krud.world.core.scripting.kotlin.base

import dev.krud.world.core.loader.KrudWorldRegistrant
import dev.krud.world.engine.framework.Engine
import dev.krud.world.engine.`object`.KrudWorldDimension
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.providedProperties

@KotlinScript(fileExtension = "proc.kts", compilationConfiguration = PreprocessorScriptDefinition::class)
abstract class PreprocessorScript

object PreprocessorScriptDefinition : ScriptCompilationConfiguration(listOf(DataScriptDefinition), {
    providedProperties(
        "engine" to Engine::class,
        "seed" to Long::class,
        "dimension" to KrudWorldDimension::class,
        "object" to KrudWorldRegistrant::class
    )
}) {
    private fun readResolve(): Any = PreprocessorScriptDefinition
}