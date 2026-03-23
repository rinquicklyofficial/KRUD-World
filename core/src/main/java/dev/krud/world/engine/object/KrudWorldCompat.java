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

import com.google.gson.Gson;
import dev.krud.world.KrudWorld;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.data.B;
import dev.krud.world.util.io.IO;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.scheduling.J;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.IOException;

@Data
public class KrudWorldCompat {
    private KList<KrudWorldCompatabilityBlockFilter> blockFilters;
    private KList<KrudWorldCompatabilityItemFilter> itemFilters;

    public KrudWorldCompat() {
        blockFilters = getDefaultBlockCompatabilityFilters();
        itemFilters = getDefaultItemCompatabilityFilters();
    }

    public static KrudWorldCompat configured(File f) {
        KrudWorldCompat def = new KrudWorldCompat();
        String defa = new JSONObject(new Gson().toJson(def)).toString(4);
        J.attemptAsync(() -> IO.writeAll(new File(f.getParentFile(), "compat.default.json"), defa));


        if (!f.exists()) {
            J.a(() -> {
                try {
                    IO.writeAll(f, defa);
                } catch (IOException e) {
                    KrudWorld.error("Failed to writeNodeData to compat file");
                    KrudWorld.reportError(e);
                }
            });
        } else {
            // If the file doesn't exist, no additional mappings are present outside default
            // so we shouldn't try getting them
            try {
                KrudWorldCompat rea = new Gson().fromJson(IO.readAll(f), KrudWorldCompat.class);

                for (KrudWorldCompatabilityBlockFilter i : rea.getBlockFilters()) {
                    def.getBlockFilters().add(i);
                }

                for (KrudWorldCompatabilityItemFilter i : rea.getItemFilters()) {
                    def.getItemFilters().add(i);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                KrudWorld.reportError(e);
            }
        }

        return def;
    }

    private static KList<KrudWorldCompatabilityItemFilter> getDefaultItemCompatabilityFilters() {
        KList<KrudWorldCompatabilityItemFilter> filters = new KList<>();

        // Below 1.16
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_HELMET", "DIAMOND_HELMET"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_CHESTPLATE", "DIAMOND_CHESTPLATE"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_BOOTS", "DIAMOND_BOOTS"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_LEGGINGS", "DIAMOND_LEGGINGS"));
        filters.add(new KrudWorldCompatabilityItemFilter("MUSIC_DISC_PIGSTEP", "MUSIC_DISC_FAR"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_SWORD", "DIAMOND_SWORD"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_AXE", "DIAMOND_AXE"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_PICKAXE", "DIAMOND_PICKAXE"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_HOE", "DIAMOND_HOE"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_SHOVEL", "DIAMOND_SHOVEL"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_INGOT", "DIAMOND"));
        filters.add(new KrudWorldCompatabilityItemFilter("PIGLIN_BANNER_PATTERN", "PORKCHOP"));
        filters.add(new KrudWorldCompatabilityItemFilter("NETHERITE_SCRAP", "GOLD_INGOT"));
        filters.add(new KrudWorldCompatabilityItemFilter("WARPED_FUNGUS_ON_A_STICK", "CARROT_ON_A_STICK"));

        // Below 1.15
        filters.add(new KrudWorldCompatabilityItemFilter("HONEY_BOTTLE", "GLASS_BOTTLE"));
        filters.add(new KrudWorldCompatabilityItemFilter("HONEYCOMB", "GLASS"));

