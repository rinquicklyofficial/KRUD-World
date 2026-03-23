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

package dev.krud.world.engine.object;

import dev.krud.world.engine.object.annotations.Desc;

@Desc("Represents a location where decorations should go")
public enum KrudWorldDecorationPart {
    @Desc("The default, decorate anywhere")
    NONE,

    @Desc("Targets shore lines (typically for sugar cane)")
    SHORE_LINE,

    @Desc("Target sea surfaces (typically for lilypads)")
    SEA_SURFACE,

    @Desc("Targets the sea floor (entire placement must be bellow sea level)")
    SEA_FLOOR,

    @Desc("Decorates on cave & carving ceilings or underside of overhangs")
    CEILING,
}
