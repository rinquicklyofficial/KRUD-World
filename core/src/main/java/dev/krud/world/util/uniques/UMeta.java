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

package dev.krud.world.util.uniques;

import com.google.gson.GsonBuilder;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.io.IO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Data
@NoArgsConstructor
public class UMeta {
    private transient BufferedImage image;
    private KMap<String, UFeatureMeta> features;
    private long id;
    private double time;
    private int width;
    private int height;

    public void registerFeature(String key, UFeatureMeta feature) {
        if (features == null) {
            features = new KMap<>();
        }

        features.put(key, feature);
    }

    public void export(File destination) throws IOException {

        for (String i : features.k()) {
            if (features.get(i).isEmpty()) {
                features.remove(i);
            }
        }

        width = image.getWidth();
        height = image.getHeight();
        ImageIO.write(image, "PNG", destination);
        IO.writeAll(new File(destination.getParentFile(), destination.getName() + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(this));
    }
}
