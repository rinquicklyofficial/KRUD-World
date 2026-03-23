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
import dev.krud.world.core.gui.components.RenderType;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.data.DataProvider;
import dev.krud.world.util.data.VanillaBiomeMap;
import dev.krud.world.util.inventorygui.RandomColor;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CNG;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.block.data.BlockData;

import java.awt.*;
import java.util.Random;


@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents an iris region")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldRegion extends KrudWorldRegistrant implements IRare {
    private final transient AtomicCache<KList<KrudWorldObjectPlacement>> surfaceObjectsCache = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldObjectPlacement>> carveObjectsCache = new AtomicCache<>();
    private final transient AtomicCache<KList<String>> cacheRidge = new AtomicCache<>();
    private final transient AtomicCache<KList<String>> cacheSpot = new AtomicCache<>();
    private final transient AtomicCache<CNG> shoreHeightGenerator = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldBiome>> realLandBiomes = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldBiome>> realLakeBiomes = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldBiome>> realRiverBiomes = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldBiome>> realSeaBiomes = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldBiome>> realShoreBiomes = new AtomicCache<>();
    private final transient AtomicCache<KList<KrudWorldBiome>> realCaveBiomes = new AtomicCache<>();
    private final transient AtomicCache<CNG> lakeGen = new AtomicCache<>();
    private final transient AtomicCache<CNG> riverGen = new AtomicCache<>();
    private final transient AtomicCache<CNG> riverChanceGen = new AtomicCache<>();
    private final transient AtomicCache<Color> cacheColor = new AtomicCache<>();
    @MinNumber(2)
    @Required
    @Desc("The name of the region")
    private String name = "A Region";
    @ArrayType(min = 1, type = KrudWorldJigsawStructurePlacement.class)
    @Desc("Jigsaw structures")
    private KList<KrudWorldJigsawStructurePlacement> jigsawStructures = new KList<>();
    @ArrayType(min = 1, type = KrudWorldEffect.class)
    @Desc("Effects are ambient effects such as potion effects, random sounds, or even particles around each player. All of these effects are played via packets so two players won't see/hear each others effects.\nDue to performance reasons, effects will play arround the player even if where the effect was played is no longer in the biome the player is in.")
    private KList<KrudWorldEffect> effects = new KList<>();
    @Desc("Spawn Entities in this region over time. KrudWorld will continually replenish these mobs just like vanilla does.")
    @ArrayType(min = 1, type = String.class)
    @RegistryListResource(KrudWorldSpawner.class)
    private KList<String> entitySpawners = new KList<>();
    @MinNumber(1)
    @MaxNumber(128)
    @Desc("The rarity of the region")
    private int rarity = 1;
    @ArrayType(min = 1, type = KrudWorldBlockDrops.class)
    @Desc("Define custom block drops for this region")
    private KList<KrudWorldBlockDrops> blockDrops = new KList<>();
    @RegistryListResource(KrudWorldSpawner.class)
    @ArrayType(min = 1, type = KrudWorldObjectPlacement.class)
    @Desc("Objects define what schematics (iob files) iris will place in this region")
    private KList<KrudWorldObjectPlacement> objects = new KList<>();
    @MinNumber(0)
    @Desc("The min shore height")
    private double shoreHeightMin = 1.2;
    @Desc("Reference loot tables in this area")
    private KrudWorldLootReference loot = new KrudWorldLootReference();
    @MinNumber(0)
    @Desc("The the max shore height")
    private double shoreHeightMax = 3.2;
    @MinNumber(0.0001)
    @Desc("The varience of the shore height")
    private double shoreHeightZoom = 3.14;
    @MinNumber(0.0001)
    @Desc("How large land biomes are in this region")
    private double landBiomeZoom = 1;
    @MinNumber(0.0001)
    @Desc("How large shore biomes are in this region")
    private double shoreBiomeZoom = 1;
    @MinNumber(0.0001)
    @Desc("How large sea biomes are in this region")
    private double seaBiomeZoom = 1;
    @MinNumber(0.0001)
    @Desc("How large cave biomes are in this region")
    private double caveBiomeZoom = 1;
    @Desc("Carving configuration for the dimension")
    private KrudWorldCarving carving = new KrudWorldCarving();
    @Desc("Configuration of fluid bodies such as rivers & lakes")
    private KrudWorldFluidBodies fluidBodies = new KrudWorldFluidBodies();
    @RegistryListResource(KrudWorldBiome.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("A list of root-level biomes in this region. Don't specify child biomes of other biomes here. Just the root parents.")
    private KList<String> landBiomes = new KList<>();
    @RegistryListResource(KrudWorldBiome.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("A list of root-level biomes in this region. Don't specify child biomes of other biomes here. Just the root parents.")
    private KList<String> seaBiomes = new KList<>();
    @RegistryListResource(KrudWorldBiome.class)
    @Required
    @ArrayType(min = 1, type = String.class)
    @Desc("A list of root-level biomes in this region. Don't specify child biomes of other biomes here. Just the root parents.")
    private KList<String> shoreBiomes = new KList<>();
    @RegistryListResource(KrudWorldBiome.class)
    @ArrayType(min = 1, type = String.class)
    @Desc("A list of root-level biomes in this region. Don't specify child biomes of other biomes here. Just the root parents.")
    private KList<String> caveBiomes = new KList<>();
    @ArrayType(min = 1, type = KrudWorldDepositGenerator.class)
    @Desc("Define regional deposit generators that add onto the global deposit generators")
    private KList<KrudWorldDepositGenerator> deposits = new KList<>();
    @Desc("The style of rivers")
    private KrudWorldGeneratorStyle riverStyle = NoiseStyle.VASCULAR_THIN.style().zoomed(7.77);
    @Desc("The style of lakes")
    private KrudWorldGeneratorStyle lakeStyle = NoiseStyle.CELLULAR_IRIS_THICK.style();
    @Desc("A color for visualizing this region with a color. I.e. #F13AF5. This will show up on the map.")
    private String color = null;
    @Desc("Collection of ores to be generated")
    @ArrayType(type = KrudWorldOreGenerator.class, min = 1)
    private KList<KrudWorldOreGenerator> ores = new KList<>();

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

    public String getName() {
        return name;
    }

    public KList<KrudWorldObjectPlacement> getSurfaceObjects() {
        return getSurfaceObjectsCache().aquire(() ->
        {
            KList<KrudWorldObjectPlacement> o = getObjects().copy();

            for (KrudWorldObjectPlacement i : o.copy()) {
                if (!i.getCarvingSupport().supportsSurface()) {
                    o.remove(i);
                }
            }

            return o;
        });
    }

    public KList<KrudWorldObjectPlacement> getCarvingObjects() {
        return getCarveObjectsCache().aquire(() ->
        {
            KList<KrudWorldObjectPlacement> o = getObjects().copy();

            for (KrudWorldObjectPlacement i : o.copy()) {
                if (!i.getCarvingSupport().supportsCarving()) {
                    o.remove(i);
                }
            }

            return o;
        });
    }

    public double getBiomeZoom(InferredType t) {
        switch (t) {
            case CAVE:
                return caveBiomeZoom;
            case LAND:
                return landBiomeZoom;
            case SEA:
                return seaBiomeZoom;
            case SHORE:
                return shoreBiomeZoom;
            default:
                break;
        }

        return 1;
    }

    public CNG getShoreHeightGenerator() {
        return shoreHeightGenerator.aquire(() ->
                CNG.signature(new RNG((long) (getName().length() + getLandBiomeZoom() + getLandBiomes().size() + 3458612))));
    }

    public double getShoreHeight(double x, double z) {
        return getShoreHeightGenerator().fitDouble(shoreHeightMin, shoreHeightMax, x / shoreHeightZoom, z / shoreHeightZoom);
    }

    public KSet<String> getAllBiomeIds() {
        KSet<String> names = new KSet<>();
        names.addAll(landBiomes);
        names.addAll(caveBiomes);
        names.addAll(seaBiomes);
        names.addAll(shoreBiomes);

        return names;
    }

    public KList<KrudWorldBiome> getAllBiomes(DataProvider g) {
        KMap<String, KrudWorldBiome> b = new KMap<>();
        KSet<String> names = getAllBiomeIds();

        while (!names.isEmpty()) {
            for (String i : new KList<>(names)) {
                if (b.containsKey(i)) {
                    names.remove(i);
                    continue;
                }

                KrudWorldBiome biome = g.getData().getBiomeLoader().load(i);

                names.remove(i);
                if (biome == null) {
                    continue;
                }

                names.add(biome.getCarvingBiome());
                b.put(biome.getLoadKey(), biome);
                names.addAll(biome.getChildren());
            }
        }

        return b.v();
    }

    public KList<KrudWorldBiome> getBiomes(DataProvider g, InferredType type) {
        if (type.equals(InferredType.LAND)) {
            return getRealLandBiomes(g);
        } else if (type.equals(InferredType.SEA)) {
            return getRealSeaBiomes(g);
        } else if (type.equals(InferredType.SHORE)) {
            return getRealShoreBiomes(g);
        } else if (type.equals(InferredType.CAVE)) {
            return getRealCaveBiomes(g);
        }

        return new KList<>();
    }

    public KList<KrudWorldBiome> getRealCaveBiomes(DataProvider g) {
        return realCaveBiomes.aquire(() ->
        {
            KList<KrudWorldBiome> realCaveBiomes = new KList<>();

            for (String i : getCaveBiomes()) {
                realCaveBiomes.add(g.getData().getBiomeLoader().load(i));
            }

            return realCaveBiomes;
        });
    }

    public KList<KrudWorldBiome> getRealShoreBiomes(DataProvider g) {
        return realShoreBiomes.aquire(() ->
        {
            KList<KrudWorldBiome> realShoreBiomes = new KList<>();

            for (String i : getShoreBiomes()) {
                realShoreBiomes.add(g.getData().getBiomeLoader().load(i));
            }

            return realShoreBiomes;
        });
    }

    public KList<KrudWorldBiome> getRealSeaBiomes(DataProvider g) {
        return realSeaBiomes.aquire(() ->
        {
            KList<KrudWorldBiome> realSeaBiomes = new KList<>();

            for (String i : getSeaBiomes()) {
                realSeaBiomes.add(g.getData().getBiomeLoader().load(i));
            }

            return realSeaBiomes;
        });
    }

    public KList<KrudWorldBiome> getRealLandBiomes(DataProvider g) {
        return realLandBiomes.aquire(() ->
        {
            KList<KrudWorldBiome> realLandBiomes = new KList<>();

            for (String i : getLandBiomes()) {
                realLandBiomes.add(g.getData().getBiomeLoader().load(i));
            }

            return realLandBiomes;
        });
    }

    public KList<KrudWorldBiome> getAllAnyBiomes() {
        KMap<String, KrudWorldBiome> b = new KMap<>();
        KSet<String> names = new KSet<>();
        names.addAll(landBiomes);
        names.addAll(caveBiomes);
        names.addAll(seaBiomes);
        names.addAll(shoreBiomes);

        while (!names.isEmpty()) {
            for (String i : new KList<>(names)) {
                if (b.containsKey(i)) {
                    names.remove(i);
                    continue;
                }

                KrudWorldBiome biome = KrudWorldData.loadAnyBiome(i, getLoader());

                names.remove(i);
                if (biome == null) {
                    continue;
                }

                names.add(biome.getCarvingBiome());
                b.put(biome.getLoadKey(), biome);
                names.addAll(biome.getChildren());
            }
        }

        return b.v();
    }

    public Color getColor(DataProvider dataProvider, RenderType type) {
        return this.cacheColor.aquire(() -> {
            if (this.color == null) {
                Random rand = new Random(getName().hashCode() + getAllBiomeIds().hashCode());
                RandomColor randomColor = new RandomColor(rand);

                KList<KrudWorldBiome> biomes = getRealLandBiomes(dataProvider);

                while (biomes.size() > 0) {
                    int index = rand.nextInt(biomes.size());
                    KrudWorldBiome biome = biomes.get(index);

                    if (biome.getVanillaDerivative() != null) {
                        RandomColor.Color col = VanillaBiomeMap.getColorType(biome.getVanillaDerivative());
                        RandomColor.Luminosity lum = VanillaBiomeMap.getColorLuminosity(biome.getVanillaDerivative());
                        RandomColor.SaturationType sat = VanillaBiomeMap.getColorSaturatiom(biome.getVanillaDerivative());
                        int newColorI = randomColor.randomColor(col, col == RandomColor.Color.MONOCHROME ? RandomColor.SaturationType.MONOCHROME : sat, lum);
                        return new Color(newColorI);
                    }

                    biomes.remove(index);
                }

                KrudWorld.warn("Couldn't find a suitable color for region " + getName());
                return new Color(new RandomColor(rand).randomColor());
            }

            try {
                return Color.decode(this.color);
            } catch (NumberFormatException e) {
                KrudWorld.warn("Could not parse color \"" + this.color + "\" for region " + getName());
                return Color.WHITE;
            }
        });
    }

    public void pickRandomColor(DataProvider data) {

    }

    @Override
    public String getFolderName() {
        return "regions";
    }

    @Override
    public String getTypeName() {
        return "Region";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
