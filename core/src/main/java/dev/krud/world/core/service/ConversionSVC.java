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

package dev.krud.world.core.service;

import com.google.gson.Gson;
import dev.krud.world.KrudWorld;
import dev.krud.world.engine.object.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.function.Consumer2;
import dev.krud.world.util.io.Converter;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.nbt.io.NBTUtil;
import dev.krud.world.util.nbt.io.NamedTag;
import dev.krud.world.util.nbt.mca.NBTWorld;
import dev.krud.world.util.nbt.tag.CompoundTag;
import dev.krud.world.util.nbt.tag.IntTag;
import dev.krud.world.util.nbt.tag.ListTag;
import dev.krud.world.util.plugin.KrudWorldService;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.J;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Jigsaw;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConversionSVC implements KrudWorldService {
    private KList<Converter> converters;
    private File folder;

    @Override
    public void onEnable() {
        folder = KrudWorld.instance.getDataFolder("convert");
        converters = new KList<>();

        J.s(() ->
                J.attemptAsync(() ->
                {

                }), 5);
    }

    @Override
    public void onDisable() {

    }

    private String toPoolName(String poolReference) {
        return poolReference.split("\\Q:\\E")[1];
    }

    public void convertStructures(File in, File out, VolmitSender s) {
        KMap<String, KrudWorldJigsawPool> pools = new KMap<>();
        KList<File> roots = new KList<>();
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger at = new AtomicInteger(0);
        File destPools = new File(out.getAbsolutePath() + "/jigsaw-pools");
        destPools.mkdirs();
        findAllNBT(in, (folder, file) -> {
            total.getAndIncrement();
            if (roots.addIfMissing(folder)) {
                String b = in.toURI().relativize(folder.toURI()).getPath();
                if (b.startsWith("/")) {
                    b = b.substring(1);
                }

                if (b.endsWith("/")) {
                    b = b.substring(0, b.length() - 1);
                }

                pools.put(b, new KrudWorldJigsawPool());
            }
        });
        findAllNBT(in, (folder, file) -> {
            at.getAndIncrement();
            String b = in.toURI().relativize(folder.toURI()).getPath();
            if (b.startsWith("/")) {
                b = b.substring(1);
            }

            if (b.endsWith("/")) {
                b = b.substring(0, b.length() - 1);
            }
            KrudWorldJigsawPool jpool = pools.get(b);
            File destObjects = new File(out.getAbsolutePath() + "/objects/" + in.toURI().relativize(folder.toURI()).getPath());
            File destPieces = new File(out.getAbsolutePath() + "/jigsaw-pieces/" + in.toURI().relativize(folder.toURI()).getPath());
            destObjects.mkdirs();
            destPieces.mkdirs();

            try {
                NamedTag tag = NBTUtil.read(file);
                CompoundTag compound = (CompoundTag) tag.getTag();

                if (compound.containsKey("blocks") && compound.containsKey("palette") && compound.containsKey("size")) {
                    String id = in.toURI().relativize(folder.toURI()).getPath() + file.getName().split("\\Q.\\E")[0];
                    @SuppressWarnings("unchecked") ListTag<IntTag> size = (ListTag<IntTag>) compound.getListTag("size");
                    int w = size.get(0).asInt();
                    int h = size.get(1).asInt();
                    int d = size.get(2).asInt();
                    KList<BlockData> palette = new KList<>();
                    @SuppressWarnings("unchecked") ListTag<CompoundTag> paletteList = (ListTag<CompoundTag>) compound.getListTag("palette");
                    for (int i = 0; i < paletteList.size(); i++) {
                        CompoundTag cp = paletteList.get(i);
                        palette.add(NBTWorld.getBlockData(cp));
                    }
                    KrudWorldJigsawPiece piece = new KrudWorldJigsawPiece();
                    KrudWorldObject object = new KrudWorldObject(w, h, d);
                    @SuppressWarnings("unchecked") ListTag<CompoundTag> blockList = (ListTag<CompoundTag>) compound.getListTag("blocks");
                    for (int i = 0; i < blockList.size(); i++) {
                        CompoundTag cp = blockList.get(i);
                        @SuppressWarnings("unchecked") ListTag<IntTag> pos = (ListTag<IntTag>) cp.getListTag("pos");
                        int x = pos.get(0).asInt();
                        int y = pos.get(1).asInt();
                        int z = pos.get(2).asInt();
                        BlockData bd = palette.get(cp.getInt("state")).clone();

                        if (bd.getMaterial().equals(Material.JIGSAW) && cp.containsKey("nbt")) {
                            piece.setObject(in.toURI().relativize(folder.toURI()).getPath() + file.getName().split("\\Q.\\E")[0]);
                            KrudWorldPosition spos = new KrudWorldPosition(object.getSigned(x, y, z));
                            CompoundTag nbt = cp.getCompoundTag("nbt");
                            CompoundTag finalState = new CompoundTag();
                            finalState.putString("Name", nbt.getString("final_state"));
                            BlockData jd = bd.clone();
                            bd = NBTWorld.getBlockData(finalState);
                            String joint = nbt.getString("joint");
                            String pool = nbt.getString("pool");
                            String poolId = toPoolName(pool);
                            String name = nbt.getString("name");
                            String target = nbt.getString("target");
                            pools.computeIfAbsent(poolId, (k) -> new KrudWorldJigsawPool());
                            KrudWorldJigsawPieceConnector connector = new KrudWorldJigsawPieceConnector();
                            connector.setName(name);
                            connector.setTargetName(target);
                            connector.setRotateConnector(false);
                            connector.setPosition(spos);
                            connector.getPools().add(poolId);
                            connector.setDirection(KrudWorldDirection.getDirection(((Jigsaw) jd).getOrientation()));

                            if (target.equals("minecraft:building_entrance")) {
                                connector.setInnerConnector(true);
                            }

                            piece.getConnectors().add(connector);
                        }

                        if (!bd.getMaterial().equals(Material.STRUCTURE_VOID) && !bd.getMaterial().equals(Material.AIR)) {
                            object.setUnsigned(x, y, z, bd);
                        }
                    }

                    jpool.getPieces().addIfMissing(id);
                    object.write(new File(destObjects, file.getName().split("\\Q.\\E")[0] + ".iob"));
                    IO.writeAll(new File(destPieces, file.getName().split("\\Q.\\E")[0] + ".json"), new JSONObject(new Gson().toJson(piece)).toString(4));
                    KrudWorld.info("[Jigsaw]: (" + Form.pc((double) at.get() / (double) total.get(), 0) + ") Exported Piece: " + id);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                KrudWorld.reportError(e);
            }
        });

        for (String i : pools.k()) {
            try {
                IO.writeAll(new File(destPools, i + ".json"), new JSONObject(new Gson().toJson(pools.get(i))).toString(4));
            } catch (IOException e) {
                e.printStackTrace();
                KrudWorld.reportError(e);
            }
        }

        KrudWorld.info("Done! Exported " + Form.f((total.get() * 2) + pools.size()) + " Files!");
    }

    public void findAllNBT(File path, Consumer2<File, File> inFile) {
        if (path == null) {
            return;
        }

        if (path.isFile() && path.getName().endsWith(".nbt")) {
            inFile.accept(path.getParentFile(), path);
            return;
        }

        for (File i : path.listFiles()) {
            if (i.isDirectory()) {
                findAllNBT(i, inFile);
            } else if (i.isFile() && i.getName().endsWith(".nbt")) {
                inFile.accept(path, i);
            }
        }
    }

    public void check(VolmitSender s) {
        int m = 0;
        KrudWorld.instance.getDataFolder("convert");

        for (File i : folder.listFiles()) {
            for (Converter j : converters) {
                if (i.getName().endsWith("." + j.getInExtension())) {
                    File out = new File(folder, i.getName().replaceAll("\\Q." + j.getInExtension() + "\\E", "." + j.getOutExtension()));
                    m++;
                    j.convert(i, out);
                    s.sendMessage("Converted " + i.getName() + " -> " + out.getName());
                }
            }

            if (i.isDirectory() && i.getName().equals("structures")) {
                File f = new File(folder, "jigsaw");

                if (!f.exists()) {
                    s.sendMessage("Converting NBT Structures into KrudWorld Jigsaw Structures...");
                    f.mkdirs();
                    J.a(() -> convertStructures(i, f, s));
                }
            }
        }

        s.sendMessage("Converted " + m + " File" + (m == 1 ? "" : "s"));
    }
}
