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

package dev.krud.world.util.matter;

import lombok.Data;

import java.util.Map;

@Data
public class MatterStructurePOI {

    public static final MatterStructurePOI BURIED_TREASURE = new MatterStructurePOI("buried_treasure");

    private static final MatterStructurePOI UNKNOWN = new MatterStructurePOI("unknown");
    private static final Map<String, MatterStructurePOI> VALUES = Map.of(
            "buried_treasure", BURIED_TREASURE
    );

    private final String type;

    public static MatterStructurePOI get(String id) {
        MatterStructurePOI poi = VALUES.get(id);
        return poi != null ? poi : new MatterStructurePOI(id);
    }
}
