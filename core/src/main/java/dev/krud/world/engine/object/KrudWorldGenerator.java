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

import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.interpolation.KrudWorldInterpolation;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.noise.CellGenerator;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents a composite generator of noise gens")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldGenerator extends KrudWorldRegistrant {
    private final transient AtomicCache<CellGenerator> cellGen = new AtomicCache<>();
    @MinNumber(0.001)
    @Desc("The zoom or frequency.")
    private double zoom = 1;
    @MinNumber(0)
    @Desc("The opacity, essentially a multiplier on the output.")
    private double opacity = 1;
    @Desc("Multiply the compsites instead of adding them")
    private boolean multiplicitive = false;
    @MinNumber(0.001)
    @Desc("The size of the cell fractures")
    private double cellFractureZoom = 1D;
    @MinNumber(0)
    @Desc("Cell Fracture Coordinate Shuffling")
    private double cellFractureShuffle = 12D;
    @Desc("The height of fracture cells. Set to 0 to disable")
    private double cellFractureHeight = 0D;
    @MinNumber(0)
    @MaxNumber(1)
    @Desc("How big are the cells (X,Z) relative to the veins that touch them. Between 0 and 1. 0.1 means thick veins, small cells.")
    private double cellPercentSize = 0.75D;
    @Desc("The offset to shift this noise x")
    private double offsetX = 0;
    @Desc("The offset to shift this noise z")
    private double offsetZ = 0;
    @Required
    @Desc("The seed for this generator")
    private long seed = 1;
    @Required
    @Desc("The interpolator to use when smoothing this generator into other regions & generators")
    private KrudWorldInterpolator interpolator = new KrudWorldInterpolator();
    @MinNumber(0)
    @MaxNumber(8192)
    @Desc("Cliff Height Max. Disable with 0 for min and max")
    private double cliffHeightMax = 0;
    @MinNumber(0)
    @MaxNumber(8192)
    @Desc("Cliff Height Min. Disable with 0 for min and max")
    private double cliffHeightMin = 0;
    @ArrayType(min = 1, type = KrudWorldNoiseGenerator.class)
    @Desc("The list of noise gens this gen contains.")
    private KList<KrudWorldNoiseGenerator> composite = new KList<>();
    @Desc("The noise gen for cliff height.")
    private KrudWorldNoiseGenerator cliffHeightGenerator = new KrudWorldNoiseGenerator();

    public double getMax() {
        return opacity;
    }

    public boolean hasCliffs() {
        return cliffHeightMax > 0;
    }

    public CellGenerator getCellGenerator(long seed) {
        return cellGen.aquire(() -> new CellGenerator(new RNG(seed + 239466)));
    }

    public <T extends IRare> T fitRarity(KList<T> b, long superSeed, double rx, double rz) {
        if (b.size() == 0) {
            return null;
        }

        if (b.size() == 1) {
            return b.get(0);
        }

        KList<T> rarityMapped = new KList<>();
        boolean o = false;
        int max = 1;
        for (T i : b) {
            if (i.getRarity() > max) {
                max = i.getRarity();
            }
        }

        max++;

        for (T i : b) {
            for (int j = 0; j < max - i.getRarity(); j++) {
                //noinspection AssignmentUsedAsCondition
                if (o = !o) {
                    rarityMapped.add(i);
                } else {
                    rarityMapped.add(0, i);
                }
            }
        }

        if (rarityMapped.size() == 1) {
            return rarityMapped.get(0);
        }

        if (rarityMapped.isEmpty()) {
            throw new RuntimeException("BAD RARITY MAP! RELATED TO: " + b.toString(", or possibly "));
        }

        return fit(rarityMapped, superSeed, rx, rz);
    }

    public <T> T fit(T[] v, long superSeed, double rx, double rz) {
        if (v.length == 0) {
            return null;
        }

        if (v.length == 1) {
            return v[0];
        }

        return v[fit(0, v.length - 1, superSeed, rx, rz)];
    }

    public <T> T fit(List<T> v, long superSeed, double rx, double rz) {
        if (v.size() == 0) {
            return null;
        }

        if (v.size() == 1) {
            return v.get(0);
        }

        return v.get(fit(0, v.size() - 1, superSeed, rx, rz));
    }

    public int fit(int min, int max, long superSeed, double rx, double rz) {
        if (min == max) {
            return min;
        }

        double noise = getHeight(rx, rz, superSeed);

        return (int) Math.round(KrudWorldInterpolation.lerp(min, max, noise));
    }

    public int fit(double min, double max, long superSeed, double rx, double rz) {
        if (min == max) {
            return (int) Math.round(min);
        }

        double noise = getHeight(rx, rz, superSeed);

        return (int) Math.round(KrudWorldInterpolation.lerp(min, max, noise));
    }

    public double fitDouble(double min, double max, long superSeed, double rx, double rz) {
        if (min == max) {
            return min;
        }

        double noise = getHeight(rx, rz, superSeed);

        return KrudWorldInterpolation.lerp(min, max, noise);
    }

    public double getHeight(double rx, double rz, long superSeed) {
        return getHeight(rx, 0, rz, superSeed, true);
    }


    public double getHeight(double rx, double ry, double rz, long superSeed) {
        return getHeight(rx, ry, rz, superSeed, false);
    }

    public double getHeight(double rx, double ry, double rz, long superSeed, boolean no3d) {
        if (composite.isEmpty()) {
            return 0;
        }

        int hc = (int) ((cliffHeightMin * 10) + 10 + cliffHeightMax * getSeed() + offsetX + offsetZ);
        double h = multiplicitive ? 1 : 0;
        double tp = 0;

        if (composite.size() == 1) {
            if (multiplicitive) {
                h *= composite.get(0).getNoise(getSeed() + superSeed + hc, (rx + offsetX) / zoom, (rz + offsetZ) / zoom, getLoader());
            } else {
                tp += composite.get(0).getOpacity();
                h += composite.get(0).getNoise(getSeed() + superSeed + hc, (rx + offsetX) / zoom, (rz + offsetZ) / zoom, getLoader());
            }
        } else {
            for (KrudWorldNoiseGenerator i : composite) {
                if (multiplicitive) {
                    h *= i.getNoise(getSeed() + superSeed + hc, (rx + offsetX) / zoom, (rz + offsetZ) / zoom, getLoader());
                } else {
                    tp += i.getOpacity();
                    h += i.getNoise(getSeed() + superSeed + hc, (rx + offsetX) / zoom, (rz + offsetZ) / zoom, getLoader());
                }
            }
        }

        double v = multiplicitive ? h * opacity : (h / tp) * opacity;

        if (Double.isNaN(v)) {
            v = 0;
        }

        v = hasCliffs() ? cliff(rx, rz, v, superSeed + 294596 + hc) : v;
        v = hasCellCracks() ? cell(rx, rz, v, superSeed + 48622 + hc) : v;

        return v;
    }

    public double cell(double rx, double rz, double v, double superSeed) {
        getCellGenerator(getSeed() + 46222).setShuffle(getCellFractureShuffle());
        return getCellGenerator(getSeed() + 46222).getDistance(rx / getCellFractureZoom(), rz / getCellFractureZoom()) > getCellPercentSize() ? (v * getCellFractureHeight()) : v;
    }

    private boolean hasCellCracks() {
        return getCellFractureHeight() != 0;
    }

    public double getCliffHeight(double rx, double rz, double superSeed) {
        int hc = (int) ((cliffHeightMin * 10) + 10 + cliffHeightMax * getSeed() + offsetX + offsetZ);
        double h = cliffHeightGenerator.getNoise((long) (getSeed() + superSeed + hc), (rx + offsetX) / zoom, (rz + offsetZ) / zoom, getLoader());
        return KrudWorldInterpolation.lerp(cliffHeightMin, cliffHeightMax, h);
    }

    public double cliff(double rx, double rz, double v, double superSeed) {
        double cliffHeight = getCliffHeight(rx, rz, superSeed - 34857);
        return (Math.round((v * 255D) / cliffHeight) * cliffHeight) / 255D;
    }

    public KrudWorldGenerator rescale(double scale) {
        zoom /= scale;
        return this;
    }

    public KList<KrudWorldNoiseGenerator> getAllComposites() {
        KList<KrudWorldNoiseGenerator> g = new KList<>();

        for (KrudWorldNoiseGenerator i : composite) {
            g.addAll(i.getAllComposites());
        }

        return g;
    }

    @Override
    public String getFolderName() {
        return "generators";
    }

    @Override
    public String getTypeName() {
        return "Generator";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
