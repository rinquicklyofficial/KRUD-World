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

package dev.krud.world.util.nbt.mca;

public class LoadFlags {

    public static final long BIOMES = 0x0001;
    public static final long HEIGHTMAPS = 0x0002;
    public static final long CARVING_MASKS = 0x0004;
    public static final long ENTITIES = 0x0008;
    public static final long TILE_ENTITIES = 0x0010;
    public static final long TILE_TICKS = 0x0040;
    public static final long LIQUID_TICKS = 0x0080;
    public static final long TO_BE_TICKED = 0x0100;
    public static final long POST_PROCESSING = 0x0200;
    public static final long STRUCTURES = 0x0400;
    public static final long BLOCK_LIGHTS = 0x0800;
    public static final long BLOCK_STATES = 0x1000;
    public static final long SKY_LIGHT = 0x2000;
    public static final long LIGHTS = 0x4000;
    public static final long LIQUIDS_TO_BE_TICKED = 0x8000;

    public static final long ALL_DATA = 0xffffffffffffffffL;


}
