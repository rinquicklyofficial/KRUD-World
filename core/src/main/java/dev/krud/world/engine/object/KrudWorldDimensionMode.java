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

import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.framework.EngineMode;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.RegistryListResource;
import dev.krud.world.engine.object.annotations.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Snippet("dimension-mode")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a dimensional mode")
@Data
public class KrudWorldDimensionMode {
    @Desc("The dimension type")
    private KrudWorldDimensionModeType type = KrudWorldDimensionModeType.OVERWORLD;

    @RegistryListResource(KrudWorldScript.class)
    @Desc("The script to create the dimension mode instead of using provided types\nFile extension: .engine.kts")
    private String script;

    public EngineMode create(Engine engine) {
        if (script == null) {
            return type.create(engine);
        }
        Object result = engine.getExecution().evaluate(script);
        if (result instanceof EngineMode) {
            return (EngineMode) result;
        }

        throw new IllegalStateException("The script '" + script + "' did not return an engine mode!");
    }
}
