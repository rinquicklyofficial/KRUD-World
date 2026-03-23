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

import lombok.Data;
import org.bukkit.util.Vector;

@Data
public class INode {

    private Vector position;
    private double tension;
    private double bias;
    private double continuity;

    public INode() {
        this(new Vector(0, 0, 0));
    }

    public INode(INode other) {
        this.position = other.position;

        this.tension = other.tension;
        this.bias = other.bias;
        this.continuity = other.continuity;
    }

    public INode(Vector position) {
        this.position = position;
    }
}