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

package dev.krud.world.util.data;

/**
 * Dimensions
 *
 * @author cyberpwn
 */
public class Dimension {
    private final int width;
    private final int height;
    private final int depth;

    /**
     * Make a dimension
     *
     * @param width  width of this (X)
     * @param height the height (Y)
     * @param depth  the depth (Z)
     */
    public Dimension(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * Make a dimension
     *
     * @param width  width of this (X)
     * @param height the height (Y)
     */
    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
        this.depth = 0;
    }

    /**
     * Get the direction of the flat part of this dimension (null if no thin
     * face)
     *
     * @return the direction of the flat pane or null
     */
    public DimensionFace getPane() {
        if (width == 1) {
            return DimensionFace.X;
        }

        if (height == 1) {
            return DimensionFace.Y;
        }

        if (depth == 1) {
            return DimensionFace.Z;
        }

        return null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }
}