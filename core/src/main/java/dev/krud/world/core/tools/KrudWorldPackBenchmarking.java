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

package dev.krud.world.core.tools;


import dev.krud.world.KrudWorld;
import dev.krud.world.core.pregenerator.PregenTask;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.KrudWorldDimension;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.exceptions.KrudWorldException;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.scheduling.PrecisionStopwatch;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;


public class KrudWorldPackBenchmarking {
    private static final ThreadLocal<KrudWorldPackBenchmarking> instance = new ThreadLocal<>();
    private final PrecisionStopwatch stopwatch = new PrecisionStopwatch();
    private final KrudWorldDimension dimension;
    private final int radius;
    private final boolean gui;

    public KrudWorldPackBenchmarking(KrudWorldDimension dimension, int radius, boolean gui) {
        this.dimension = dimension;
        this.radius = radius;
        this.gui = gui;
        runBenchmark();
    }

    public static KrudWorldPackBenchmarking getInstance() {
        return instance.get();
    }

    private void runBenchmark() {
        Thread.ofVirtual()
                .name("PackBenchmarking")
                .start(() -> {
                    KrudWorld.info("Setting up benchmark environment ");
                    IO.delete(new File(Bukkit.getWorldContainer(), "benchmark"));
                    createBenchmark();
                    while (!KrudWorldToolbelt.isKrudWorldWorld(Bukkit.getWorld("benchmark"))) {
                        J.sleep(1000);
                        KrudWorld.debug("KrudWorld PackBenchmark: Waiting...");
                    }
                    KrudWorld.info("Starting Benchmark!");
                    stopwatch.begin();
                    startBenchmark();
                });

    }

    public void finishedBenchmark(KList<Integer> cps) {
        try {
            String time = Form.duration((long) stopwatch.getMilliseconds());
            Engine engine = KrudWorldToolbelt.access(Bukkit.getWorld("benchmark")).getEngine();
            KrudWorld.info("-----------------");
            KrudWorld.info("Results:");
            KrudWorld.info("- Total time: " + time);
            KrudWorld.info("- Average CPS: " + calculateAverage(cps));
            KrudWorld.info("  - Median CPS: " + calculateMedian(cps));
            KrudWorld.info("  - Highest CPS: " + findHighest(cps));
            KrudWorld.info("  - Lowest CPS: " + findLowest(cps));
            KrudWorld.info("-----------------");
            KrudWorld.info("Creating a report..");
            File results = KrudWorld.instance.getDataFile("packbenchmarks", dimension.getName() + " " + LocalDateTime.now(Clock.systemDefaultZone()).toString().replace(':', '-') + ".txt");
            KMap<String, Double> metrics = engine.getMetrics().pull();
            try (FileWriter writer = new FileWriter(results)) {
                writer.write("-----------------\n");
                writer.write("Results:\n");
                writer.write("Dimension: " + dimension.getName() + "\n");
                writer.write("- Date of Benchmark: " + LocalDateTime.now(Clock.systemDefaultZone()) + "\n");
                writer.write("\n");
                writer.write("Metrics");
                for (String m : metrics.k()) {
                    double i = metrics.get(m);
                    writer.write("- " + m + ": " + i);
                }
                writer.write("- " + metrics);
                writer.write("Benchmark: " + LocalDateTime.now(Clock.systemDefaultZone()) + "\n");
                writer.write("- Total time: " + time + "\n");
                writer.write("- Average CPS: " + calculateAverage(cps) + "\n");
                writer.write("  - Median CPS: " + calculateMedian(cps) + "\n");
                writer.write("  - Highest CPS: " + findHighest(cps) + "\n");
                writer.write("  - Lowest CPS: " + findLowest(cps) + "\n");
                writer.write("-----------------\n");
                KrudWorld.info("Finished generating a report!");
            } catch (IOException e) {
                KrudWorld.error("An error occurred writing to the file.");
                e.printStackTrace();
            }

            J.s(() -> {
                var world = Bukkit.getWorld("benchmark");
                if (world == null) return;
                KrudWorldToolbelt.evacuate(world);
                Bukkit.unloadWorld(world, true);
            });

            stopwatch.end();
        } catch (Exception e) {
            KrudWorld.error("Something has gone wrong!");
            e.printStackTrace();
        }
    }

    private void createBenchmark() {
        try {
            KrudWorldToolbelt.createWorld()
                    .dimension(dimension.getLoadKey())
                    .name("benchmark")
                    .seed(1337)
                    .studio(false)
                    .benchmark(true)
                    .create();
        } catch (KrudWorldException e) {
            throw new RuntimeException(e);
        }
    }

    private void startBenchmark() {
        try {
            instance.set(this);
            KrudWorldToolbelt.pregenerate(PregenTask
                    .builder()
                    .gui(gui)
                    .radiusX(radius)
                    .radiusZ(radius)
                    .build(), Bukkit.getWorld("benchmark")
            );
        } finally {
            instance.remove();
        }
    }

    private double calculateAverage(KList<Integer> list) {
        double sum = 0;
        for (int num : list) {
            sum += num;
        }
        return sum / list.size();
    }

    private double calculateMedian(KList<Integer> list) {
        Collections.sort(list);
        int middle = list.size() / 2;

        if (list.size() % 2 == 1) {
            return list.get(middle);
        } else {
            return (list.get(middle - 1) + list.get(middle)) / 2.0;
        }
    }

    private int findLowest(KList<Integer> list) {
        return Collections.min(list);
    }

    private int findHighest(KList<Integer> list) {
        return Collections.max(list);
    }
}