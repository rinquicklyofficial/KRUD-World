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

package dev.krud.world.engine.object.annotations.functions;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.framework.ListFunction;
import dev.krud.world.engine.mantle.ComponentFlag;
import dev.krud.world.engine.mantle.MantleComponent;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.mantle.flag.MantleFlag;

import java.util.Objects;

public class ComponentFlagFunction implements ListFunction<KList<String>> {
    @Override
    public String key() {
        return "component-flag";
    }

    @Override
    public String fancyName() {
        return "Component Flag";
    }

    @Override
    public KList<String> apply(KrudWorldData data) {
        var engine = data.getEngine();
        if (engine != null) return engine.getMantle().getComponentFlags().toStringList();
        return KrudWorld.getClasses("dev.krud.world.engine.mantle.components", ComponentFlag.class)
                .stream()
                .filter(MantleComponent.class::isAssignableFrom)
                .map(c -> c.getDeclaredAnnotation(ComponentFlag.class))
                .filter(Objects::nonNull)
                .map(ComponentFlag::value)
                .map(MantleFlag::toString)
                .collect(KList.collector());
    }
}
