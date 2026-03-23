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

import dev.krud.world.core.scripting.kotlin.runner.configure
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(fileExtension = "simple.kts", compilationConfiguration = SimpleScriptDefinition::class)
abstract class SimpleScript

object SimpleScriptDefinition : ScriptCompilationConfiguration({
    defaultImports(
        DependsOn::class.qualifiedName!!,
        Repository::class.qualifiedName!!,
        "dev.krud.world.KrudWorld.info",
        "dev.krud.world.KrudWorld.debug",
        "dev.krud.world.KrudWorld.warn",
        "dev.krud.world.KrudWorld.error"
    )

    jvm {
        dependenciesFromClassContext(KotlinScript::class, wholeClasspath = true)
        dependenciesFromClassContext(SimpleScript::class, wholeClasspath = true)
    }

    configure()
}) {
    private fun readResolve(): Any = SimpleScriptDefinition
}