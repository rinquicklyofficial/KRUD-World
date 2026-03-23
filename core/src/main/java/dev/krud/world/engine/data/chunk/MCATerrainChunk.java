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

package dev.krud.world.engine.data.chunk;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.nms.BiomeBaseInjector;
import dev.krud.world.util.data.KrudWorldCustomData;
import dev.krud.world.util.nbt.mca.Chunk;
import dev.krud.world.util.nbt.mca.NBTWorld;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

@Builder
@AllArgsConstructor
public class MCATerrainChunk implements TerrainChunk {
    private final NBTWorld writer;
    private final BiomeBaseInjector injector;
    private final int ox;
    private final int oz;
    private final int minHeight;
    private final int maxHeight;
    private final Chunk mcaChunk;

    @Override
    public BiomeBaseInjector getBiomeBaseInjector() {
        return injector;
    }

    @Override
    public Biome getBiome(int x, int z) {
        return Biome.THE_VOID;
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return Biome.THE_VOID;
    }

    @Override
    public void setBiome(int x, int z, Biome bio) {
        setBiome(ox + x, 0, oz + z, bio);
    }

    @Override
    public void setBiome(int x, int y, int z, Biome bio) {
        mcaChunk.setBiomeAt((ox + x) & 15, y, (oz + z) & 15, writer.getBiomeId(bio));
    }

    @Override
    public int getMinHeight() {
        return minHeight;
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public void setBlock(int x, int y, int z, BlockData blockData) {
        int xx = (x + ox) & 15;
        int zz = (z + oz) & 15;

        if (y > getMaxHeight() || y < getMinHeight()) {
            return;
        }

        if (blockData == null) {
            KrudWorld.error("NULL BD");
        }
        if (blockData instanceof KrudWorldCustomData data)
            blockData = data.getBase();

        mcaChunk.setBlockStateAt(xx, y, zz, NBTWorld.getCompound(blockData), false);
    }

    @Override
    public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) {
        if (y > getMaxHeight()) {
            y = getMaxHeight();
        }

        if (y < getMinHeight()) {
            y = getMinHeight();
        }

        return NBTWorld.getBlockData(mcaChunk.getBlockStateAt((x + ox) & 15, y, (z + oz) & 15));
    }

    @Override
    public ChunkGenerator.ChunkData getRaw() {
        return null;
    }

    @Override
    public void setRaw(ChunkGenerator.ChunkData data) {

    }

    @Override
    public void inject(ChunkGenerator.BiomeGrid biome) {

    }

    @Override
    public void setBlock(int x, int y, int z, Material material) {

    }

    @Override
    public void setBlock(int x, int y, int z, MaterialData material) {

    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {

    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData material) {

    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockData blockData) {

    }


    @Override
    public Material getType(int x, int y, int z) {
        return null;
    }


    @Override
    public MaterialData getTypeAndData(int x, int y, int z) {
        return null;
    }

    @Override
    public byte getData(int x, int y, int z) {
        return 0;
    }
}
