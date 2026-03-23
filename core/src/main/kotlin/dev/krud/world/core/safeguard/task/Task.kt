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

package dev.krud.world.core.safeguard.task

import dev.krud.world.core.safeguard.Mode
import dev.krud.world.util.format.Form
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

abstract class Task(
    val id: String,
    val name: String = Form.capitalizeWords(id.replace(" ", "_").lowercase()),
) {

    abstract fun run(): ValueWithDiagnostics<Mode>

    companion object {
        fun of(id: String, name: String = id, action: () -> ValueWithDiagnostics<Mode>) = object : Task(id, name) {
            override fun run() = action()
        }

        fun of(id: String, action: () -> ValueWithDiagnostics<Mode>) = object : Task(id) {
            override fun run() = action()
        }

        fun task(action: () -> ValueWithDiagnostics<Mode>) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Task>> { _, _ ->
            ReadOnlyProperty { _, property -> of(property.name, action) }
        }
    }
}