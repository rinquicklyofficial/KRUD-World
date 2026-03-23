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
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class KrudWorldImage extends KrudWorldRegistrant {
    private final BufferedImage image;

    public KrudWorldImage() {
        this(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB));
    }

    public KrudWorldImage(BufferedImage image) {
        this.image = image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getRawValue(int x, int z) {
        if (x >= getWidth() || z >= getHeight() || x < 0 || z < 0) {
            return 0;
        }

        return image.getRGB(x, z);
    }

    public double getValue(KrudWorldImageChannel channel, int x, int z) {
        int color = getRawValue(x, z);

        switch (channel) {
            case RED -> {
                return ((color >> 16) & 0xFF) / 255D;
            }
            case GREEN -> {
                return ((color >> 8) & 0xFF) / 255D;
            }
            case BLUE -> {
                return ((color) & 0xFF) / 255D;
            }
            case SATURATION -> {
                return Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, null)[1];
            }
            case HUE -> {
                return Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, null)[0];
            }
            case BRIGHTNESS -> {
                return Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, null)[2];
            }
            case COMPOSITE_ADD_RGB -> {
                return ((((color >> 16) & 0xFF) / 255D) + (((color >> 8) & 0xFF) / 255D) + (((color) & 0xFF) / 255D)) / 3D;
            }
            case COMPOSITE_MUL_RGB -> {
                return (((color >> 16) & 0xFF) / 255D) * (((color >> 8) & 0xFF) / 255D) * (((color) & 0xFF) / 255D);
            }
            case COMPOSITE_MAX_RGB -> {
                return Math.max(Math.max((((color >> 16) & 0xFF) / 255D), (((color >> 8) & 0xFF) / 255D)), (((color) & 0xFF) / 255D));
            }
            case COMPOSITE_ADD_HSB -> {
                float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, null);
                return (hsb[0] + hsb[1] + hsb[2]) / 3D;
            }
            case COMPOSITE_MUL_HSB -> {
                float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, null);
                return hsb[0] * hsb[1] * hsb[2];
            }
            case COMPOSITE_MAX_HSB -> {
                float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, null);
                return Math.max(hsb[0], Math.max(hsb[1], hsb[2]));
            }
            case RAW -> {
                return color;
            }
        }

        return color;
    }

    @Override
    public String getFolderName() {
        return "images";
    }

    @Override
    public String getTypeName() {
        return "Image";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }

    public void writeDebug(KrudWorldImageChannel channel) {


        try {
            File at = new File(getLoadFile().getParentFile(), "debug-see-" + getLoadFile().getName());
            BufferedImage b = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < getWidth(); i++) {
                for (int j = 0; j < getHeight(); j++) {
                    b.setRGB(i, j, Color.getHSBColor(0, 0, (float) getValue(channel, i, j)).getRGB());
                }
            }
            ImageIO.write(b, "png", at);
            KrudWorld.warn("Debug image written to " + at.getPath() + " for channel " + channel.name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
