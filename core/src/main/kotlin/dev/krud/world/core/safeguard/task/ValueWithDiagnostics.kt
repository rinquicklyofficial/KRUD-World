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

import dev.krud.world.KrudWorld
import dev.krud.world.util.format.C
import java.io.ByteArrayOutputStream
import java.io.PrintStream

data class ValueWithDiagnostics<out T>(
    val value: T,
    val diagnostics: List<Diagnostic>
) {
    constructor(value: T, vararg diagnostics: Diagnostic) : this(value, diagnostics.toList())

    @JvmOverloads
    fun log(
        withException: Boolean = true,
        withStackTrace: Boolean = false
    ) {
        diagnostics.forEach { it.log(withException, withStackTrace) }
    }
}

data class Diagnostic @JvmOverloads constructor(
    val logger: Logger = Logger.ERROR,
    val message: String,
    val exception: Throwable? = null
) {

    enum class Logger(
        private val logger: (String) -> Unit
    ) {
        DEBUG(KrudWorld::debug),
        RAW(KrudWorld::msg),
        INFO(KrudWorld::info),
        WARN(KrudWorld::warn),
        ERROR(KrudWorld::error);

        fun print(message: String) = message.split('\n').forEach(logger)
        fun create(message: String, exception: Throwable? = null) = Diagnostic(this, message, exception)
    }

    @JvmOverloads
    fun log(
        withException: Boolean = true,
        withStackTrace: Boolean = false
    ) {
        logger.print(render(withException, withStackTrace))
    }

    fun render(
        withException: Boolean = true,
        withStackTrace: Boolean = false
    ): String = buildString {
        append(message)
        if (withException && exception != null) {
            append(": ")
            append(exception)
            if (withStackTrace) {
                ByteArrayOutputStream().use { os ->
                    val ps = PrintStream(os)
                    exception.printStackTrace(ps)
                    ps.flush()
                    append("\n")
                    append(os.toString())
                }
            }
        }
    }

    override fun toString(): String = C.strip(render())
}

fun <T> T.withDiagnostics(vararg diagnostics: Diagnostic) = ValueWithDiagnostics(this, diagnostics.toList())
fun <T> T.withDiagnostics(diagnostics: List<Diagnostic>) = ValueWithDiagnostics(this, diagnostics)