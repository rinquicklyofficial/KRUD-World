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

package dev.krud.world.util.network;

import java.io.IOException;
import java.io.OutputStream;

public class MeteredOutputStream extends OutputStream {
    private final OutputStream os;
    private long written;
    private long totalWritten;
    private long since;
    private boolean auto;
    private long interval;
    private long bps;

    public MeteredOutputStream(OutputStream os, long interval) {
        this.os = os;
        written = 0;
        totalWritten = 0;
        auto = true;
        this.interval = interval;
        bps = 0;
        since = System.currentTimeMillis();
    }

    public MeteredOutputStream(OutputStream os) {
        this(os, 100);
        auto = false;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
        written++;
        totalWritten++;

        if (auto && System.currentTimeMillis() - getSince() > interval) {
            pollWritten();
        }
    }

    public long getSince() {
        return since;
    }

    public long getWritten() {
        return written;
    }

    public long pollWritten() {
        long w = written;
        written = 0;
        double secondsElapsedSince = (double) (System.currentTimeMillis() - since) / 1000.0;
        bps = (long) ((double) w / secondsElapsedSince);
        since = System.currentTimeMillis();
        return w;
    }

    public void close() throws IOException {
        os.close();
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getTotalWritten() {
        return totalWritten;
    }

    public long getBps() {
        return bps;
    }
}