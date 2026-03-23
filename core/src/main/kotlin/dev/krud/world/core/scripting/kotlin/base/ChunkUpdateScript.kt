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

import dev.krud.world.core.scripting.func.UpdateExecutor
import dev.krud.world.util.mantle.MantleChunk
import org.bukkit.Chunk
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.providedProperties

@KotlinScript(fileExtension = "update.kts", compilationConfiguration = ChunkUpdateScriptDefinition::class)
abstract class ChunkUpdateScript

object ChunkUpdateScriptDefinition : ScriptCompilationConfiguration(listOf(EngineScriptDefinition), {
    providedProperties(
        "mantleChunk" to MantleChunk::class,
        "chunk" to Chunk::class,
        "executor" to UpdateExecutor::class
    )
}) {
    private fun readResolve(): Any = MobSpawningScriptDefinition
}