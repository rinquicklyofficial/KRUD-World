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

package dev.krud.world.util.scheduling;

import dev.krud.world.KrudWorld;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.math.RollingSequence;

/**
 * Not particularly efficient or perfectly accurate but is great at fast thread
 * switching detection
 *
 * @author dan
 */
public class ThreadMonitor extends Thread {
    private final Thread monitor;
    private final ChronoLatch cl;
    private final RollingSequence sq = new RollingSequence(3);
    int cycles = 0;
    private boolean running;
    private State lastState;
    private PrecisionStopwatch st;

    private ThreadMonitor(Thread monitor) {
        running = true;
        st = PrecisionStopwatch.start();
        this.monitor = monitor;
        lastState = State.NEW;
        cl = new ChronoLatch(1000);
        start();
    }

    public static ThreadMonitor bind(Thread monitor) {
        return new ThreadMonitor(monitor);
    }

    public void run() {
        while (running) {
            try {
                //noinspection BusyWait
                Thread.sleep(0);
                State s = monitor.getState();
                if (lastState != s) {
                    cycles++;
                    pushState(s);
                }

                lastState = s;

                if (cl.flip()) {
                    KrudWorld.info("Cycles: " + Form.f(cycles) + " (" + Form.duration(sq.getAverage(), 2) + ")");
                }
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                running = false;
                break;
            }
        }
    }

    public void pushState(State s) {
        if (s != State.RUNNABLE) {
            if (st != null) {
                sq.put(st.getMilliseconds());
            }
        } else {

            st = PrecisionStopwatch.start();
        }
    }

    public void unbind() {
        running = false;
    }
}
