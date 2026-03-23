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

package dev.krud.world.core.pregenerator;

public interface PregenListener {
    void onTick(double chunksPerSecond, double chunksPerMinute, double regionsPerMinute, double percent, long generated, long totalChunks, long chunksRemaining, long eta, long elapsed, String method, boolean cached);

    void onChunkGenerating(int x, int z);

    default void onChunkGenerated(int x, int z) {
        onChunkGenerated(x, z, false);
    }

    void onChunkGenerated(int x, int z, boolean cached);

    void onRegionGenerated(int x, int z);

    void onRegionGenerating(int x, int z);

    void onChunkCleaned(int x, int z);

    void onRegionSkipped(int x, int z);

    void onNetworkStarted(int x, int z);

    void onNetworkFailed(int x, int z);

    void onNetworkReclaim(int revert);

    void onNetworkGeneratedChunk(int x, int z);

    void onNetworkDownloaded(int x, int z);

    void onClose();

    void onSaving();

    void onChunkExistsInRegionGen(int x, int z);
}
