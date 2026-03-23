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

package dev.krud.world.util.parallel;

import dev.krud.world.util.math.Position2;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamUtils {

    public static Stream<Position2> streamRadius(int x, int z, int radius) {
        return streamRadius(x, z, radius, radius);
    }

    public static Stream<Position2> streamRadius(int x, int z, int radiusX, int radiusZ) {
        return IntStream.rangeClosed(-radiusX, radiusX)
                .mapToObj(xx -> IntStream.rangeClosed(-radiusZ, radiusZ)
                        .mapToObj(zz -> new Position2(x + xx, z + zz)))
                .flatMap(Function.identity());
    }

    public static <T, M> void forEach(Stream<T> stream, Function<T, Stream<M>> mapper, Consumer<M> consumer, @Nullable MultiBurst burst) {
        forEach(stream.flatMap(mapper), consumer, burst);
    }

    @SneakyThrows
    public static <T> void forEach(Stream<T> stream, Consumer<T> task, @Nullable MultiBurst burst) {
        if (burst == null) stream.forEach(task);
        else {
            var list = stream.toList();
            var exec = burst.burst(list.size());
            list.forEach(val -> exec.queue(() -> task.accept(val)));
            exec.complete();
        }
    }
}
