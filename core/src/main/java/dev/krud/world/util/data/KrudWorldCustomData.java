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

package dev.krud.world.util.data;

import dev.krud.world.core.link.Identifier;
import dev.krud.world.util.collection.KMap;
import lombok.NonNull;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.*;

public interface KrudWorldCustomData extends BlockData {
	@NonNull BlockData getBase();
	@NonNull Identifier getCustom();

	static KrudWorldCustomData of(@NotNull BlockData base, @NotNull Identifier custom) {
		var clazz = base.getClass();
		var loader = KrudWorldCustomData.class.getClassLoader();
		return (KrudWorldCustomData) Proxy.newProxyInstance(loader, Internal.getInterfaces(loader, clazz), (proxy, method, args) ->
				switch (method.getName()) {
					case "getBase" -> base;
					case "getCustom" -> custom;
					case "merge" -> of(base.merge((BlockData) args[0]), custom);
					case "clone" -> of(base.clone(), custom);
					case "hashCode" -> Objects.hash(base, custom);
                    case "matches" -> {
						if (!(args[0] instanceof KrudWorldCustomData store))
							yield false;
						yield base.matches(store.getBase()) && custom.equals(store.getCustom());
					}
					case "equals" -> {
						if (!(args[0] instanceof KrudWorldCustomData store))
							yield false;
						yield store.getBase().equals(base) && store.getCustom().equals(custom);
					}
					default -> method.invoke(base, args);
				});
	}

	@ApiStatus.Internal
	abstract class Internal {
		private static final KMap<Class<?>, Class<?>[]> cache = new KMap<>();

		private static Class<?>[] getInterfaces(ClassLoader loader, Class<?> base) {
			return cache.computeIfAbsent(base, k -> {
				Set<Class<?>> set = new HashSet<>();

				Class<?> i = base;
				while (i != null) {
					if (!BlockData.class.isAssignableFrom(i))
						break;

					for (Class<?> j : i.getInterfaces()) {
						if (j.isSealed() || j.isHidden())
							continue;

						try {
							Class.forName(j.getName(), false, loader);
							set.add(j);
						} catch (ClassNotFoundException ignored) {}
					}

					i = i.getSuperclass();
				}

				set.add(KrudWorldCustomData.class);
				return set.toArray(Class<?>[]::new);
			});
		}
	}
}
