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

package dev.krud.world.util.scheduling.jobs;

import dev.krud.world.util.network.DL;
import dev.krud.world.util.network.DownloadMonitor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadJob implements Job {
    private final DL.Download download;
    private int tw;
    private int cw;

    public DownloadJob(String url, File destination) throws MalformedURLException {
        tw = 1;
        cw = 0;
        download = new DL.Download(new URL(url), destination, DL.DownloadFlag.CALCULATE_SIZE);
        download.monitor(new DownloadMonitor() {
            @Override
            public void onUpdate(DL.DownloadState state, double progress, long elapsed, long estimated, long bps, long iobps, long size, long downloaded, long buffer, double bufferuse) {
                if (size == -1) {
                    tw = 1;
                } else {
                    tw = (int) (size / 100);
                    cw = (int) (downloaded / 100);
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Downloading";
    }

    @Override
    public void execute() {
        try {
            download.start();
            while (download.isState(DL.DownloadState.DOWNLOADING)) {
                download.downloadChunk();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        cw = tw;
    }

    @Override
    public void completeWork() {

    }

    @Override
    public int getTotalWork() {
        return tw;
    }

    @Override
    public int getWorkCompleted() {
        return cw;
    }
}
