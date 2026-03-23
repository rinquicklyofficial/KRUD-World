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

package dev.krud.world.engine;

import dev.krud.world.core.nms.container.Pair;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.mantle.EngineMantle;
import dev.krud.world.engine.mantle.MantleComponent;
import dev.krud.world.engine.mantle.components.MantleCarvingComponent;
import dev.krud.world.engine.mantle.components.MantleFluidBodyComponent;
import dev.krud.world.engine.mantle.components.MantleJigsawComponent;
import dev.krud.world.engine.mantle.components.MantleObjectComponent;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.mantle.flag.MantleFlag;
import lombok.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "engine")
@ToString(exclude = "engine")
public class KrudWorldEngineMantle implements EngineMantle {
    private final Engine engine;
    private final Mantle mantle;
    @Getter(AccessLevel.NONE)
    private final KMap<Integer, KList<MantleComponent>> components;
    private final KMap<MantleFlag, MantleComponent> registeredComponents = new KMap<>();
    private final AtomicCache<List<Pair<List<MantleComponent>, Integer>>> componentsCache = new AtomicCache<>();
    private final AtomicCache<Set<MantleFlag>> disabledFlags = new AtomicCache<>();
    private final MantleObjectComponent object;
    private final MantleJigsawComponent jigsaw;

    public KrudWorldEngineMantle(Engine engine) {
        this.engine = engine;
        this.mantle = new Mantle(new File(engine.getWorld().worldFolder(), "mantle"), engine.getTarget().getHeight());
        components = new KMap<>();
        registerComponent(new MantleCarvingComponent(this));
        registerComponent(new MantleFluidBodyComponent(this));
        jigsaw = new MantleJigsawComponent(this);
        registerComponent(jigsaw);
        object = new MantleObjectComponent(this);
        registerComponent(object);
    }

    @Override
    public int getRadius() {
        if (components.isEmpty()) return 0;
        return getComponents().getFirst().getB();
    }

    @Override
    public int getRealRadius() {
        if (components.isEmpty()) return 0;
        return getComponents().getLast().getB();
    }

    @Override
    public List<Pair<List<MantleComponent>, Integer>> getComponents() {
        return componentsCache.aquire(() -> {
            var list = components.keySet()
                    .stream()
                    .sorted()
                    .map(components::get)
                    .map(components -> {
                        int radius = components.stream()
                                .filter(MantleComponent::isEnabled)
                                .mapToInt(MantleComponent::getRadius)
                                .max()
                                .orElse(0);
                        return new Pair<>(List.copyOf(components), radius);
                    })
                    .filter(pair -> !pair.getA().isEmpty())
                    .toList();

            int radius = 0;
            for (var pair : list.reversed()) {
                radius += pair.getB();
                pair.setB(Math.ceilDiv(radius, 16));
            }

            return list;
        });
    }

    @Override
    public Map<MantleFlag, MantleComponent> getRegisteredComponents() {
        return Collections.unmodifiableMap(registeredComponents);
    }

    @Override
    public boolean registerComponent(MantleComponent c) {
        if (registeredComponents.putIfAbsent(c.getFlag(), c) != null) return false;
        c.setEnabled(!getDisabledFlags().contains(c.getFlag()));
        components.computeIfAbsent(c.getPriority(), k -> new KList<>()).add(c);
        componentsCache.reset();
        return true;
    }

    @Override
    public KList<MantleFlag> getComponentFlags() {
        return new KList<>(registeredComponents.keySet());
    }

    @Override
    public void hotload() {
        disabledFlags.reset();
        for (var component : registeredComponents.values()) {
            component.hotload();
            component.setEnabled(!getDisabledFlags().contains(component.getFlag()));
        }
        componentsCache.reset();
    }

    private Set<MantleFlag> getDisabledFlags() {
        return disabledFlags.aquire(() -> Set.copyOf(getDimension().getDisabledComponents()));
    }

    @Override
    public MantleJigsawComponent getJigsawComponent() {
        return jigsaw;
    }

    @Override
    public MantleObjectComponent getObjectComponent() {
        return object;
    }
}
