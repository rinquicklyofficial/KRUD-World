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

package dev.krud.world.util.reflect;

import dev.krud.world.KrudWorld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class WrappedReturningMethod<C, R> {

    private final Method method;

    public WrappedReturningMethod(Class<C> origin, String methodName, Class<?>... paramTypes) {
        Method m = null;
        try {
            m = origin.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
        } catch (NoSuchMethodException e) {
            KrudWorld.error("Failed to created WrappedMethod %s#%s: %s%s", origin.getSimpleName(), methodName, e.getClass().getSimpleName(), e.getMessage().equals("") ? "" : " | " + e.getMessage());
        }
        this.method = m;
    }

    public R invoke(Object... args) {
        return invoke(null, args);
    }

    public R invoke(C instance, Object... args) {
        if (method == null) {
            return null;
        }

        try {
            return (R) method.invoke(instance, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            KrudWorld.error("Failed to invoke WrappedMethod %s#%s: %s%s", method.getDeclaringClass().getSimpleName(), method.getName(), e.getClass().getSimpleName(), e.getMessage().equals("") ? "" : " | " + e.getMessage());
            return null;
        }
    }
}
