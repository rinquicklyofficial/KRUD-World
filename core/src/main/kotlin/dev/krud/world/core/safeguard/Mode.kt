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

import dev.krud.world.BuildConstants
import dev.krud.world.KrudWorld
import dev.krud.world.core.KrudWorldSettings
import dev.krud.world.util.format.C
import dev.krud.world.util.format.Form

enum class Mode(private val color: C) {
    STABLE(C.IRIS),
    WARNING(C.GOLD),
    UNSTABLE(C.RED);

    val id = name.lowercase()

    fun highest(m: Mode): Mode {
        return if (m.ordinal > ordinal) m else this
    }

    fun tag(subTag: String?): String {
        if (subTag == null || subTag.isBlank()) return wrap("KrudWorld") + C.GRAY + ": "
        return wrap("KrudWorld") + " " + wrap(subTag) + C.GRAY + ": "
    }

    private fun wrap(tag: String?): String {
        return C.BOLD.toString() + "" + C.DARK_GRAY + "[" + C.BOLD + color + tag + C.BOLD + C.DARK_GRAY + "]" + C.RESET
    }

    fun trySplash() {
        if (!KrudWorldSettings.get().general.isSplashLogoStartup) return
        splash()
    }

    fun splash() {
        val padd = Form.repeat(" ", 8)
        val padd2 = Form.repeat(" ", 4)

        val splash = arrayOf(
            padd + C.GRAY + "   @@@@@@@@@@@@@@" + C.DARK_GRAY + "@@@",
            padd + C.GRAY + " @@&&&&&&&&&" + C.DARK_GRAY + "&&&&&&" + color + "   .(((()))).                     ",
            padd + C.GRAY + "@@@&&&&&&&&" + C.DARK_GRAY + "&&&&&" + color + "  .((((((())))))).                  ",
            padd + C.GRAY + "@@@&&&&&" + C.DARK_GRAY + "&&&&&&&" + color + "  ((((((((()))))))))               " + C.GRAY + " @",
            padd + C.GRAY + "@@@&&&&" + C.DARK_GRAY + "@@@@@&" + color + "    ((((((((-)))))))))              " + C.GRAY + " @@",
            padd + C.GRAY + "@@@&&" + color + "            ((((((({ }))))))))           " + C.GRAY + " &&@@@",
            padd + C.GRAY + "@@" + color + "               ((((((((-)))))))))    " + C.DARK_GRAY + "&@@@@@" + C.GRAY + "&&&&@@@",
            padd + C.GRAY + "@" + color + "                ((((((((()))))))))  " + C.DARK_GRAY + "&&&&&" + C.GRAY + "&&&&&&&@@@",
            padd + C.GRAY + "" + color + "                  '((((((()))))))'  " + C.DARK_GRAY + "&&&&&" + C.GRAY + "&&&&&&&&@@@",
            padd + C.GRAY + "" + color + "                     '(((())))'   " + C.DARK_GRAY + "&&&&&&&&" + C.GRAY + "&&&&&&&@@",
            padd + C.GRAY + "                               " + C.DARK_GRAY + "@@@" + C.GRAY + "@@@@@@@@@@@@@@",
        )

        val info = arrayOf(
            "",
            "",
            "",
            "",
            "",
            padd2 + color + " KrudWorld",
            padd2 + C.GRAY + " by " + color + "Volmit Software",
            padd2 + C.GRAY + " v" + color + KrudWorld.instance.description.version,
            padd2 + C.GRAY + " c" + color + BuildConstants.COMMIT + C.GRAY + "/" + color + BuildConstants.ENVIRONMENT,
        )


        val builder = StringBuilder("\n\n")
        for (i in splash.indices) {
            builder.append(splash[i])
            if (i < info.size) {
                builder.append(info[i])
            }
            builder.append("\n")
        }

        KrudWorld.info(builder.toString())
    }
}