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

package dev.krud.world.engine.mantle;

import com.google.common.collect.ImmutableList;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.engine.data.cache.Cache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.IObjectPlacer;
import dev.krud.world.engine.object.KrudWorldGeneratorStyle;
import dev.krud.world.engine.object.KrudWorldPosition;
import dev.krud.world.engine.object.TileData;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.collection.KSet;
import dev.krud.world.util.data.B;
import dev.krud.world.util.data.KrudWorldCustomData;
import dev.krud.world.util.documentation.ChunkCoordinates;
import dev.krud.world.util.function.Function3;
import dev.krud.world.util.mantle.Mantle;
import dev.krud.world.util.mantle.MantleChunk;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.matter.Matter;
import dev.krud.world.util.matter.MatterCavern;
import dev.krud.world.util.matter.TileWrapper;
import dev.krud.world.util.noise.CNG;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Data;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.*;

import static dev.krud.world.engine.mantle.EngineMantle.AIR;

@Data
public class MantleWriter implements IObjectPlacer, AutoCloseable {
    private final EngineMantle engineMantle;
    private final Mantle mantle;
    private final Map<Long, MantleChunk> cachedChunks;
    private final int radius;
    private final int x;
    private final int z;

    public MantleWriter(EngineMantle engineMantle, Mantle mantle, int x, int z, int radius, boolean multicore) {
        this.engineMantle = engineMantle;
        this.mantle = mantle;
        this.radius = radius * 2;
        final int d = this.radius + 1;
        this.cachedChunks = multicore ? new KMap<>(d * d, 0.75f, Math.max(32, Runtime.getRuntime().availableProcessors() * 4)) : new Long2ObjectOpenHashMap<>(d * d);
        this.x = x;
        this.z = z;

        final int parallelism = multicore ? Runtime.getRuntime().availableProcessors() / 2 : 4;
        final var map = multicore ? cachedChunks : new KMap<Long, MantleChunk>(d * d, 1f, parallelism);
        mantle.getChunks(
                x - radius,
                x + radius,
                z - radius,
                z + radius,
                parallelism,
                (i, j, c) -> map.put(Cache.key(i, j), c.use())
        );
        if (!multicore) cachedChunks.putAll(map);
    }

    private static Set<KrudWorldPosition> getBallooned(Set<KrudWorldPosition> vset, double radius) {
        Set<KrudWorldPosition> returnset = new HashSet<>();
        int ceilrad = (int) Math.ceil(radius);
        double r2 = Math.pow(radius, 2);

        for (KrudWorldPosition v : vset) {
            int tipx = v.getX();
            int tipy = v.getY();
            int tipz = v.getZ();

            for (int loopx = tipx - ceilrad; loopx <= tipx + ceilrad; loopx++) {
                for (int loopy = tipy - ceilrad; loopy <= tipy + ceilrad; loopy++) {
                    for (int loopz = tipz - ceilrad; loopz <= tipz + ceilrad; loopz++) {
                        if (hypot(loopx - tipx, loopy - tipy, loopz - tipz) <= r2) {
                            returnset.add(new KrudWorldPosition(loopx, loopy, loopz));
                        }
                    }
                }
            }
        }
        return returnset;
    }

    private static Set<KrudWorldPosition> getHollowed(Set<KrudWorldPosition> vset) {
        Set<KrudWorldPosition> returnset = new KSet<>();
        for (KrudWorldPosition v : vset) {
            double x = v.getX();
            double y = v.getY();
            double z = v.getZ();
            if (!(vset.contains(new KrudWorldPosition(x + 1, y, z))
                    && vset.contains(new KrudWorldPosition(x - 1, y, z))
                    && vset.contains(new KrudWorldPosition(x, y + 1, z))
                    && vset.contains(new KrudWorldPosition(x, y - 1, z))
                    && vset.contains(new KrudWorldPosition(x, y, z + 1))
                    && vset.contains(new KrudWorldPosition(x, y, z - 1)))) {
                returnset.add(v);
            }
        }
        return returnset;
    }

    private static double hypot(double... pars) {
        double sum = 0;
        for (double d : pars) {
            sum += Math.pow(d, 2);
        }
        return sum;
    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }

    public <T> void setDataWarped(int x, int y, int z, T t, RNG rng, KrudWorldData data, KrudWorldGeneratorStyle style) {
        setData((int) Math.round(style.warp(rng, data, x, x, y, -z)),
                (int) Math.round(style.warp(rng, data, y, z, -x, y)),
                (int) Math.round(style.warp(rng, data, z, -y, z, x)), t);
    }