        // Below 1.14
        filters.add(new KrudWorldCompatabilityItemFilter("SWEET_BERRIES", "APPLE"));
        filters.add(new KrudWorldCompatabilityItemFilter("SUSPICIOUS_STEW", "MUSHROOM_STEW"));
        filters.add(new KrudWorldCompatabilityItemFilter("BLACK_DYE", "INK_SAC"));
        filters.add(new KrudWorldCompatabilityItemFilter("WHITE_DYE", "BONE_MEAL"));
        filters.add(new KrudWorldCompatabilityItemFilter("BROWN_DYE", "COCOA_BEANS"));
        filters.add(new KrudWorldCompatabilityItemFilter("BLUE_DYE", "LAPIS_LAZULI"));
        filters.add(new KrudWorldCompatabilityItemFilter("CROSSBOW", "BOW"));
        filters.add(new KrudWorldCompatabilityItemFilter("FLOWER_BANNER_PATTERN", "CORNFLOWER"));
        filters.add(new KrudWorldCompatabilityItemFilter("SKULL_BANNER_PATTERN", "BONE"));
        filters.add(new KrudWorldCompatabilityItemFilter("GLOBE_BANNER_PATTERN", "WHEAT_SEEDS"));
        filters.add(new KrudWorldCompatabilityItemFilter("MOJANG_BANNER_PATTERN", "DIRT"));
        filters.add(new KrudWorldCompatabilityItemFilter("CREEPER_BANNER_PATTERN", "CREEPER_HEAD"));

