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
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Wall;
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Snippet("object-rotator")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Configures rotation for iris")
@Data
public class KrudWorldObjectRotation {
    private static final List<BlockFace> WALL_FACES = List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);

    @Desc("If this rotator is enabled or not")
    private boolean enabled = true;

    @Desc("The x axis rotation")
    private KrudWorldAxisRotationClamp xAxis = new KrudWorldAxisRotationClamp();

    @Desc("The y axis rotation")
    private KrudWorldAxisRotationClamp yAxis = new KrudWorldAxisRotationClamp(true, false, 0, 0, 90);

    @Desc("The z axis rotation")
    private KrudWorldAxisRotationClamp zAxis = new KrudWorldAxisRotationClamp();

    public static KrudWorldObjectRotation of(double x, double y, double z) {
        KrudWorldObjectRotation rt = new KrudWorldObjectRotation();
        KrudWorldAxisRotationClamp rtx = new KrudWorldAxisRotationClamp();
        KrudWorldAxisRotationClamp rty = new KrudWorldAxisRotationClamp();
        KrudWorldAxisRotationClamp rtz = new KrudWorldAxisRotationClamp();
        rt.setEnabled(x != 0 || y != 0 || z != 0);
        rt.setXAxis(rtx);
        rt.setYAxis(rty);
        rt.setZAxis(rtz);
        rtx.setEnabled(x != 0);
        rty.setEnabled(y != 0);
        rtz.setEnabled(z != 0);
        rtx.setInterval(90);
        rty.setInterval(90);
        rtz.setInterval(90);
        rtx.minMax(x);
        rty.minMax(y);
        rtz.minMax(z);

        return rt;
    }

    public double getYRotation(int spin) {
        return getRotation(spin, yAxis);
    }

    public double getXRotation(int spin) {
        return getRotation(spin, xAxis);
    }

    public double getZRotation(int spin) {
        return getRotation(spin, zAxis);
    }

    public KrudWorldObject rotateCopy(KrudWorldObject e) {
        if (e == null) {
            return null;
        }

        return e.rotateCopy(this);
    }

    public KrudWorldJigsawPiece rotateCopy(KrudWorldJigsawPiece v, KrudWorldPosition offset) {
        KrudWorldJigsawPiece piece = v.copy();
        for (KrudWorldJigsawPieceConnector i : piece.getConnectors()) {
            i.setPosition(rotate(i.getPosition()).add(offset));
            i.setDirection(rotate(i.getDirection()));
        }
        try {
            var translate = piece.getPlacementOptions().getTranslate();
            var pos = rotate(new KrudWorldPosition(translate.getX(), translate.getY(), translate.getZ())).add(offset);
            translate.setX(pos.getX()).setY(pos.getY()).setZ(pos.getZ());
        } catch (NullPointerException ignored) {}

        return piece;
    }

    public BlockVector rotate(BlockVector direction) {
        return rotate(direction, 0, 0, 0);
    }

    public KrudWorldDirection rotate(KrudWorldDirection direction) {
        BlockVector v = rotate(direction.toVector().toBlockVector());
        return KrudWorldDirection.closest(v);
    }

    public double getRotation(int spin, KrudWorldAxisRotationClamp clamp) {
        if (!enabled) {
            return 0;
        }

        if (!clamp.isEnabled()) {
            return 0;
        }

        return clamp.getRadians(spin);
    }

    public BlockFace getFace(BlockVector v) {
        int x = (int) Math.round(v.getX());
        int y = (int) Math.round(v.getY());
        int z = (int) Math.round(v.getZ());

        if (x == 0 && z == -1) {
            return BlockFace.NORTH;
        }

        if (x == 0 && z == 1) {
            return BlockFace.SOUTH;
        }

        if (x == 1 && z == 0) {
            return BlockFace.EAST;
        }

        if (x == -1 && z == 0) {
            return BlockFace.WEST;
        }

        if (y > 0) {
            return BlockFace.UP;
        }

        if (y < 0) {
            return BlockFace.DOWN;
        }

        return BlockFace.SOUTH;
    }

    public BlockFace getHexFace(BlockVector v) {
        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();

        if (x == 0 && z == -1) return BlockFace.NORTH;
        if (x == 1 && z == -2) return BlockFace.NORTH_NORTH_EAST;
        if (x == 1 && z == -1) return BlockFace.NORTH_EAST;
        if (x == 2 && z == -1) return BlockFace.EAST_NORTH_EAST;
        if (x == 1 && z == 0) return BlockFace.EAST;
        if (x == 2 && z == 1) return BlockFace.EAST_SOUTH_EAST;
        if (x == 1 && z == 1) return BlockFace.SOUTH_EAST;
        if (x == 1 && z == 2) return BlockFace.SOUTH_SOUTH_EAST;
        if (x == 0 && z == 1) return BlockFace.SOUTH;
        if (x == -1 && z == 2) return BlockFace.SOUTH_SOUTH_WEST;
        if (x == -1 && z == 1) return BlockFace.SOUTH_WEST;
        if (x == -2 && z == 1) return BlockFace.WEST_SOUTH_WEST;
        if (x == -1 && z == 0) return BlockFace.WEST;
        if (x == -2 && z == -1) return BlockFace.WEST_NORTH_WEST;
        if (x == -1 && z == -1) return BlockFace.NORTH_WEST;
        if (x == -1 && z == -2) return BlockFace.NORTH_NORTH_WEST;

        if (y > 0) {
            return BlockFace.UP;
        }

        if (y < 0) {
            return BlockFace.DOWN;
        }

        return BlockFace.SOUTH;
    }

    public BlockFace faceForAxis(Axis axis) {
        return switch (axis) {
            case X -> BlockFace.EAST;
            case Y -> BlockFace.UP;
            case Z -> BlockFace.NORTH;
        };

    }

    public Axis axisFor(BlockFace f) {
        return switch (f) {
            case NORTH, SOUTH -> Axis.Z;
            case EAST, WEST -> Axis.X;
            default -> Axis.Y;
        };

    }

    public Axis axisFor2D(BlockFace f) {
        return switch (f) {
            case EAST, WEST, UP, DOWN -> Axis.X;
            default -> Axis.Z;
        };

    }

    public BlockData rotate(BlockData dd, int spinxx, int spinyy, int spinzz) {
        BlockData d = dd;
        try {
            int spinx = (int) (90D * (Math.ceil(Math.abs((spinxx % 360D) / 90D))));
            int spiny = (int) (90D * (Math.ceil(Math.abs((spinyy % 360D) / 90D))));
            int spinz = (int) (90D * (Math.ceil(Math.abs((spinzz % 360D) / 90D))));

            if (!canRotate()) {
                return d;
            }

            if (d instanceof Directional g) {
                BlockFace f = g.getFacing();
                BlockVector bv = new BlockVector(f.getModX(), f.getModY(), f.getModZ());
                bv = rotate(bv.clone(), spinx, spiny, spinz);
                BlockFace t = getFace(bv);

                if (g.getFaces().contains(t)) {
                    g.setFacing(t);
                } else if (!g.getMaterial().isSolid()) {
                    d = null;
                }
            } else if (d instanceof Rotatable g) {
                BlockFace f = g.getRotation();

                BlockVector bv = new BlockVector(f.getModX(), 0, f.getModZ());
                bv = rotate(bv.clone(), spinx, spiny, spinz);
                BlockFace face = getHexFace(bv);

                g.setRotation(face);

            } else if (d instanceof Orientable g) {
                BlockFace f = getFace(g.getAxis());
                BlockVector bv = new BlockVector(f.getModX(), f.getModY(), f.getModZ());
                bv = rotate(bv.clone(), spinx, spiny, spinz);
                Axis a = getAxis(bv);

                if (!a.equals(g.getAxis()) && g.getAxes().contains(a)) {
                    g.setAxis(a);
                }
            } else if (d instanceof MultipleFacing g) {
                List<BlockFace> faces = new KList<>();

                for (BlockFace i : g.getFaces()) {
                    BlockVector bv = new BlockVector(i.getModX(), i.getModY(), i.getModZ());
                    bv = rotate(bv.clone(), spinx, spiny, spinz);
                    BlockFace r = getFace(bv);

                    if (g.getAllowedFaces().contains(r)) {
                        faces.add(r);
                    }
                }

                for (BlockFace i : g.getFaces()) {
                    g.setFace(i, false);
                }

                for (BlockFace i : faces) {
                    g.setFace(i, true);
                }
            } else if (d instanceof Wall wall) {
                KMap<BlockFace, Wall.Height> faces = new KMap<>();

                for (BlockFace i : WALL_FACES) {
                    Wall.Height h = wall.getHeight(i);
                    BlockVector bv = new BlockVector(i.getModX(), i.getModY(), i.getModZ());
                    bv = rotate(bv.clone(), spinx, spiny, spinz);
                    BlockFace r = getFace(bv);
                    if (WALL_FACES.contains(r)) {
                        faces.put(r, h);
                    }
                }

                for (BlockFace i : WALL_FACES) {
                    wall.setHeight(i, faces.getOrDefault(i, Wall.Height.NONE));
                }
            } else if (d instanceof RedstoneWire wire) {
                Map<BlockFace, RedstoneWire.Connection> faces = new HashMap<>();

                var allowed = wire.getAllowedFaces();
                for (BlockFace i : allowed) {
                    RedstoneWire.Connection connection = wire.getFace(i);
                    BlockVector bv = new BlockVector(i.getModX(), i.getModY(), i.getModZ());
                    bv = rotate(bv.clone(), spinx, spiny, spinz);
                    BlockFace r = getFace(bv);
                    if (allowed.contains(r))
                        faces.put(r, connection);
                }

                for (BlockFace i : allowed) {
                    wire.setFace(i, faces.getOrDefault(i, RedstoneWire.Connection.NONE));
                }
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);

        }

        return d;
    }

    public Axis getAxis(BlockVector v) {
        if (Math.abs(v.getBlockX()) > Math.max(Math.abs(v.getBlockY()), Math.abs(v.getBlockZ()))) {
            return Axis.X;
        }

        if (Math.abs(v.getBlockY()) > Math.max(Math.abs(v.getBlockX()), Math.abs(v.getBlockZ()))) {
            return Axis.Y;
        }

        if (Math.abs(v.getBlockZ()) > Math.max(Math.abs(v.getBlockX()), Math.abs(v.getBlockY()))) {
            return Axis.Z;
        }

        return Axis.Y;
    }

    private BlockFace getFace(Axis axis) {
        return switch (axis) {
            case X -> BlockFace.EAST;
            case Y -> BlockFace.UP;
            case Z -> BlockFace.SOUTH;
        };
    }

    public KrudWorldPosition rotate(KrudWorldPosition b) {
        return rotate(b, 0, 0, 0);
    }

    public KrudWorldPosition rotate(KrudWorldPosition b, int spinx, int spiny, int spinz) {
        return new KrudWorldPosition(rotate(new BlockVector(b.getX(), b.getY(), b.getZ()), spinx, spiny, spinz));
    }

    public BlockVector rotate(BlockVector b, int spinx, int spiny, int spinz) {
        if (!canRotate()) {
            return b;
        }

        BlockVector v = b.clone();

        if (canRotateX()) {
            if (getXAxis().isLocked()) {
                if (Math.abs(getXAxis().getMax()) % 360D == 180D) {
                    v.setZ(-v.getZ());
                    v.setY(-v.getY());
                } else if (getXAxis().getMax() % 360D == 90D || getXAxis().getMax() % 360D == -270D) {
                    double z = v.getZ();
                    v.setZ(v.getY());
                    v.setY(-z);
                } else if (getXAxis().getMax() == -90D || getXAxis().getMax() % 360D == 270D) {
                    double z = v.getZ();
                    v.setZ(-v.getY());
                    v.setY(z);
                } else {
                    v.rotateAroundX(getXRotation(spinx));
                }
            } else {
                v.rotateAroundX(getXRotation(spinx));
            }
        }

        if (canRotateZ()) {
            if (getZAxis().isLocked()) {
                if (Math.abs(getZAxis().getMax()) % 360D == 180D) {
                    v.setY(-v.getY());
                    v.setX(-v.getX());
                } else if (getZAxis().getMax() % 360D == 90D || getZAxis().getMax() % 360D == -270D) {
                    double y = v.getY();
                    v.setY(v.getX());
                    v.setX(-y);
                } else if (getZAxis().getMax() == -90D || getZAxis().getMax() % 360D == 270D) {
                    double y = v.getY();
                    v.setY(-v.getX());
                    v.setX(y);
                } else {
                    v.rotateAroundZ(getZRotation(spinz));
                }
            } else {
                v.rotateAroundY(getZRotation(spinz));
            }
        }

        if (canRotateY()) {
            if (getYAxis().isLocked()) {
                if (Math.abs(getYAxis().getMax()) % 360D == 180D) {
                    v.setX(-v.getX());
                    v.setZ(-v.getZ());
                } else if (getYAxis().getMax() % 360D == 90D || getYAxis().getMax() % 360D == -270D) {
                    double x = v.getX();
                    v.setX(v.getZ());
                    v.setZ(-x);
                } else if (getYAxis().getMax() == -90D || getYAxis().getMax() % 360D == 270D) {
                    double x = v.getX();
                    v.setX(-v.getZ());
                    v.setZ(x);
                } else {
                    v.rotateAroundY(getYRotation(spiny));
                }
            } else {
                v.rotateAroundY(getYRotation(spiny));
            }
        }

        return v;
    }

    public boolean canRotateX() {
        return enabled && xAxis.isEnabled();
    }

    public boolean canRotateY() {
        return enabled && yAxis.isEnabled();
    }

    public boolean canRotateZ() {
        return enabled && zAxis.isEnabled();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canRotate() {
        return canRotateX() || canRotateY() || canRotateZ();
    }
}