    public <T> void setData(int x, int y, int z, T t) {
        if (t == null) {
            return;
        }

        int cx = x >> 4;
        int cz = z >> 4;

        if (y < 0 || y >= mantle.getWorldHeight()) {
            return;
        }

        MantleChunk chunk = acquireChunk(cx, cz);
        if (chunk == null) return;

        Matter matter = chunk.getOrCreate(y >> 4);
        matter.slice(matter.getClass(t)).set(x & 15, y & 15, z & 15, t);
    }

    public <T> T getData(int x, int y, int z, Class<T> type) {
        int cx = x >> 4;
        int cz = z >> 4;

        if (y < 0 || y >= mantle.getWorldHeight()) {
            return null;
        }

        MantleChunk chunk = acquireChunk(cx, cz);
        if (chunk == null) {
            return null;
        }

        return chunk.getOrCreate(y >> 4)
                .<T>slice(type)
                .get(x & 15, y & 15, z & 15);
    }

    @ChunkCoordinates
    public MantleChunk acquireChunk(int cx, int cz) {
        if (cx < this.x - radius || cx > this.x + radius
                || cz < this.z - radius || cz > this.z + radius) {
            KrudWorld.error("Mantle Writer Accessed chunk out of bounds" + cx + "," + cz);
            return null;
        }
        final Long key = Cache.key(cx, cz);
        MantleChunk chunk = cachedChunks.get(key);
        if (chunk == null) {
            chunk = mantle.getChunk(cx, cz).use();
            var old = cachedChunks.put(key, chunk);
            if (old != null) old.release();
        }
        return chunk;
    }

    @Override
    public int getHighest(int x, int z, KrudWorldData data) {
        return engineMantle.getHighest(x, z, data);
    }

    @Override
    public int getHighest(int x, int z, KrudWorldData data, boolean ignoreFluid) {
        return engineMantle.getHighest(x, z, data, ignoreFluid);
    }

    @Override
    public void set(int x, int y, int z, BlockData d) {
        if (d instanceof KrudWorldCustomData data) {
            setData(x, y, z, data.getBase());
            setData(x, y, z, data.getCustom());
        } else setData(x, y, z, d);
    }

    @Override
    public BlockData get(int x, int y, int z) {
        BlockData block = getData(x, y, z, BlockData.class);
        if (block == null)
            return AIR;
        return block;
    }

    @Override
    public boolean isPreventingDecay() {
        return getEngineMantle().isPreventingDecay();
    }

    @Override
    public boolean isCarved(int x, int y, int z) {
        return getData(x, y, z, MatterCavern.class) != null;
    }

    @Override
    public boolean isSolid(int x, int y, int z) {
        return B.isSolid(get(x, y, z));
    }

    @Override
    public boolean isUnderwater(int x, int z) {
        return getEngineMantle().isUnderwater(x, z);
    }

    @Override
    public int getFluidHeight() {
        return getEngineMantle().getFluidHeight();
    }

    @Override
    public boolean isDebugSmartBore() {
        return getEngineMantle().isDebugSmartBore();
    }

    @Override
    public void setTile(int xx, int yy, int zz, TileData tile) {
        setData(xx, yy, zz, new TileWrapper(tile));
    }

    @Override
    public Engine getEngine() {
        return getEngineMantle().getEngine();
    }

    /**
     * Set a sphere into the mantle
     *
     * @param cx     the center x
     * @param cy     the center y
     * @param cz     the center z
     * @param radius the radius of this sphere
     * @param fill   should it be filled? or just the outer shell?
     * @param data   the data to set
     * @param <T>    the type of data to apply to the mantle
     */
    public <T> void setSphere(int cx, int cy, int cz, double radius, boolean fill, T data) {
        setElipsoid(cx, cy, cz, radius, radius, radius, fill, data);
    }

    public <T> void setElipsoid(int cx, int cy, int cz, double rx, double ry, double rz, boolean fill, T data) {
        setElipsoidFunction(cx, cy, cz, rx, ry, rz, fill, (a, b, c) -> data);
    }

    public <T> void setElipsoidWarped(int cx, int cy, int cz, double rx, double ry, double rz, boolean fill, T data, RNG rng, KrudWorldData idata, KrudWorldGeneratorStyle style) {
        setElipsoidFunctionWarped(cx, cy, cz, rx, ry, rz, fill, (a, b, c) -> data, rng, idata, style);
    }

