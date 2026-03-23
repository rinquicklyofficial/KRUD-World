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
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("tree-settings")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Desc("Tree growth override settings")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldTreeSettings {

    @Required
    @Desc("Turn replacing on and off")
    boolean enabled = false;

    @Desc("Object picking modes")
    KrudWorldTreeModes mode = KrudWorldTreeModes.FIRST;
}
