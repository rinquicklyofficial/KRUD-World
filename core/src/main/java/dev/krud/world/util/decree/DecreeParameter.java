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

package dev.krud.world.util.decree;

import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.decree.annotations.Param;
import dev.krud.world.util.decree.exceptions.DecreeParsingException;
import dev.krud.world.util.decree.specialhandlers.DummyHandler;
import lombok.Data;

import java.lang.reflect.Parameter;

@Data
public class DecreeParameter {
    private final Parameter parameter;
    private final Param param;
    private transient final AtomicCache<DecreeParameterHandler<?>> handlerCache = new AtomicCache<>();

    public DecreeParameter(Parameter parameter) {
        this.parameter = parameter;
        this.param = parameter.getDeclaredAnnotation(Param.class);
        if (param == null) {
            throw new RuntimeException("Cannot instantiate DecreeParameter on " + parameter.getName() + " in method " + parameter.getDeclaringExecutable().getName() + "(...) in class " + parameter.getDeclaringExecutable().getDeclaringClass().getCanonicalName() + " not annotated by @Param");
        }
    }

    public DecreeParameterHandler<?> getHandler() {
        return handlerCache.aquire(() -> {
            try {
                if (param.customHandler().equals(DummyHandler.class)) {
                    return DecreeSystem.getHandler(getType());
                }

                return param.customHandler().getConstructor().newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    public Class<?> getType() {
        return parameter.getType();
    }

    public String getName() {
        return param.name().isEmpty() ? parameter.getName() : param.name();
    }

    public String getDescription() {
        return param.description().isEmpty() ? Param.DEFAULT_DESCRIPTION : param.description();
    }

    public boolean isRequired() {
        return !hasDefault();
    }

    public KList<String> getNames() {
        KList<String> d = new KList<>();

        for (String i : param.aliases()) {
            if (i.isEmpty()) {
                continue;
            }

            d.add(i);
        }

        d.add(getName());
        d.removeDuplicates();

        return d;
    }

    public Object getDefaultValue() throws DecreeParsingException {
        return param.defaultValue().trim().isEmpty() ? null : getHandler().parse(param.defaultValue().trim(), true);
    }

    public boolean hasDefault() {
        return !param.defaultValue().trim().isEmpty();
    }

    public String example() {
        KList<?> ff = getHandler().getPossibilities();
        ff = ff != null ? ff : new KList<>();
        KList<String> f = ff.convert((i) -> getHandler().toStringForce(i));
        if (f.isEmpty()) {
            f = new KList<>();
            f.add(getHandler().getRandomDefault());
        }

        return f.getRandom();
    }

    public boolean isContextual() {
        return param.contextual();
    }
}
