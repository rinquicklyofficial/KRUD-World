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

package dev.krud.world.engine.object;

import lombok.Data;

@Data
public class KrudWorldEngineStatistics {
    private int totalHotloads = 0;
    private int chunksGenerated = 0;
    private int KrudWorldToUpgradedVersion = 0;
    private int KrudWorldCreationVersion = 0;
    private int MinecraftVersion = 0;

    public void generatedChunk() {
        chunksGenerated++;
    }

    public void setUpgradedVersion(int i) {
        KrudWorldToUpgradedVersion = i;
    }
    public int getUpgradedVersion() {
        return KrudWorldToUpgradedVersion;
    }
    public void setVersion(int i) {
        KrudWorldCreationVersion = i;
    }

    public int getVersion() {
        return KrudWorldCreationVersion;
    }

    public void setMCVersion(int i) {
        MinecraftVersion = i;
    }

    public int getMCVersion() {
        return MinecraftVersion;
    }

    public void hotloaded() {
        totalHotloads++;
    }
}
