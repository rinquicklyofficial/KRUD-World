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
import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.B;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.plugin.VolmitSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Map;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents Block Data")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldBlockData extends KrudWorldRegistrant {
    private final transient AtomicCache<BlockData> blockdata = new AtomicCache<>();
    private final transient AtomicCache<String> realProperties = new AtomicCache<>();
    @RegistryListBlockType
    @Required
    @Desc("The block to use")
    private String block = "air";
    @Desc("Debug this block by printing it to the console when it's used. Must have debug turned on in settings.")
    private boolean debug = false;
    @MinNumber(1)
    @MaxNumber(1000)
    @Desc("The weight is used when this block data is inside of a list of blockdata. A weight of two is just as if you placed two of the same block data values in the same list making it more common when randomly picked.")
    private int weight = 1;
    @Desc("If the block cannot be created on this version, KrudWorld will attempt to use this backup block data instead.")
    private KrudWorldBlockData backup = null;
    @RegistryMapBlockState("block")
    @Desc("Optional properties for this block data such as 'waterlogged': true")
    private KMap<String, Object> data = new KMap<>();
    @Desc("Optional tile data for this block data")
    private KMap<String, Object> tileData = new KMap<>();

    public KrudWorldBlockData(String b) {
        this.block = b;
    }

    public static KrudWorldBlockData from(String j) {
        KrudWorldBlockData b = new KrudWorldBlockData();
        String v = j.toLowerCase().trim();

        if (v.contains("[")) {
            KList<String> props = new KList<>();
            String rp = v.split("\\Q[\\E")[1].replaceAll("\\Q]\\E", "");
            b.setBlock(v.split("\\Q[\\E")[0]);

            if (rp.contains(",")) {
                props.add(rp.split("\\Q,\\E"));
            } else {
                props.add(rp);
            }

            for (String i : props) {
                Object kg = filter(i.split("\\Q=\\E")[1]);
                b.data.put(i.split("\\Q=\\E")[0], kg);
            }
        } else {
            b.setBlock(v);
        }

        return b;
    }

    private static Object filter(String string) {
        if (string.equals("true")) {
            return true;
        }

        if (string.equals("false")) {
            return false;
        }

        try {
            return Integer.parseInt(string);
        } catch (Throwable ignored) {
            // Checks
        }

        try {
            return Double.valueOf(string).intValue();
        } catch (Throwable ignored) {
            // Checks
        }

        return string;
    }

    public String computeProperties(KMap<String, Object> data) {
        if (data.isEmpty()) {
            return "";
        }

        KList<String> r = new KList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            r.add(entry.getKey() + "=" + filter(entry.getValue().toString()));
        }

        return "[" + r.toString(",") + "]";
    }

    public String computeProperties() {
        return computeProperties(getData());
    }

    public BlockData getBlockData(KrudWorldData data) {
        return blockdata.aquire(() ->
        {
            BlockData b = null;

            KrudWorldBlockData customData = data.getBlockLoader().load(getBlock(), false);

            if (customData != null) {
                b = customData.getBlockData(data);

                if (b != null) {
                    b = b.clone();

                    String st = b.getAsString(true);

                    if (st.contains("[")) {
                        st = st.split("\\Q[\\E")[0];
                    }

                    KMap<String, Object> cdata = customData.getData().copy();

                    for (String i : getData().keySet()) {
                        cdata.put(i, getData().get(i));
                    }

                    String sx = keyify(st) + computeProperties(cdata);

                    if (debug) {
                        KrudWorld.debug("Block Data used " + sx + " (CUSTOM)");
                    }

                    BlockData bx = B.get(sx);

                    if (bx != null) {
                        return bx;
                    }

                    if (b != null) {
                        return b;
                    }
                }
            }

            String ss = keyify(getBlock()) + computeProperties();
            b = B.get(ss);

            if (debug) {
                KrudWorld.debug("Block Data used " + ss);
            }

            if (b != null) {
                return b;
            }

            if (backup != null) {
                return backup.getBlockData(data);
            }

            return B.get("AIR");
        });
    }

    public TileData tryGetTile(KrudWorldData data) {
        //TODO Do like a registry thing with the tile data registry. Also update the parsing of data to include **block** entities.
        var type = getBlockData(data).getMaterial();
        if (type == Material.SPAWNER && this.data.containsKey("entitySpawn")) {
            String id = (String) this.data.get("entitySpawn");
            if (tileData == null) tileData = new KMap<>();
            KMap<String, Object> spawnData = (KMap<String, Object>) tileData.computeIfAbsent("SpawnData", k -> new KMap<>());
            KMap<String, Object> entity = (KMap<String, Object>) spawnData.computeIfAbsent("entity", k -> new KMap<>());
            entity.putIfAbsent("id", Identifier.fromString(id).toString());
        }

        if (!INMS.get().hasTile(type) || tileData == null || tileData.isEmpty())
            return null;
        return new TileData(type, this.tileData);
    }

    private String keyify(String dat) {
        if (dat.contains(":")) {
            return dat;
        }

        return "minecraft:" + dat;
    }

    @Override
    public String getFolderName() {
        return "blocks";
    }

    @Override
    public String getTypeName() {
        return "Block";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