    /**
     * Set an elipsoid into the mantle
     *
     * @param cx   the center x
     * @param cy   the center y
     * @param cz   the center z
     * @param rx   the x radius
     * @param ry   the y radius
     * @param rz   the z radius
     * @param fill should it be filled or just the outer shell?
     * @param data the data to set
     * @param <T>  the type of data to apply to the mantle
     */
    public <T> void setElipsoidFunction(int cx, int cy, int cz, double rx, double ry, double rz, boolean fill, Function3<Integer, Integer, Integer, T> data) {
        rx += 0.5;
        ry += 0.5;
        rz += 0.5;
        final double invRadiusX = 1 / rx;
        final double invRadiusY = 1 / ry;
        final double invRadiusZ = 1 / rz;
        final int ceilRadiusX = (int) Math.ceil(rx);
        final int ceilRadiusY = (int) Math.ceil(ry);
        final int ceilRadiusZ = (int) Math.ceil(rz);
        double nextXn = 0;

        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break;
                    }

                    if (!fill) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    setData(x + cx, y + cy, z + cz, data.apply(x + cx, y + cy, z + cz));
                    setData(-x + cx, y + cy, z + cz, data.apply(-x + cx, y + cy, z + cz));
                    setData(x + cx, -y + cy, z + cz, data.apply(x + cx, -y + cy, z + cz));
                    setData(x + cx, y + cy, -z + cz, data.apply(x + cx, y + cy, -z + cz));
                    setData(-x + cx, y + cy, -z + cz, data.apply(-x + cx, y + cy, -z + cz));
                    setData(-x + cx, -y + cy, z + cz, data.apply(-x + cx, -y + cy, z + cz));
                    setData(x + cx, -y + cy, -z + cz, data.apply(x + cx, -y + cy, -z + cz));
                    setData(-x + cx, y + cy, -z + cz, data.apply(-x + cx, y + cy, -z + cz));
                    setData(-x + cx, -y + cy, -z + cz, data.apply(-x + cx, -y + cy, -z + cz));
                }
            }
        }
    }

    public <T> void setElipsoidFunctionWarped(int cx, int cy, int cz, double rx, double ry, double rz, boolean fill, Function3<Integer, Integer, Integer, T> data, RNG rng, KrudWorldData idata, KrudWorldGeneratorStyle style) {
        rx += 0.5;
        ry += 0.5;
        rz += 0.5;
        final double invRadiusX = 1 / rx;
        final double invRadiusY = 1 / ry;
        final double invRadiusZ = 1 / rz;
        final int ceilRadiusX = (int) Math.ceil(rx);
        final int ceilRadiusY = (int) Math.ceil(ry);
        final int ceilRadiusZ = (int) Math.ceil(rz);
        double nextXn = 0;

        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break;
                    }

                    if (!fill) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    setDataWarped(x + cx, y + cy, z + cz, data.apply(x + cx, y + cy, z + cz), rng, idata, style);
                    setDataWarped(-x + cx, y + cy, z + cz, data.apply(-x + cx, y + cy, z + cz), rng, idata, style);
                    setDataWarped(x + cx, -y + cy, z + cz, data.apply(x + cx, -y + cy, z + cz), rng, idata, style);
                    setDataWarped(x + cx, y + cy, -z + cz, data.apply(x + cx, y + cy, -z + cz), rng, idata, style);
                    setDataWarped(-x + cx, y + cy, -z + cz, data.apply(-x + cx, y + cy, -z + cz), rng, idata, style);
                    setDataWarped(-x + cx, -y + cy, z + cz, data.apply(-x + cx, -y + cy, z + cz), rng, idata, style);
                    setDataWarped(x + cx, -y + cy, -z + cz, data.apply(x + cx, -y + cy, -z + cz), rng, idata, style);
                    setDataWarped(-x + cx, y + cy, -z + cz, data.apply(-x + cx, y + cy, -z + cz), rng, idata, style);
                    setDataWarped(-x + cx, -y + cy, -z + cz, data.apply(-x + cx, -y + cy, -z + cz), rng, idata, style);
                }
            }
        }
    }

    /**
     * Set a cuboid of data in the mantle
     *
     * @param x1   the min x
     * @param y1   the min y
     * @param z1   the min z
     * @param x2   the max x
     * @param y2   the max y
     * @param z2   the max z
     * @param data the data to set
     * @param <T>  the type of data to apply to the mantle
     */
    public <T> void setCuboid(int x1, int y1, int z1, int x2, int y2, int z2, T data) {
        int j, k;

        for (int i = x1; i <= x2; i++) {
            for (j = x1; j <= x2; j++) {
                for (k = x1; k <= x2; k++) {
                    setData(i, j, k, data);
                }
            }
        }
    }

    /**
     * Set a pyramid of data in the mantle
     *
     * @param cx     the center x
     * @param cy     the base y
     * @param cz     the center z
     * @param data   the data to set
     * @param size   the size of the pyramid (width of base & height)
     * @param filled should it be filled or hollow
     * @param <T>    the type of data to apply to the mantle
     */
    @SuppressWarnings("ConstantConditions")
    public <T> void setPyramid(int cx, int cy, int cz, T data, int size, boolean filled) {
        int height = size;

        for (int y = 0; y <= height; ++y) {
            size--;
            for (int x = 0; x <= size; ++x) {
                for (int z = 0; z <= size; ++z) {
                    if ((filled && z <= size && x <= size) || z == size || x == size) {
                        setData(x + cx, y + cy, z + cz, data);
                        setData(-x + cx, y + cy, z + cz, data);
                        setData(x + cx, y + cy, -z + cz, data);
                        setData(-x + cx, y + cy, -z + cz, data);
                    }
                }
            }
        }
    }

    /**
     * Set a 3d line
     *
     * @param a      the first point
     * @param b      the second point
     * @param radius the radius
     * @param filled hollow or filled?
     * @param data   the data
     * @param <T>    the type of data to apply to the mantle
     */
    public <T> void setLine(KrudWorldPosition a, KrudWorldPosition b, double radius, boolean filled, T data) {
        setLine(ImmutableList.of(a, b), radius, filled, data);
    }

    public <T> void setLine(List<KrudWorldPosition> vectors, double radius, boolean filled, T data) {
        setLineConsumer(vectors, radius, filled, (_x, _y, _z) -> data);
    }

    /**
     * Set lines for points
     *
     * @param vectors the points
     * @param radius  the radius
     * @param filled  hollow or filled?
     * @param data    the data to set
     * @param <T>     the type of data to apply to the mantle
     */
    public <T> void setLineConsumer(List<KrudWorldPosition> vectors, double radius, boolean filled, Function3<Integer, Integer, Integer, T> data) {
        Set<KrudWorldPosition> vset = cleanup(vectors);
        vset = getBallooned(vset, radius);

        if (!filled) {
            vset = getHollowed(vset);
        }

        setConsumer(vset, data);
    }

    /**
     * Set lines for points
     *
     * @param vectors the points
     * @param radius  the radius
     * @param filled  hollow or filled?
     * @param data    the data to set
     * @param <T>     the type of data to apply to the mantle
     */
    public <T> void setNoiseMasked(List<KrudWorldPosition> vectors, double radius, double threshold, CNG shape, Set<KrudWorldPosition> masks, boolean filled, Function3<Integer, Integer, Integer, T> data) {
        Set<KrudWorldPosition> vset = cleanup(vectors);
        vset = masks == null ? getBallooned(vset, radius) : getMasked(vset, masks, radius);
        vset.removeIf(p -> shape.noise(p.getX(), p.getY(), p.getZ()) < threshold);

        if (!filled) {
            vset = getHollowed(vset);
        }

        setConsumer(vset, data);
    }

    private static Set<KrudWorldPosition> getMasked(Set<KrudWorldPosition> vectors, Set<KrudWorldPosition> masks, double radius) {
        Set<KrudWorldPosition> vset = new KSet<>();
        int ceil = (int) Math.ceil(radius);
        double r2 = Math.pow(radius, 2);

        for (KrudWorldPosition v : vectors) {
            int tipX = v.getX();
            int tipY = v.getY();
            int tipZ = v.getZ();

            for (int x = -ceil; x <= ceil; x++) {
                for (int y = -ceil; y <= ceil; y++) {
                    for (int z = -ceil; z <= ceil; z++) {
                        if (hypot(x, y, z) > r2 || !masks.contains(new KrudWorldPosition(x, y, z)))
                            continue;
                        vset.add(new KrudWorldPosition(tipX + x, tipY + y, tipZ + z));
                    }
                }
            }
        }

        return vset;
    }

    private static Set<KrudWorldPosition> cleanup(List<KrudWorldPosition> vectors) {
        Set<KrudWorldPosition> vset = new KSet<>();

        for (int i = 0; vectors.size() != 0 && i < vectors.size() - 1; i++) {
            KrudWorldPosition pos1 = vectors.get(i);
            KrudWorldPosition pos2 = vectors.get(i + 1);
            int x1 = pos1.getX();
            int y1 = pos1.getY();
            int z1 = pos1.getZ();
            int x2 = pos2.getX();
            int y2 = pos2.getY();
            int z2 = pos2.getZ();
            int tipx = x1;
            int tipy = y1;
            int tipz = z1;
            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int dz = Math.abs(z2 - z1);

            if (dx + dy + dz == 0) {
                vset.add(new KrudWorldPosition(tipx, tipy, tipz));
                continue;
            }

            int dMax = Math.max(Math.max(dx, dy), dz);
            if (dMax == dx) {
                for (int domstep = 0; domstep <= dx; domstep++) {
                    tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                    tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dx) * (y2 - y1 > 0 ? 1 : -1));
                    tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dx) * (z2 - z1 > 0 ? 1 : -1));

                    vset.add(new KrudWorldPosition(tipx, tipy, tipz));
                }
            } else if (dMax == dy) {
                for (int domstep = 0; domstep <= dy; domstep++) {
                    tipy = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
                    tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dy) * (x2 - x1 > 0 ? 1 : -1));
                    tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dy) * (z2 - z1 > 0 ? 1 : -1));

                    vset.add(new KrudWorldPosition(tipx, tipy, tipz));
                }
            } else /* if (dMax == dz) */ {
                for (int domstep = 0; domstep <= dz; domstep++) {
                    tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
                    tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dz) * (y2 - y1 > 0 ? 1 : -1));
                    tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dz) * (x2 - x1 > 0 ? 1 : -1));

                    vset.add(new KrudWorldPosition(tipx, tipy, tipz));
                }
            }
        }

        return vset;
    }

    /**
     * Set a cylinder in the mantle
     *
     * @param cx     the center x
     * @param cy     the base y
     * @param cz     the center z
     * @param data   the data to set
     * @param radius the radius
     * @param height the height of the cyl
     * @param filled filled or not
     */
    public <T> void setCylinder(int cx, int cy, int cz, T data, double radius, int height, boolean filled) {
        setCylinder(cx, cy, cz, data, radius, radius, height, filled);
    }

    /**
     * Set a cylinder in the mantle
     *
     * @param cx      the center x
     * @param cy      the base y
     * @param cz      the center z
     * @param data    the data to set
     * @param radiusX the x radius
     * @param radiusZ the z radius
     * @param height  the height of this cyl
     * @param filled  filled or hollow?
     */
    public <T> void setCylinder(int cx, int cy, int cz, T data, double radiusX, double radiusZ, int height, boolean filled) {
        int affected = 0;
        radiusX += 0.5;
        radiusZ += 0.5;

        if (height == 0) {
            return;
        } else if (height < 0) {
            height = -height;
            cy = cy - height;
        }

        if (cy < 0) {
            cy = 0;
        } else if (cy + height - 1 > getMantle().getWorldHeight()) {
            height = getMantle().getWorldHeight() - cy + 1;
        }

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;
        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);
        double nextXn = 0;

        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;
                double distanceSq = lengthSq(xn, zn);

                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }

                    break;
                }

                if (!filled) {
                    if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                        continue;
                    }
                }

                for (int y = 0; y < height; ++y) {
                    setData(cx + x, cy + y, cz + z, data);
                    setData(cx + -x, cy + y, cz + z, data);
                    setData(cx + x, cy + y, cz + -z, data);
                    setData(cx + -x, cy + y, cz + -z, data);
                }
            }
        }
    }

    public <T> void set(KrudWorldPosition pos, T data) {
        try {
            setData(pos.getX(), pos.getY(), pos.getZ(), data);
        } catch (Throwable e) {
            KrudWorld.error("No set? " + data.toString() + " for " + pos.toString());
        }
    }

    public <T> void set(List<KrudWorldPosition> positions, T data) {
        for (KrudWorldPosition i : positions) {
            set(i, data);
        }
    }

    public <T> void set(Set<KrudWorldPosition> positions, T data) {
        for (KrudWorldPosition i : positions) {
            set(i, data);
        }
    }

    public <T> void setConsumer(Set<KrudWorldPosition> positions, Function3<Integer, Integer, Integer, T> data) {
        for (KrudWorldPosition i : positions) {
            set(i, data.apply(i.getX(), i.getY(), i.getZ()));
        }
    }

    public boolean isWithin(Vector pos) {
        return isWithin(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    public boolean isWithin(int x, int y, int z) {
        int cx = x >> 4;
        int cz = z >> 4;

        if (y < 0 || y >= mantle.getWorldHeight()) {
            return false;
        }

        return cx >= this.x - radius && cx <= this.x + radius
                && cz >= this.z - radius && cz <= this.z + radius;
    }

    @Override
    public void close() {
        var iterator = cachedChunks.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().release();
            iterator.remove();
        }
    }
}
