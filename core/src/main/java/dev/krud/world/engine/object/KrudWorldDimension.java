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

import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.ServerConfigurator.DimensionHeight;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.nms.datapack.IDataFixer;
import dev.krud.world.core.nms.datapack.IDataFixer.Dimension;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.engine.object.annotations.functions.ComponentFlagFunction;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.data.DataProvider;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.math.Position2;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;

import java.io.*;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Desc("Represents a dimension")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldDimension extends KrudWorldRegistrant {
    public static final BlockData STONE = Material.STONE.createBlockData();
    public static final BlockData WATER = Material.WATER.createBlockData();
    private final transient AtomicCache<Position2> parallaxSize = new AtomicCache<>();
    private final transient AtomicCache<CNG> rockLayerGenerator = new AtomicCache<>();
    private final transient AtomicCache<CNG> fluidLayerGenerator = new AtomicCache<>();
    private final transient AtomicCache<CNG> coordFracture = new AtomicCache<>();
    private final transient AtomicCache<Double> sinr = new AtomicCache<>();
    private final transient AtomicCache<Double> cosr = new AtomicCache<>();
    private final transient AtomicCache<Double> rad = new AtomicCache<>();
    private final transient AtomicCache<Boolean> featuresUsed = new AtomicCache<>();
    private final transient AtomicCache<KList<Position2>> strongholdsCache = new AtomicCache<>();
    private final transient AtomicCache<KMap<String, KList<String>>> cachedPreProcessors = new AtomicCache<>();
    @MinNumber(2)
    @Required
    @Desc("The human readable name of this dimension")
    private String name = "A Dimension";
    @MinNumber(1)
    @MaxNumber(2032)
    @Desc("Maximum height at which players can be teleported to through gameplay.")
    private int logicalHeight = 256;
    @RegistryListResource(KrudWorldJigsawStructure.class)
    @Desc("If defined, KrudWorld will place the given jigsaw structure where minecraft should place the overworld stronghold.")
    private String stronghold;
    @Desc("If set to true, KrudWorld will remove chunks to allow visualizing cross sections of chunks easily")
    private boolean debugChunkCrossSections = false;
    @Desc("Vertically split up the biome palettes with 3 air blocks in between to visualize them")
    private boolean explodeBiomePalettes = false;
    @Desc("Studio Mode for testing different parts of the world")
    private StudioMode studioMode = StudioMode.NORMAL;
    @MinNumber(1)
    @MaxNumber(16)
    @Desc("Customize the palette height explosion")
    private int explodeBiomePaletteSize = 3;
    @MinNumber(2)
    @MaxNumber(16)
    @Desc("Every X/Z % debugCrossSectionsMod == 0 cuts the chunk")
    private int debugCrossSectionsMod = 3;
    @Desc("The average distance between strongholds")
    private int strongholdJumpDistance = 1280;
    @Desc("Define the maximum strongholds to place")
    private int maxStrongholds = 14;
    @Desc("Tree growth override settings")
    private KrudWorldTreeSettings treeSettings = new KrudWorldTreeSettings();
    @Desc("Spawn Entities in this dimension over time. KrudWorld will continually replenish these mobs just like vanilla does.")
    @ArrayType(min = 1, type = String.class)
    @RegistryListResource(KrudWorldSpawner.class)
    private KList<String> entitySpawners = new KList<>();
    @Desc("Reference loot tables in this area")
    private KrudWorldLootReference loot = new KrudWorldLootReference();
    @MinNumber(0)
    @Desc("The version of this dimension. Changing this will stop users from accidentally upgrading (and breaking their worlds).")
    private int version = 1;
    @ArrayType(min = 1, type = KrudWorldBlockDrops.class)
    @Desc("Define custom block drops for this dimension")
    private KList<KrudWorldBlockDrops> blockDrops = new KList<>();
    @Desc("Should bedrock be generated or not.")
    private boolean bedrock = true;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("The land chance. Up to 1.0 for total land or 0.0 for total sea")
    private double landChance = 0.625;
    @Desc("The placement style of regions")
    private KrudWorldGeneratorStyle regionStyle = NoiseStyle.CELLULAR_IRIS_DOUBLE.style();
    @Desc("The placement style of land/sea")
    private KrudWorldGeneratorStyle continentalStyle = NoiseStyle.CELLULAR_IRIS_DOUBLE.style();
    @Desc("The placement style of biomes")
    private KrudWorldGeneratorStyle landBiomeStyle = NoiseStyle.CELLULAR_IRIS_DOUBLE.style();
    @Desc("The placement style of biomes")
    private KrudWorldGeneratorStyle shoreBiomeStyle = NoiseStyle.CELLULAR_IRIS_DOUBLE.style();
    @Desc("The placement style of biomes")
    private KrudWorldGeneratorStyle seaBiomeStyle = NoiseStyle.CELLULAR_IRIS_DOUBLE.style();
    @Desc("The placement style of biomes")
    private KrudWorldGeneratorStyle caveBiomeStyle = NoiseStyle.CELLULAR_IRIS_DOUBLE.style();
    @Desc("Instead of filling objects with air, fills them with cobweb so you can see them")
    private boolean debugSmartBore = false;
    @Desc("Generate decorations or not")
    private boolean decorate = true;
    @Desc("Use post processing or not")
    private boolean postProcessing = true;
    @Desc("Add slabs in post processing")
    private boolean postProcessingSlabs = true;
    @Desc("Add painted walls in post processing")
    private boolean postProcessingWalls = true;
    @Desc("Carving configuration for the dimension")
    private KrudWorldCarving carving = new KrudWorldCarving();
    @Desc("Configuration of fluid bodies such as rivers & lakes")
    private KrudWorldFluidBodies fluidBodies = new KrudWorldFluidBodies();
    @Desc("forceConvertTo320Height")
    private Boolean forceConvertTo320Height = false;
    @Desc("The world environment")
    private Environment environment = Environment.NORMAL;
    @RegistryListResource(KrudWorldRegion.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("Define all of the regions to include in this dimension. Dimensions -> Regions -> Biomes -> Objects etc")
    private KList<String> regions = new KList<>();
    @ArrayType(min = 1, type = KrudWorldJigsawStructurePlacement.class)
    @Desc("Jigsaw structures")
    private KList<KrudWorldJigsawStructurePlacement> jigsawStructures = new KList<>();
    @Desc("The jigsaw structure divisor to use when generating missing jigsaw placement values")
    private double jigsawStructureDivisor = 18;
    @Required
    @MinNumber(0)
    @MaxNumber(1024)
    @Desc("The fluid height for this dimension")
    private int fluidHeight = 63;
    @Desc("Define the min and max Y bounds of this dimension. Please keep in mind that KrudWorld internally generates from 0 to (max - min). \n\nFor example at -64 to 320, KrudWorld is internally generating to 0 to 384, then on outputting chunks, it shifts it down by the min height (64 blocks). The default is -64 to 320. \n\nThe fluid height is placed at (fluid height + min height). So a fluid height of 63 would actually show up in the world at 1.")
    private KrudWorldRange dimensionHeight = new KrudWorldRange(-64, 320);
    @Desc("Define options for this dimension")
    private KrudWorldDimensionTypeOptions dimensionOptions = new KrudWorldDimensionTypeOptions();
    @RegistryListResource(KrudWorldBiome.class)
    @Desc("Keep this either undefined or empty. Setting any biome name into this will force iris to only generate the specified biome. Great for testing.")
    private String focus = "";
    @RegistryListResource(KrudWorldRegion.class)
    @Desc("Keep this either undefined or empty. Setting any region name into this will force iris to only generate the specified region. Great for testing.")
    private String focusRegion = "";
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("Zoom in or out the biome size. Higher = bigger biomes")
    private double biomeZoom = 1D;
    @MinNumber(0)
    @MaxNumber(360)
    @Desc("You can rotate the input coordinates by an angle. This can make terrain appear more natural (less sharp corners and lines). This literally rotates the entire dimension by an angle. Hint: Try 12 degrees or something not on a 90 or 45 degree angle.")
    private double dimensionAngleDeg = 0;
    @Required
    @Desc("Define the mode of this dimension (required!)")
    private KrudWorldDimensionMode mode = new KrudWorldDimensionMode();
    @MinNumber(0)
    @MaxNumber(8192)
    @Desc("Coordinate fracturing applies noise to the input coordinates. This creates the 'iris swirls' and wavy features. The distance pushes these waves further into places they shouldnt be. This is a block value multiplier.")
    private double coordFractureDistance = 20;
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("Coordinate fracturing zoom. Higher = less frequent warping, Lower = more frequent and rapid warping / swirls.")
    private double coordFractureZoom = 8;
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("This zooms in the land space")
    private double landZoom = 1;
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("This zooms oceanic biomes")
    private double seaZoom = 1;
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("Zoom in continents")
    private double continentZoom = 1;
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("Change the size of regions")
    private double regionZoom = 1;
    @Desc("Disable this to stop placing objects, entities, features & updates")
    private boolean useMantle = true;
    @Desc("Prevent Leaf decay as if placed in creative mode")
    private boolean preventLeafDecay = false;
    @ArrayType(min = 1, type = KrudWorldDepositGenerator.class)
    @Desc("Define global deposit generators")
    private KList<KrudWorldDepositGenerator> deposits = new KList<>();
    @ArrayType(min = 1, type = KrudWorldShapedGeneratorStyle.class)
    @Desc("Overlay additional noise on top of the interoplated terrain.")
    private KList<KrudWorldShapedGeneratorStyle> overlayNoise = new KList<>();
    @Desc("If true, the spawner system has infinite energy. This is NOT recommended because it would allow for mobs to keep spawning over and over without a rate limit")
    private boolean infiniteEnergy = false;
    @MinNumber(0)
    @MaxNumber(10000)
    @Desc("This is the maximum energy you can have in a dimension")
    private double maximumEnergy = 1000;
    @MinNumber(0.0001)
    @MaxNumber(512)
    @Desc("The rock zoom mostly for zooming in on a wispy palette")
    private double rockZoom = 5;
    @Desc("The palette of blocks for 'stone'")
    private KrudWorldMaterialPalette rockPalette = new KrudWorldMaterialPalette().qclear().qadd("stone");
    @Desc("The palette of blocks for 'water'")
    private KrudWorldMaterialPalette fluidPalette = new KrudWorldMaterialPalette().qclear().qadd("water");
    @Desc("Prevent cartographers to generate explorer maps (KrudWorld worlds only)\nONLY TOUCH IF YOUR SERVER CRASHES WHILE GENERATING EXPLORER MAPS")
    private boolean disableExplorerMaps = false;
    @Desc("Collection of ores to be generated")
    @ArrayType(type = KrudWorldOreGenerator.class, min = 1)
    private KList<KrudWorldOreGenerator> ores = new KList<>();
    @MinNumber(0)
    @MaxNumber(318)
    @Desc("The Subterrain Fluid Layer Height")
    private int caveLavaHeight = 8;
    @RegistryListFunction(ComponentFlagFunction.class)
    @ArrayType(type = String.class)
    @Desc("Collection of disabled components")
    private KList<MantleFlag> disabledComponents = new KList<>();
    @Desc("A list of globally applied pre-processors")
    @ArrayType(type = KrudWorldPreProcessors.class)
    private KList<KrudWorldPreProcessors> globalPreProcessors = new KList<>();
    @Desc("A list of scripts executed on engine setup\nFile extension: .engine.kts")
    @RegistryListResource(KrudWorldScript.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> engineScripts = new KList<>();
    @Desc("A list of scripts executed on data setup\nFile extension: .data.kts")
    @RegistryListResource(KrudWorldScript.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> dataScripts = new KList<>();
    @Desc("A list of scripts executed on chunk update\nFile extension: .update.kts")
    @RegistryListResource(KrudWorldScript.class)
    @ArrayType(type = String.class, min = 1)
    private KList<String> chunkUpdateScripts = new KList<>();
    @Desc("Use legacy rarity instead of modern one\nWARNING: Changing this may break expressions and image maps")
    private boolean legacyRarity = true;

    public int getMaxHeight() {
        return (int) getDimensionHeight().getMax();
    }

    public int getMinHeight() {
        return (int) getDimensionHeight().getMin();
    }

    public BlockData generateOres(int x, int y, int z, RNG rng, KrudWorldData data, boolean surface) {
        if (ores.isEmpty()) {
            return null;
        }
        BlockData b = null;
        for (KrudWorldOreGenerator i : ores) {
            if (i.isGenerateSurface() != surface)
                continue;

            b = i.generate(x, y, z, rng, data);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    public KList<Position2> getStrongholds(long seed) {
        return strongholdsCache.aquire(() -> {
            KList<Position2> pos = new KList<>();
            int jump = strongholdJumpDistance;
            RNG rng = new RNG((seed * 223) + 12945);
            for (int i = 0; i < maxStrongholds + 1; i++) {
                int m = i + 1;
                pos.add(new Position2(
                        (int) ((rng.i(jump * i) + (jump * i)) * (rng.b() ? -1D : 1D)),
                        (int) ((rng.i(jump * i) + (jump * i)) * (rng.b() ? -1D : 1D))
                ));
            }

            pos.remove(0);

            return pos;
        });
    }

    public int getFluidHeight() {
        return fluidHeight - (int) dimensionHeight.getMin();
    }

    public CNG getCoordFracture(RNG rng, int signature) {
        return coordFracture.aquire(() ->
        {
            CNG coordFracture = CNG.signature(rng.nextParallelRNG(signature));
            coordFracture.scale(0.012 / coordFractureZoom);
            return coordFracture;
        });
    }

    public double getDimensionAngle() {
        return rad.aquire(() -> Math.toRadians(dimensionAngleDeg));
    }

    public Environment getEnvironment() {
        return environment;
    }

    public boolean hasFocusRegion() {
        return !focusRegion.equals("");
    }

    public String getFocusRegion() {
        return focusRegion;
    }

    public double sinRotate() {
        return sinr.aquire(() -> Math.sin(getDimensionAngle()));
    }

    public double cosRotate() {
        return cosr.aquire(() -> Math.cos(getDimensionAngle()));
    }

    public KList<KrudWorldRegion> getAllRegions(DataProvider g) {
        KList<KrudWorldRegion> r = new KList<>();

        for (String i : getRegions()) {
            r.add(g.getData().getRegionLoader().load(i));
        }

        return r;
    }

    public KList<KrudWorldRegion> getAllAnyRegions() {
        KList<KrudWorldRegion> r = new KList<>();

        for (String i : getRegions()) {
            r.add(KrudWorldData.loadAnyRegion(i, getLoader()));
        }

        return r;
    }

    public KList<KrudWorldBiome> getAllBiomes(DataProvider g) {
        return g.getData().getBiomeLoader().loadAll(g.getData().getBiomeLoader().getPossibleKeys());
    }

    public KList<KrudWorldBiome> getAllAnyBiomes() {
        KList<KrudWorldBiome> r = new KList<>();

        for (KrudWorldRegion i : getAllAnyRegions()) {
            if (i == null) {
                continue;
            }

            r.addAll(i.getAllAnyBiomes());
        }

        return r;
    }

    public KList<String> getPreProcessors(String type) {
        return cachedPreProcessors.aquire(() -> {
            KMap<String, KList<String>> preProcessors = new KMap<>();
            for (var entry : globalPreProcessors) {
                preProcessors.computeIfAbsent(entry.getType(), k -> new KList<>())
                        .addAll(entry.getScripts());
            }
            return preProcessors;
        }).get(type);
    }

    public KrudWorldGeneratorStyle getBiomeStyle(InferredType type) {
        switch (type) {
            case CAVE:
                return caveBiomeStyle;
            case LAND:
                return landBiomeStyle;
            case SEA:
                return seaBiomeStyle;
            case SHORE:
                return shoreBiomeStyle;
            default:
                break;
        }

        return landBiomeStyle;
    }

    public void installBiomes(IDataFixer fixer, DataProvider data, KList<File> folders, KSet<String> biomes) {
        getAllBiomes(data)
                .stream()
                .filter(KrudWorldBiome::isCustom)
                .map(KrudWorldBiome::getCustomDerivitives)
                .flatMap(KList::stream)
                .parallel()
                .forEach(j -> {
                    String json = j.generateJson(fixer);
                    synchronized (biomes) {
                        if (!biomes.add(j.getId())) {
                            KrudWorld.verbose("Duplicate Data Pack Biome: " + getLoadKey() + "/" + j.getId());
                            return;
                        }
                    }

                    for (File datapacks : folders) {
                        File output = new File(datapacks, "iris/data/" + getLoadKey().toLowerCase() + "/worldgen/biome/" + j.getId() + ".json");

                        KrudWorld.verbose("    Installing Data Pack Biome: " + output.getPath());
                        output.getParentFile().mkdirs();
                        try {
                            IO.writeAll(output, json);
                        } catch (IOException e) {
                            KrudWorld.reportError(e);
                            e.printStackTrace();
                        }
                    }
                });
    }

    public Dimension getBaseDimension() {
        return switch (getEnvironment()) {
            case NETHER -> Dimension.NETHER;
            case THE_END -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }

    public String getDimensionTypeKey() {
        return getDimensionType().key();
    }

    public KrudWorldDimensionType getDimensionType() {
        return new KrudWorldDimensionType(getBaseDimension(), getDimensionOptions(), getLogicalHeight(), getMaxHeight() - getMinHeight(), getMinHeight());
    }

    public void installDimensionType(IDataFixer fixer, KList<File> folders) {
        KrudWorldDimensionType type = getDimensionType();
        String json = type.toJson(fixer);

        KrudWorld.verbose("    Installing Data Pack Dimension Type: \"iris:" + type.key() + '"');
        for (File datapacks : folders) {
            File output = new File(datapacks, "iris/data/iris/dimension_type/" + type.key() + ".json");
            output.getParentFile().mkdirs();
            try {
                IO.writeAll(output, json);
            } catch (IOException e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getFolderName() {
        return "dimensions";
    }

    @Override
    public String getTypeName() {
        return "Dimension";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }

    public static void writeShared(KList<File> folders, DimensionHeight height) {
        KrudWorld.verbose("    Installing Data Pack Vanilla Dimension Types");
        String[] jsonStrings = height.jsonStrings();
        for (File datapacks : folders) {
            write(datapacks, "overworld", jsonStrings[0]);
            write(datapacks, "the_nether", jsonStrings[1]);
            write(datapacks, "the_end", jsonStrings[2]);
        }

        String raw = """
                        {
                            "pack": {
                                "description": "KrudWorld Data Pack. This pack contains all installed KrudWorld Packs' resources.",
                                "pack_format": {}
                            }
                        }
                        """.replace("{}", INMS.get().getDataVersion().getPackFormat() + "");

        for (File datapacks : folders) {
            File mcm = new File(datapacks, "iris/pack.mcmeta");
            try {
                IO.writeAll(mcm, raw);
            } catch (IOException e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
            KrudWorld.verbose("    Installing Data Pack MCMeta: " + mcm.getPath());
        }
    }

    private static void write(File datapacks, String type, String json) {
        if (json == null) return;
        File dimTypeVanilla = new File(datapacks, "iris/data/minecraft/dimension_type/" + type + ".json");

        if (KrudWorldSettings.get().getGeneral().adjustVanillaHeight || dimTypeVanilla.exists()) {
            dimTypeVanilla.getParentFile().mkdirs();
            try {
                IO.writeAll(dimTypeVanilla, json);
            } catch (IOException e) {
                KrudWorld.reportError(e);
                e.printStackTrace();
            }
        }
    }
}
