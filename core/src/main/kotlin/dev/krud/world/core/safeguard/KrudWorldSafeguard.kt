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

package dev.krud.world.core.safeguard

import dev.krud.world.KrudWorld
import dev.krud.world.core.KrudWorldSettings
import dev.krud.world.core.safeguard.task.Diagnostic
import dev.krud.world.core.safeguard.task.Task
import dev.krud.world.core.safeguard.task.ValueWithDiagnostics
import dev.krud.world.core.safeguard.task.tasks
import dev.krud.world.util.format.C
import dev.krud.world.util.scheduling.J
import org.bukkit.Bukkit
import java.util.*

object KrudWorldSafeguard {
    @Volatile
    private var forceShutdown = false
    private var results: Map<Task, ValueWithDiagnostics<Mode>> = emptyMap()
    private var context: Map<String, String> = emptyMap()
    private var attachment: Map<String, List<String>> = emptyMap()
    private var mode = Mode.STABLE
    private var count = 0

    @JvmStatic
    fun execute() {
        val results = LinkedHashMap<Task, ValueWithDiagnostics<Mode>>(tasks.size)
        val context = LinkedHashMap<String, String>(tasks.size)
        val attachment = LinkedHashMap<String, List<String>>(tasks.size)
        var mode = Mode.STABLE
        var count = 0
        for (task in tasks) {
            var result: ValueWithDiagnostics<Mode>
            try {
                result = task.run()
            } catch (e: Throwable) {
                KrudWorld.reportError(e)
                result = ValueWithDiagnostics(
                    Mode.WARNING,
                    Diagnostic(Diagnostic.Logger.ERROR, "Error while running task ${task.id}", e)
                )
            }
            mode = mode.highest(result.value)
            results[task] = result
            context[task.id] = result.value.id
            attachment[task.id] = result.diagnostics.flatMap { it.toString().split('\n') }
            if (result.value != Mode.STABLE) count++
        }

        this.results = Collections.unmodifiableMap(results)
        this.context = Collections.unmodifiableMap(context)
        this.attachment = Collections.unmodifiableMap(attachment)
        this.mode = mode
        this.count = count
    }

    @JvmStatic
    fun mode() = mode

    @JvmStatic
    fun asContext() = context

    @JvmStatic
    fun asAttachment() = attachment

    @JvmStatic
    fun splash() {
        KrudWorld.instance.splash()
        printReports()
        printFooter()
    }

    @JvmStatic
    fun printReports() {
        when (mode) {
            Mode.STABLE -> KrudWorld.info(C.BLUE.toString() + "0 Conflicts found")
            Mode.WARNING -> KrudWorld.warn(C.GOLD.toString() + "%s Issues found", count)
            Mode.UNSTABLE -> KrudWorld.error(C.DARK_RED.toString() + "%s Issues found", count)
        }

        results.values.forEach { it.log(withStackTrace = true) }
    }

    @JvmStatic
    fun printFooter() {
        when (mode) {
            Mode.STABLE -> KrudWorld.info(C.BLUE.toString() + "KrudWorld is running Stable")
            Mode.WARNING -> warning()
            Mode.UNSTABLE -> unstable()
        }
    }

    @JvmStatic
    fun isForceShutdown() = forceShutdown

    private fun warning() {
        KrudWorld.warn(C.GOLD.toString() + "KrudWorld is running in Warning Mode")

        KrudWorld.warn("")
        KrudWorld.warn(C.DARK_GRAY.toString() + "--==<" + C.GOLD + " IMPORTANT " + C.DARK_GRAY + ">==--")
        KrudWorld.warn(C.GOLD.toString() + "KrudWorld is running in warning mode which may cause the following issues:")
        KrudWorld.warn("- Data Loss")
        KrudWorld.warn("- Errors")
        KrudWorld.warn("- Broken worlds")
        KrudWorld.warn("- Unexpected behavior.")
        KrudWorld.warn("- And perhaps further complications.")
        KrudWorld.warn("")
    }

    private fun unstable() {
        KrudWorld.error(C.DARK_RED.toString() + "KrudWorld is running in Unstable Mode")

        KrudWorld.error("")
        KrudWorld.error(C.DARK_GRAY.toString() + "--==<" + C.RED + " IMPORTANT " + C.DARK_GRAY + ">==--")
        KrudWorld.error("KrudWorld is running in unstable mode which may cause the following issues:")
        KrudWorld.error(C.DARK_RED.toString() + "Server Issues")
        KrudWorld.error("- Server won't boot")
        KrudWorld.error("- Data Loss")
        KrudWorld.error("- Unexpected behavior.")
        KrudWorld.error("- And More...")
        KrudWorld.error(C.DARK_RED.toString() + "World Issues")
        KrudWorld.error("- Worlds can't load due to corruption.")
        KrudWorld.error("- Worlds may slowly corrupt until they can't load.")
        KrudWorld.error("- World data loss.")
        KrudWorld.error("- And More...")
        KrudWorld.error(C.DARK_RED.toString() + "ATTENTION: " + C.RED + "While running KrudWorld in unstable mode, you won't be eligible for support.")

        if (KrudWorldSettings.get().general.isDoomsdayAnnihilationSelfDestructMode) {
            KrudWorld.error(C.DARK_RED.toString() + "Boot Unstable is set to true, continuing with the startup process in 10 seconds.")
            J.sleep(10000L)
        } else {
            KrudWorld.error(C.DARK_RED.toString() + "Go to plugins/iris/settings.json and set DoomsdayAnnihilationSelfDestructMode to true if you wish to proceed.")
            KrudWorld.error(C.DARK_RED.toString() + "The server will shutdown in 10 seconds.")
            J.sleep(10000L)
            KrudWorld.error(C.DARK_RED.toString() + "Shutting down server.")
            forceShutdown = true
            try {
                Bukkit.getPluginManager().disablePlugins()
            } finally {
                Runtime.getRuntime().halt(42)
            }
        }
        KrudWorld.info("")
    }
}