        return filters;
    }

    private static KList<KrudWorldCompatabilityBlockFilter> getDefaultBlockCompatabilityFilters() {
        KList<KrudWorldCompatabilityBlockFilter> filters = new KList<>();

        filters.add(new KrudWorldCompatabilityBlockFilter("CHAIN", "IRON_CHAIN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("GRASS", "SHORT_GRASS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SHORT_GRASS", "GRASS"));

        // Below 1.16
        filters.add(new KrudWorldCompatabilityBlockFilter("WEEPING_VINES", "NETHER_FENCE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WEEPING_VINES_PLANT", "NETHER_FENCE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_WART_BLOCK", "NETHER_WART_BLOCK"));
        filters.add(new KrudWorldCompatabilityBlockFilter("TWISTING_VINES", "BAMBOO"));
        filters.add(new KrudWorldCompatabilityBlockFilter("TWISTING_VINES_PLANT", "BAMBOO"));
        filters.add(new KrudWorldCompatabilityBlockFilter("TARGET", "COBBLESTONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SOUL_SOIL", "SOULSAND"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SOUL_TORCH", "TORCH"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SOUL_LANTERN", "LANTERN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SOUL_FIRE", "FIRE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SOUL_CAMPFIRE", "CAMPFIRE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SHROOMLIGHT", "GLOWSTONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("RESPAWN_ANCHOR", "OBSIDIAN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("NETHER_SPROUTS", "RED_MUSHROOM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("NETHER_GOLD_ORE", "GOLD_ORE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("LODESTONE", "STONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STRIPPED_WARPED_HYPHAE", "BROWN_MUSHROOM_BLOCK"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STRIPPED_CRIMSON_HYPHAE", "RED_MUSHROOM_BLOCK"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_HYPHAE", "MUSHROOM_STEM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_HYPHAE", "RED_MUSHROOM_BLOCK"));
        filters.add(new KrudWorldCompatabilityBlockFilter("GILDED_BLACKSTONE", "COBBLESTONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRYING_OBSIDIAN", "OBSIDIAN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STRIPPED_WARPED_STEM", "MUSHROOM_STEM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STRIPPED_CRIMSON_STEM", "MUSHROOM_STEM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_STEM", "MUSHROOM_STEM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_STEM", "MUSHROOM_STEM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_ROOTS", "RED_MUSHROOM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_ROOTS", "BROWN_MUSHROOM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_PLANKS", "OAK_PLANKS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_PLANKS", "OAK_PLANKS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_NYLIUM", "MYCELIUM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_NYLIUM", "MYCELIUM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_FUNGUS", "BROWN_MUSHROOM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_FUNGUS", "RED_MUSHROOM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRACKED_NETHER_BRICKS", "NETHER_BRICKS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CHISELED_NETHER_BRICKS", "NETHER_BRICKS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("NETHER_FENCE", "LEGACY_NETHER_FENCE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("IRON_CHAIN", "IRON_BARS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("NETHERITE_BLOCK", "QUARTZ_BLOCK"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BLACKSTONE", "COBBLESTONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BASALT", "STONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ANCIENT_DEBRIS", "NETHERRACK"));
        filters.add(new KrudWorldCompatabilityBlockFilter("NETHERRACK", "LEGACY_NETHERRACK"));

        // Below 1.15
        filters.add(new KrudWorldCompatabilityBlockFilter("HONEY_BLOCK", "OAK_LEAVES"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BEEHIVE", "OAK_LEAVES"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BEE_NEST", "OAK_LEAVES"));

        // Below 1.14
        filters.add(new KrudWorldCompatabilityBlockFilter("GRANITE_WALL", "COBBLESTONE_WALL"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BLUE_ICE", "PACKED_ICE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("DIORITE_WALL", "COBBLESTONE_WALL"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ANDESITE_WALL", "COBBLESTONE_WALL"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SWEET_BERRY_BUSH", "GRASS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STONECUTTER", "CRAFTING_TABLE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SANDSTONE_STAIRS", "LEGACY_SANDSTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOOTH_SANDSTONE_STAIRS", "LEGACY_SANDSTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("MOSSY_COBBLESTONE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("MOSSY_STONE_BRICK_STAIRS", "STONE_BRICK_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POLISHED_GRANITE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("GRANITE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POLISHED_DIORITE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("DIORITE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POLISHED_ANDESITE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ANDESITE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STONE_STAIRS", "COBBLESTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("END_STONE_BRICK_STAIRS", "LEGACY_SANDSTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("NETHER_BRICK_STAIRS", "LEGACY_NETHER_BRICK_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("RED_NETHER_BRICK_STAIRS", "NETHER_BRICK_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOOTH_QUARTZ_STAIRS", "LEGACY_QUARTZ_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("QUARTZ_STAIRS", "LEGACY_QUARTZ_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("RED_SANDSTONE_STAIRS", "LEGACY_RED_SANDSTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOOTH_RED_SANDSTONE_STAIRS", "LEGACY_RED_SANDSTONE_STAIRS"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STONE_SLAB", "SMOOTH_STONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOKER", "FURNACE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMITHING_TABLE", "CRAFTING_TABLE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("END_STONE_BRICK_SLAB", "SANDSTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("RED_NETHER_BRICK_SLAB", "NETHER_BRICK_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOOTH_QUARTZ_SLAB", "QUARTZ_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CUT_SANDSTONE_SLAB", "SANDSTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CUT_RED_SANDSTONE_SLAB", "RED_SANDSTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOOTH_RED_SANDSTONE_SLAB", "RED_SANDSTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SMOOTH_SANDSTONE_SLAB", "SANDSTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("MOSSY_COBBLESTONE_SLAB", "COBBLESTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("MOSSY_STONE_BRICK_SLAB", "STONE_BRICK_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("STONE_SLAB", "SMOOTH_STONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ANDESITE_SLAB", "COBBLESTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ANDESITE_SLAB", "COBBLESTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("DIORITE_SLAB", "COBBLESTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("GRANITE_SLAB", "COBBLESTONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POLISHED_ANDESITE_SLAB", "SMOOTH_STONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POLISHED_DIORITE_SLAB", "SMOOTH_STONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POLISHED_GRANITE_SLAB", "SMOOTH_STONE_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("WARPED_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SPRUCE_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SPRUCE_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("OAK_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("OAK_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("JUNGLE_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("JUNGLE_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("DARK_OAK_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("DARK_OAK_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CRIMSON_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BIRCH_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BIRCH_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ACACIA_WALL_SIGN", "LEGACY_WALL_SIGN"));
        filters.add(new KrudWorldCompatabilityBlockFilter("ACACIA_SIGN", "LEGACY_SIGN_POST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("SCAFFOLDING", "BIRCH_FENCE"));
        //filters.add(new KrudWorldCompatabilityBlockFilter("LOOM", "LOOM"));
        filters.add(new KrudWorldCompatabilityBlockFilter("LECTERN", "BOOKSHELF"));
        filters.add(new KrudWorldCompatabilityBlockFilter("LANTERN", "REDSTONE_LAMP"));
        filters.add(new KrudWorldCompatabilityBlockFilter("JIGSAW", "AIR"));
        filters.add(new KrudWorldCompatabilityBlockFilter("GRINDSTONE", "COBBLESTONE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("FLETCHING_TABLE", "CRAFTING_TABLE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("COMPOSTER", "CHEST"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CARTOGRAPHY_TABLE", "CRAFTING_TABLE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("CAMPFIRE", "DARK_OAK_SLAB"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BLAST_FURNACE", "FURNACE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BELL", "REDSTONE_LAMP"));
        filters.add(new KrudWorldCompatabilityBlockFilter("minecraft:barrel[facing=south]", "minecraft:hay_bale[axis=z]", true));
        filters.add(new KrudWorldCompatabilityBlockFilter("minecraft:barrel[facing=north]", "minecraft:hay_bale[axis=z]", true));
        filters.add(new KrudWorldCompatabilityBlockFilter("minecraft:barrel[facing=east]", "minecraft:hay_bale[axis=x]", true));
        filters.add(new KrudWorldCompatabilityBlockFilter("minecraft:barrel[facing=west]", "minecraft:hay_bale[axis=x]", true));
        filters.add(new KrudWorldCompatabilityBlockFilter("minecraft:barrel[facing=up]", "minecraft:hay_bale[axis=y]", true));
        filters.add(new KrudWorldCompatabilityBlockFilter("minecraft:barrel[facing=down]", "minecraft:hay_bale[axis=y]", true));
        filters.add(new KrudWorldCompatabilityBlockFilter("BAMBOO", "BIRCH_FENCE"));
        filters.add(new KrudWorldCompatabilityBlockFilter("BAMBOO_SAPLING", "BIRCH_SAPLING"));
        filters.add(new KrudWorldCompatabilityBlockFilter("POTTED_BAMBOO", "POTTED_BIRCH_SAPLING"));

        return filters;
    }

    public BlockData getBlock(String n) {
        String buf = n;
        int err = 16;

        BlockData tx = B.getOrNull(buf, false);

        if (tx != null) {
            return tx;
        }

        searching:
        while (true) {
            if (err-- <= 0) {
                KrudWorld.error("Can't find block data for " + n);
                return B.getNoCompat("STONE");
            }
            String m = buf;
            if (m.contains("[")) {
                m = m.split("\\Q[\\E")[0];
            }
            if (m.contains(":")) {
                m = m.split("\\Q:\\E", 2)[1];
            }

            for (KrudWorldCompatabilityBlockFilter i : blockFilters) {
                if (i.getWhen().equalsIgnoreCase(i.isExact() ? buf : m)) {
                    BlockData b = i.getReplace();

                    if (b != null) {
                        return b;
                    }

                    buf = i.getSupplement();
                    continue searching;
                }
            }

            KrudWorld.error("Can't find block data for " + n);
            return B.getNoCompat("STONE");
        }
    }

    public Material getItem(String n) {
        String buf = n;
        int err = 16;
        Material txf = B.getMaterialOrNull(buf);

        if (txf != null) {
            return txf;
        }

        int nomore = 64;

        searching:
        while (true) {
            if (nomore < 0) {
                return B.getMaterial("STONE");
            }

            nomore--;
            if (err-- <= 0) {
                break;
            }

            for (KrudWorldCompatabilityItemFilter i : itemFilters) {
                if (i.getWhen().equalsIgnoreCase(buf)) {
                    Material b = i.getReplace();

                    if (b != null) {
                        return b;
                    }

                    buf = i.getSupplement();
                    continue searching;
                }
            }

            break;
        }

        buf = n;
        BlockData tx = B.getOrNull(buf, false);

        if (tx != null) {
            return tx.getMaterial();
        }
        nomore = 64;

        searching:
        while (true) {
            if (nomore < 0) {
                return B.getMaterial("STONE");
            }

            nomore--;

            if (err-- <= 0) {
                return B.getMaterial("STONE");
            }

            for (KrudWorldCompatabilityBlockFilter i : blockFilters) {
                if (i.getWhen().equalsIgnoreCase(buf)) {
                    BlockData b = i.getReplace();

                    if (b != null) {
                        return b.getMaterial();
                    }

                    buf = i.getSupplement();
                    continue searching;
                }
            }

            return B.getMaterial("STONE");
        }
    }
}
