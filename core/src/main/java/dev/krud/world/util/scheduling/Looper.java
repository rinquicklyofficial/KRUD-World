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
import dev.krud.world.core.service.PreservationSVC;

public abstract class Looper extends Thread {
    @SuppressWarnings("BusyWait")
    public void run() {
        KrudWorld.service(PreservationSVC.class).register(this);
        while (!interrupted()) {
            try {
                long m = loop();

                if (m < 0) {
                    break;
                }

                //noinspection BusyWait
                Thread.sleep(m);
            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
        }

        KrudWorld.debug("KrudWorld Thread " + getName() + " Shutdown.");
    }

    protected abstract long loop();
}
