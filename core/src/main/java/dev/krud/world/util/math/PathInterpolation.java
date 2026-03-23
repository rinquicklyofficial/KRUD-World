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

package dev.krud.world.util.math;

import org.bukkit.util.Vector;

import java.util.List;

public interface PathInterpolation {

    /**
     * Sets nodes to be used by subsequent calls to
     * {@link #getPosition(double)} and the other methods.
     *
     * @param nodes the nodes
     */
    void setNodes(List<INode> nodes);

    /**
     * Gets the result of f(position).
     *
     * @param position the position to interpolate
     * @return the result
     */
    Vector getPosition(double position);

    /**
     * Gets the result of f'(position).
     *
     * @param position the position to interpolate
     * @return the result
     */
    Vector get1stDerivative(double position);

    /**
     * Gets the result of &int;<sub>a</sub><sup style="position: relative; left: -1ex">b</sup>|f'(t)| dt.<br />
     * That means it calculates the arc length (in meters) between positionA
     * and positionB.
     *
     * @param positionA lower limit
     * @param positionB upper limit
     * @return the arc length
     */
    double arcLength(double positionA, double positionB);

    /**
     * Get the segment position.
     *
     * @param position the position
     * @return the segment position
     */
    int getSegment(double position);

}