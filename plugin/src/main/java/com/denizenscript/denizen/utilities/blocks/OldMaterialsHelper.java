package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.objects.MaterialTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for special materials used prior to 1.13
 */
public class OldMaterialsHelper {

    /////////////////
    // MaterialTag Varieties
    ///////////////

    // MaterialTag 'extra materials' for making 'data variety' materials easier to work with. Register materials
    // that aren't included in the bukkit Material enum here to make lookup easier.

    public enum MaterialTags {
        WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL,
        LIME_WOOL, PINK_WOOL, GRAY_WOOL, LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL,
        GREEN_WOOL, RED_WOOL, BLACK_WOOL, WHITE_CARPET, ORANGE_CARPET, MAGENTA_CARPET, LIGHT_BLUE_CARPET,
        YELLOW_CARPET, LIME_CARPET, PINK_CARPET, GRAY_CARPET, LIGHT_GRAY_CARPET, CYAN_CARPET, PURPLE_CARPET,
        BLUE_CARPET, BROWN_CARPET, GREEN_CARPET, RED_CARPET, BLACK_CARPET, WHITE_CLAY, ORANGE_CLAY,
        MAGENTA_CLAY, LIGHT_BLUE_CLAY, YELLOW_CLAY, LIME_CLAY, PINK_CLAY, GRAY_CLAY, LIGHT_GRAY_CLAY,
        CYAN_CLAY, PURPLE_CLAY, BLUE_CLAY, BROWN_CLAY, GREEN_CLAY, RED_CLAY, BLACK_CLAY, WHITE_STAINED_GLASS,
        ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS, YELLOW_STAINED_GLASS,
        LIME_STAINED_GLASS, PINK_STAINED_GLASS, GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS,
        CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS, BLUE_STAINED_GLASS, BROWN_STAINED_GLASS,
        GREEN_STAINED_GLASS, RED_STAINED_GLASS, BLACK_STAINED_GLASS, WHITE_STAINED_GLASS_PANE,
        ORANGE_STAINED_GLASS_PANE, MAGENTA_STAINED_GLASS_PANE, LIGHT_BLUE_STAINED_GLASS_PANE,
        YELLOW_STAINED_GLASS_PANE, LIME_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE, GRAY_STAINED_GLASS_PANE,
        LIGHT_GRAY_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE, BLUE_STAINED_GLASS_PANE,
        BROWN_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, RED_STAINED_GLASS_PANE, BLACK_STAINED_GLASS_PANE,
        CHARCOAL, OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARKOAK_PLANKS,
        OAK_SAPLING, SPRUCE_SAPLING, BIRCH_SAPLING, JUNGLE_SAPLING, ACACIA_SAPLING, DARKOAK_SAPLING,
        OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARKOAK_LEAVES,
        PLACED_OAK_LEAVES, PLACED_SPRUCE_LEAVES, PLACED_BIRCH_LEAVES, PLACED_JUNGLE_LEAVES,
        PLACED_ACACIA_LEAVES, PLACED_DARKOAK_LEAVES, POPPY, BLUE_ORCHID, ALLIUM,
        AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, SUNFLOWER, LILAC,
        DOUBLE_TALLGRASS, ROSE_BUSH, LARGE_FERN, PEONY, OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG,
        ACACIA_LOG, DARKOAK_LOG, CHISELED_SANDSTONE, SMOOTH_SANDSTONE, STONE_BRICK,
        MOSSY_STONE_BRICK, CRACKED_STONE_BRICK, CHISELED_STONE_BRICK, INK, RED_DYE, GREEN_DYE, COCOA_BEANS,
        LAPIS_LAZULI, PURPLE_DYE, CYAN_DYE, LIGHT_GRAY_DYE, GRAY_DYE, PINK_DYE, LIME_DYE, YELLOW_DYE,
        LIGHT_BLUE_DYE, MAGENTA_DYE, ORANGE_DYE, BONE_MEAL, TALL_GRASS, FERN, SHRUB, EMPTY_POT, POTTED_POPPY,
        POTTED_DAISY, POTTED_OAK_SAPLING, POTTED_SPRUCE_SAPLING, POTTED_BIRCH_SAPLING, POTTED_JUNGLE_SAPLING,
        POTTED_RED_MUSHROOM, POTTED_BROWN_MUSHROOM, POTTED_CACTUS, POTTED_SHRUB, POTTED_FERN, POTTED_ACACIA_SAPLING,
        POTTED_DARKOAK_SAPLING, SKELETON_SKULL, WITHERSKELETON_SKULL, ZOMBIE_SKULL, HUMAN_SKULL, CREEPER_SKULL,
        RED_SAND, MOSSY_COBBLE_WALL, CHISELED_QUARTZ_BLOCK, PILLAR_QUARTZ_BLOCK, PILLAR_QUARTZ_BLOCK_EAST,
        PILLAR_QUARTZ_BLOCK_NORTH, OAK_LOG_EAST, OAK_LOG_NORTH, OAK_LOG_BALL, SPRUCE_LOG_EAST, SPRUCE_LOG_NORTH,
        SPRUCE_LOG_BALL, BIRCH_LOG_EAST, BIRCH_LOG_NORTH, BIRCH_LOG_BALL, JUNGLE_LOG_EAST, JUNGLE_LOG_NORTH,
        JUNGLE_LOG_BALL, ACACIA_LOG_EAST, ACACIA_LOG_NORTH, ACACIA_LOG_BALL, DARKOAK_LOG_EAST,
        DARKOAK_LOG_NORTH, DARKOAK_LOG_BALL, DOUBLEPLANT_TOP, STONE_SLAB, SANDSTONE_SLAB, WOODEN_SLAB,
        COBBLESTONE_SLAB, BRICKS_SLAB, STONEBRICKS_SLAB, NETHERBRICK_SLAB, QUARTZ_SLAB, OAK_WOOD_SLAB,
        SPRUCE_WOOD_SLAB, BIRCH_WOOD_SLAB, JUNGLE_WOOD_SLAB, ACACIA_WOOD_SLAB, DARKOAK_WOOD_SLAB, STONE_SLAB_UP,
        SANDSTONE_SLAB_UP, WOODEN_SLAB_UP, COBBLESTONE_SLAB_UP, BRICKS_SLAB_UP, STONEBRICKS_SLAB_UP,
        NETHERBRICK_SLAB_UP, QUARTZ_SLAB_UP, OAK_WOOD_SLAB_UP, SPRUCE_WOOD_SLAB_UP, BIRCH_WOOD_SLAB_UP,
        JUNGLE_WOOD_SLAB_UP, ACACIA_WOOD_SLAB_UP, DARKOAK_WOOD_SLAB_UP, STONE_DOUBLESLAB, SANDSTONE_DOUBLESLAB,
        WOODEN_DOUBLESLAB, COBBLESTONE_DOUBLESLAB, BRICKS_DOUBLESLAB, STONEBRICKS_DOUBLESLAB, NETHERBRICK_DOUBLESLAB,
        QUARTZ_DOUBLESLAB, OAK_WOOD_DOUBLESLAB, SPRUCE_WOOD_DOUBLESLAB, BIRCH_WOOD_DOUBLESLAB, JUNGLE_WOOD_DOUBLESLAB,
        ACACIA_WOOD_DOUBLESLAB, DARKOAK_WOOD_DOUBLESLAB, SKELETON_EGG, RAW_FISH, RAW_SALMON, RAW_CLOWNFISH,
        RAW_PUFFERFISH, COOKED_FISH, COOKED_SALMON, COOKED_CLOWNFISH, COOKED_PUFFERFISH,
        GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ANDESITE, COARSE_DIRT, PODZOL,
        WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE, YELLOW_CONCRETE, LIME_CONCRETE,
        PINK_CONCRETE, GRAY_CONCRETE, LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE,
        BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE, WHITE_CONCRETE_POWDER,
        ORANGE_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER, YELLOW_CONCRETE_POWDER,
        LIME_CONCRETE_POWDER, PINK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER, LIGHT_GRAY_CONCRETE_POWDER,
        CYAN_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, BLUE_CONCRETE_POWDER, BROWN_CONCRETE_POWDER,
        GREEN_CONCRETE_POWDER, RED_CONCRETE_POWDER, BLACK_CONCRETE_POWDER, WHITE_BED, ORANGE_BED,
        MAGENTA_BED, LIGHT_BLUE_BED, YELLOW_BED, LIME_BED, PINK_BED, GRAY_BED, LIGHT_GRAY_BED,
        CYAN_BED, PURPLE_BED, BLUE_BED, BROWN_BED, GREEN_BED, RED_BED, BLACK_BED
    }

    // Colored Wool
    public final static MaterialTag WHITE_WOOL = getMaterialPre1_13("WOOL", 0, "WHITE_WOOL");
    public final static MaterialTag ORANGE_WOOL = getMaterialPre1_13("WOOL", 1, "ORANGE_WOOL");
    public final static MaterialTag MAGENTA_WOOL = getMaterialPre1_13("WOOL", 2, "MAGENTA_WOOL");
    public final static MaterialTag LIGHT_BLUE_WOOL = getMaterialPre1_13("WOOL", 3, "LIGHT_BLUE_WOOL");
    public final static MaterialTag YELLOW_WOOL = getMaterialPre1_13("WOOL", 4, "YELLOW_WOOL");
    public final static MaterialTag LIME_WOOL = getMaterialPre1_13("WOOL", 5, "LIME_WOOL");
    public final static MaterialTag PINK_WOOL = getMaterialPre1_13("WOOL", 6, "PINK_WOOL");
    public final static MaterialTag GRAY_WOOL = getMaterialPre1_13("WOOL", 7, "GRAY_WOOL");
    public final static MaterialTag LIGHT_GRAY_WOOL = getMaterialPre1_13("WOOL", 8, "LIGHT_GRAY_WOOL");
    public final static MaterialTag CYAN_WOOL = getMaterialPre1_13("WOOL", 9, "CYAN_WOOL");
    public final static MaterialTag PURPLE_WOOL = getMaterialPre1_13("WOOL", 10, "PURPLE_WOOL");
    public final static MaterialTag BLUE_WOOL = getMaterialPre1_13("WOOL", 11, "BLUE_WOOL");
    public final static MaterialTag BROWN_WOOL = getMaterialPre1_13("WOOL", 12, "BROWN_WOOL");
    public final static MaterialTag GREEN_WOOL = getMaterialPre1_13("WOOL", 13, "GREEN_WOOL");
    public final static MaterialTag RED_WOOL = getMaterialPre1_13("WOOL", 14, "RED_WOOL");
    public final static MaterialTag BLACK_WOOL = getMaterialPre1_13("WOOL", 15, "BLACK_WOOL");

    // Colored Carpets
    public final static MaterialTag WHITE_CARPET = getMaterialPre1_13("CARPET", 0, "WHITE_CARPET");
    public final static MaterialTag ORANGE_CARPET = getMaterialPre1_13("CARPET", 1, "ORANGE_CARPET");
    public final static MaterialTag MAGENTA_CARPET = getMaterialPre1_13("CARPET", 2, "MAGENTA_CARPET");
    public final static MaterialTag LIGHT_BLUE_CARPET = getMaterialPre1_13("CARPET", 3, "LIGHT_BLUE_CARPET");
    public final static MaterialTag YELLOW_CARPET = getMaterialPre1_13("CARPET", 4, "YELLOW_CARPET");
    public final static MaterialTag LIME_CARPET = getMaterialPre1_13("CARPET", 5, "LIME_CARPET");
    public final static MaterialTag PINK_CARPET = getMaterialPre1_13("CARPET", 6, "PINK_CARPET");
    public final static MaterialTag GRAY_CARPET = getMaterialPre1_13("CARPET", 7, "GRAY_CARPET");
    public final static MaterialTag LIGHT_GRAY_CARPET = getMaterialPre1_13("CARPET", 8, "LIGHT_GRAY_CARPET");
    public final static MaterialTag CYAN_CARPET = getMaterialPre1_13("CARPET", 9, "CYAN_CARPET");
    public final static MaterialTag PURPLE_CARPET = getMaterialPre1_13("CARPET", 10, "PURPLE_CARPET");
    public final static MaterialTag BLUE_CARPET = getMaterialPre1_13("CARPET", 11, "BLUE_CARPET");
    public final static MaterialTag BROWN_CARPET = getMaterialPre1_13("CARPET", 12, "BROWN_CARPET");
    public final static MaterialTag GREEN_CARPET = getMaterialPre1_13("CARPET", 13, "GREEN_CARPET");
    public final static MaterialTag RED_CARPET = getMaterialPre1_13("CARPET", 14, "RED_CARPET");
    public final static MaterialTag BLACK_CARPET = getMaterialPre1_13("CARPET", 15, "BLACK_CARPET");

    // Colored Clay
    public final static MaterialTag WHITE_CLAY = getMaterialPre1_13("STAINED_CLAY", 0, "WHITE_CLAY");
    public final static MaterialTag ORANGE_CLAY = getMaterialPre1_13("STAINED_CLAY", 1, "ORANGE_CLAY");
    public final static MaterialTag MAGENTA_CLAY = getMaterialPre1_13("STAINED_CLAY", 2, "MAGENTA_CLAY");
    public final static MaterialTag LIGHT_BLUE_CLAY = getMaterialPre1_13("STAINED_CLAY", 3, "LIGHT_BLUE_CLAY");
    public final static MaterialTag YELLOW_CLAY = getMaterialPre1_13("STAINED_CLAY", 4, "YELLOW_CLAY");
    public final static MaterialTag LIME_CLAY = getMaterialPre1_13("STAINED_CLAY", 5, "LIME_CLAY");
    public final static MaterialTag PINK_CLAY = getMaterialPre1_13("STAINED_CLAY", 6, "PINK_CLAY");
    public final static MaterialTag GRAY_CLAY = getMaterialPre1_13("STAINED_CLAY", 7, "GRAY_CLAY");
    public final static MaterialTag LIGHT_GRAY_CLAY = getMaterialPre1_13("STAINED_CLAY", 8, "LIGHT_GRAY_CLAY");
    public final static MaterialTag CYAN_CLAY = getMaterialPre1_13("STAINED_CLAY", 9, "CYAN_CLAY");
    public final static MaterialTag PURPLE_CLAY = getMaterialPre1_13("STAINED_CLAY", 10, "PURPLE_CLAY");
    public final static MaterialTag BLUE_CLAY = getMaterialPre1_13("STAINED_CLAY", 11, "BLUE_CLAY");
    public final static MaterialTag BROWN_CLAY = getMaterialPre1_13("STAINED_CLAY", 12, "BROWN_CLAY");
    public final static MaterialTag GREEN_CLAY = getMaterialPre1_13("STAINED_CLAY", 13, "GREEN_CLAY");
    public final static MaterialTag RED_CLAY = getMaterialPre1_13("STAINED_CLAY", 14, "RED_CLAY");
    public final static MaterialTag BLACK_CLAY = getMaterialPre1_13("STAINED_CLAY", 15, "BLACK_CLAY");

    // Stained Glass
    public final static MaterialTag WHITE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 0, "WHITE_STAINED_GLASS");
    public final static MaterialTag ORANGE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 1, "ORANGE_STAINED_GLASS");
    public final static MaterialTag MAGENTA_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 2, "MAGENTA_STAINED_GLASS");
    public final static MaterialTag LIGHT_BLUE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 3, "LIGHT_BLUE_STAINED_GLASS");
    public final static MaterialTag YELLOW_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 4, "YELLOW_STAINED_GLASS");
    public final static MaterialTag LIME_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 5, "LIME_STAINED_GLASS");
    public final static MaterialTag PINK_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 6, "PINK_STAINED_GLASS");
    public final static MaterialTag GRAY_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 7, "GRAY_STAINED_GLASS");
    public final static MaterialTag LIGHT_GRAY_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 8, "LIGHT_GRAY_STAINED_GLASS");
    public final static MaterialTag CYAN_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 9, "CYAN_STAINED_GLASS");
    public final static MaterialTag PURPLE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 10, "PURPLE_STAINED_GLASS");
    public final static MaterialTag BLUE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 11, "BLUE_STAINED_GLASS");
    public final static MaterialTag BROWN_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 12, "BROWN_STAINED_GLASS");
    public final static MaterialTag GREEN_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 13, "GREEN_STAINED_GLASS");
    public final static MaterialTag RED_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 14, "RED_STAINED_GLASS");
    public final static MaterialTag BLACK_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 15, "BLACK_STAINED_GLASS");

    // Stained Glass Panes
    public final static MaterialTag WHITE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 0, "WHITE_STAINED_GLASS_PANE");
    public final static MaterialTag ORANGE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 1, "ORANGE_STAINED_GLASS_PANE");
    public final static MaterialTag MAGENTA_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 2, "MAGENTA_STAINED_GLASS_PANE");
    public final static MaterialTag LIGHT_BLUE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 3, "LIGHT_BLUE_STAINED_GLASS_PANE");
    public final static MaterialTag YELLOW_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 4, "YELLOW_STAINED_GLASS_PANE");
    public final static MaterialTag LIME_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 5, "LIME_STAINED_GLASS_PANE");
    public final static MaterialTag PINK_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 6, "PINK_STAINED_GLASS_PANE");
    public final static MaterialTag GRAY_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 7, "GRAY_STAINED_GLASS_PANE");
    public final static MaterialTag LIGHT_GRAY_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 8, "LIGHT_GRAY_STAINED_GLASS_PANE");
    public final static MaterialTag CYAN_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 9, "CYAN_STAINED_GLASS_PANE");
    public final static MaterialTag PURPLE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 10, "PURPLE_STAINED_GLASS_PANE");
    public final static MaterialTag BLUE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 11, "BLUE_STAINED_GLASS_PANE");
    public final static MaterialTag BROWN_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 12, "BROWN_STAINED_GLASS_PANE");
    public final static MaterialTag GREEN_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 13, "GREEN_STAINED_GLASS_PANE");
    public final static MaterialTag RED_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 14, "RED_STAINED_GLASS_PANE");
    public final static MaterialTag BLACK_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 15, "BLACK_STAINED_GLASS_PANE");

    // Planks
    public final static MaterialTag OAK_PLANKS = getMaterialPre1_13("WOOD", 0, "OAK_PLANKS");
    public final static MaterialTag SPRUCE_PLANKS = getMaterialPre1_13("WOOD", 1, "SPRUCE_PLANKS");
    public final static MaterialTag BIRCH_PLANKS = getMaterialPre1_13("WOOD", 2, "BIRCH_PLANKS");
    public final static MaterialTag JUNGLE_PLANKS = getMaterialPre1_13("WOOD", 3, "JUNGLE_PLANKS");
    public final static MaterialTag ACACIA_PLANKS = getMaterialPre1_13("WOOD", 4, "ACACIA_PLANKS");
    public final static MaterialTag DARKOAK_PLANKS = getMaterialPre1_13("WOOD", 5, "DARKOAK_PLANKS");

    // Saplings
    public final static MaterialTag OAK_SAPLING = getMaterialPre1_13("SAPLING", 0, "OAK_SAPLING");
    public final static MaterialTag SPRUCE_SAPLING = getMaterialPre1_13("SAPLING", 1, "SPRUCE_SAPLING");
    public final static MaterialTag BIRCH_SAPLING = getMaterialPre1_13("SAPLING", 2, "BIRCH_SAPLING");
    public final static MaterialTag JUNGLE_SAPLING = getMaterialPre1_13("SAPLING", 3, "JUNGLE_SAPLING");
    public final static MaterialTag ACACIA_SAPLING = getMaterialPre1_13("SAPLING", 4, "ACACIA_SAPLING");
    public final static MaterialTag DARKOAK_SAPLING = getMaterialPre1_13("SAPLING", 5, "DARKOAK_SAPLING");

    // Leaves
    public final static MaterialTag OAK_LEAVES = getMaterialPre1_13("LEAVES", 0, "OAK_LEAVES");
    public final static MaterialTag SPRUCE_LEAVES = getMaterialPre1_13("LEAVES", 1, "SPRUCE_LEAVES");
    public final static MaterialTag BIRCH_LEAVES = getMaterialPre1_13("LEAVES", 2, "BIRCH_LEAVES");
    public final static MaterialTag JUNGLE_LEAVES = getMaterialPre1_13("LEAVES", 3, "JUNGLE_LEAVES");
    public final static MaterialTag ACACIA_LEAVES = getMaterialPre1_13("LEAVES_2", 0, "ACACIA_LEAVES");
    public final static MaterialTag DARKOAK_LEAVES = getMaterialPre1_13("LEAVES_2", 1, "DARKOAK_LEAVES");
    public final static MaterialTag PLACED_OAK_LEAVES = getMaterialPre1_13("LEAVES", 4, "PLACED_OAK_LEAVES");
    public final static MaterialTag PLACED_SPRUCE_LEAVES = getMaterialPre1_13("LEAVES", 5, "PLACED_SPRUCE_LEAVES");
    public final static MaterialTag PLACED_BIRCH_LEAVES = getMaterialPre1_13("LEAVES", 6, "PLACED_BIRCH_LEAVES");
    public final static MaterialTag PLACED_JUNGLE_LEAVES = getMaterialPre1_13("LEAVES", 7, "PLACED_JUNGLE_LEAVES");
    public final static MaterialTag PLACED_ACACIA_LEAVES = getMaterialPre1_13("LEAVES_2", 4, "PLACED_ACACIA_LEAVES");
    public final static MaterialTag PLACED_DARKOAK_LEAVES = getMaterialPre1_13("LEAVES_2", 5, "PLACED_DARKOAK_LEAVES");

    // Logs
    public final static MaterialTag OAK_LOG = getMaterialPre1_13("LOG", 0, "OAK_LOG");
    public final static MaterialTag SPRUCE_LOG = getMaterialPre1_13("LOG", 1, "SPRUCE_LOG");
    public final static MaterialTag BIRCH_LOG = getMaterialPre1_13("LOG", 2, "BIRCH_LOG");
    public final static MaterialTag JUNGLE_LOG = getMaterialPre1_13("LOG", 3, "JUNGLE_LOG");
    public final static MaterialTag ACACIA_LOG = getMaterialPre1_13("LOG_2", 0, "ACACIA_LOG");
    public final static MaterialTag DARKOAK_LOG = getMaterialPre1_13("LOG_2", 1, "DARKOAK_LOG");
    public final static MaterialTag OAK_LOG_EAST = getMaterialPre1_13("LOG", 4, "OAK_LOG_EAST");
    public final static MaterialTag SPRUCE_LOG_EAST = getMaterialPre1_13("LOG", 5, "SPRUCE_LOG_EAST");
    public final static MaterialTag BIRCH_LOG_EAST = getMaterialPre1_13("LOG", 6, "BIRCH_LOG_EAST");
    public final static MaterialTag JUNGLE_LOG_EAST = getMaterialPre1_13("LOG", 7, "JUNGLE_LOG_EAST");
    public final static MaterialTag ACACIA_LOG_EAST = getMaterialPre1_13("LOG_2", 4, "ACACIA_LOG_EAST");
    public final static MaterialTag DARKOAK_LOG_EAST = getMaterialPre1_13("LOG_2", 5, "DARKOAK_LOG_EAST");
    public final static MaterialTag OAK_LOG_NORTH = getMaterialPre1_13("LOG", 8, "OAK_LOG_NORTH");
    public final static MaterialTag SPRUCE_LOG_NORTH = getMaterialPre1_13("LOG", 9, "SPRUCE_LOG_NORTH");
    public final static MaterialTag BIRCH_LOG_NORTH = getMaterialPre1_13("LOG", 10, "BIRCH_LOG_NORTH");
    public final static MaterialTag JUNGLE_LOG_NORTH = getMaterialPre1_13("LOG", 11, "JUNGLE_LOG_NORTH");
    public final static MaterialTag ACACIA_LOG_NORTH = getMaterialPre1_13("LOG_2", 8, "ACACIA_LOG_NORTH");
    public final static MaterialTag DARKOAK_LOG_NORTH = getMaterialPre1_13("LOG_2", 9, "DARKOAK_LOG_NORTH");
    public final static MaterialTag OAK_LOG_BALL = getMaterialPre1_13("LOG", 12, "OAK_LOG_BALL");
    public final static MaterialTag SPRUCE_LOG_BALL = getMaterialPre1_13("LOG", 13, "SPRUCE_LOG_BALL");
    public final static MaterialTag BIRCH_LOG_BALL = getMaterialPre1_13("LOG", 14, "BIRCH_LOG_BALL");
    public final static MaterialTag JUNGLE_LOG_BALL = getMaterialPre1_13("LOG", 15, "JUNGLE_LOG_BALL");
    public final static MaterialTag ACACIA_LOG_BALL = getMaterialPre1_13("LOG_2", 12, "ACACIA_LOG_BALL");
    public final static MaterialTag DARKOAK_LOG_BALL = getMaterialPre1_13("LOG_2", 13, "DARKOAK_LOG_BALL");

    // Sandstone
    public final static MaterialTag CHISELED_SANDSTONE = getMaterialPre1_13("SANDSTONE", 1, "CHISELED_SANDSTONE");
    public final static MaterialTag SMOOTH_SANDSTONE = getMaterialPre1_13("SANDSTONE", 2, "SMOOTH_SANDSTONE");

    // Stone Bricks
    public final static MaterialTag STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 0, "STONE_BRICK");
    public final static MaterialTag MOSSY_STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 1, "MOSSY_STONE_BRICK");
    public final static MaterialTag CRACKED_STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 2, "CRACKED_STONE_BRICK");
    public final static MaterialTag CHISELED_STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 3, "CHISELED_STONE_BRICK");

    // Quartz Block
    public final static MaterialTag CHISELED_QUARTZ_BLOCK = getMaterialPre1_13("QUARTZ_BLOCK", 1, "CHISELED_QUARTZ_BLOCK");
    public final static MaterialTag PILLAR_QUARTZ_BLOCK = getMaterialPre1_13("QUARTZ_BLOCK", 2, "PILLAR_QUARTZ_BLOCK");
    public final static MaterialTag PILLAR_QUARTZ_BLOCK_EAST = getMaterialPre1_13("QUARTZ_BLOCK", 3, "PILLAR_QUARTZ_BLOCK_EAST");
    public final static MaterialTag PILLAR_QUARTZ_BLOCK_NORTH = getMaterialPre1_13("QUARTZ_BLOCK", 4, "PILLAR_QUARTZ_BLOCK_NORTH");

    // Colored Ink
    public final static MaterialTag INK = getMaterialPre1_13("INK_SACK", 0, "INK");
    public final static MaterialTag RED_DYE = getMaterialPre1_13("INK_SACK", 1, "RED_DYE");
    public final static MaterialTag GREEN_DYE = getMaterialPre1_13("INK_SACK", 2, "GREEN_DYE");
    public final static MaterialTag COCOA_BEANS = getMaterialPre1_13("INK_SACK", 3, "COCOA_BEANS");
    public final static MaterialTag LAPIS_LAZULI = getMaterialPre1_13("INK_SACK", 4, "LAPIS_LAZULI");
    public final static MaterialTag PURPLE_DYE = getMaterialPre1_13("INK_SACK", 5, "PURPLE_DYE");
    public final static MaterialTag CYAN_DYE = getMaterialPre1_13("INK_SACK", 6, "CYAN_DYE");
    public final static MaterialTag LIGHT_GRAY_DYE = getMaterialPre1_13("INK_SACK", 7, "LIGHT_GRAY_DYE");
    public final static MaterialTag GRAY_DYE = getMaterialPre1_13("INK_SACK", 8, "GRAY_DYE");
    public final static MaterialTag PINK_DYE = getMaterialPre1_13("INK_SACK", 9, "PINK_DYE");
    public final static MaterialTag LIME_DYE = getMaterialPre1_13("INK_SACK", 10, "LIME_DYE");
    public final static MaterialTag YELLOW_DYE = getMaterialPre1_13("INK_SACK", 11, "YELLOW_DYE");
    public final static MaterialTag LIGHT_BLUE_DYE = getMaterialPre1_13("INK_SACK", 12, "LIGHT_BLUE_DYE");
    public final static MaterialTag MAGENTA_DYE = getMaterialPre1_13("INK_SACK", 13, "MAGENTA_DYE");
    public final static MaterialTag ORANGE_DYE = getMaterialPre1_13("INK_SACK", 14, "ORANGE_DYE");
    public final static MaterialTag BONE_MEAL = getMaterialPre1_13("INK_SACK", 15, "BONE_MEAL");

    // Tall Grass
    public final static MaterialTag SHRUB = getMaterialPre1_13("LONG_GRASS", 0, "SHRUB");
    public final static MaterialTag TALL_GRASS = getMaterialPre1_13("LONG_GRASS", 1, "TALL_GRASS");
    public final static MaterialTag FERN = getMaterialPre1_13("LONG_GRASS", 2, "FERN");

    // Flowers
    public final static MaterialTag POPPY = getMaterialPre1_13("RED_ROSE", 0, "POPPY");
    public final static MaterialTag BLUE_ORCHID = getMaterialPre1_13("RED_ROSE", 1, "BLUE_ORCHID");
    public final static MaterialTag ALLIUM = getMaterialPre1_13("RED_ROSE", 2, "ALLIUM");
    public final static MaterialTag AZURE_BLUET = getMaterialPre1_13("RED_ROSE", 3, "AZURE_BLUET");
    public final static MaterialTag RED_TULIP = getMaterialPre1_13("RED_ROSE", 4, "RED_TULIP");
    public final static MaterialTag ORANGE_TULIP = getMaterialPre1_13("RED_ROSE", 5, "ORANGE_TULIP");
    public final static MaterialTag WHITE_TULIP = getMaterialPre1_13("RED_ROSE", 6, "WHITE_TULIP");
    public final static MaterialTag PINK_TULIP = getMaterialPre1_13("RED_ROSE", 7, "PINK_TULIP");
    public final static MaterialTag OXEYE_DAISY = getMaterialPre1_13("RED_ROSE", 8, "OXEYE_DAISY");

    // Double-tall Plants
    public final static MaterialTag SUNFLOWER = getMaterialPre1_13("DOUBLE_PLANT", 0, "SUNFLOWER");
    public final static MaterialTag LILAC = getMaterialPre1_13("DOUBLE_PLANT", 1, "LILAC");
    public final static MaterialTag DOUBLE_TALLGRASS = getMaterialPre1_13("DOUBLE_PLANT", 2, "DOUBLE_TALLGRASS");
    public final static MaterialTag LARGE_FERN = getMaterialPre1_13("DOUBLE_PLANT", 3, "LARGE_FERN");
    public final static MaterialTag ROSE_BUSH = getMaterialPre1_13("DOUBLE_PLANT", 4, "ROSE_BUSH");
    public final static MaterialTag PEONY = getMaterialPre1_13("DOUBLE_PLANT", 5, "PEONY");
    public final static MaterialTag DOUBLEPLANT_TOP = getMaterialPre1_13("DOUBLE_PLANT", 8, "DOUBLEPLANT_TOP");

    // Potted Plants
    public final static MaterialTag EMPTY_POT = getMaterialPre1_13("FLOWER_POT", 0, "EMPTY_POT");
    public final static MaterialTag POTTED_POPPY = getMaterialPre1_13("FLOWER_POT", 1, "POTTED_POPPY");
    public final static MaterialTag POTTED_DAISY = getMaterialPre1_13("FLOWER_POT", 2, "POTTED_DAISY");
    public final static MaterialTag POTTED_OAK_SAPLING = getMaterialPre1_13("FLOWER_POT", 3, "POTTED_OAK_SAPLING");
    public final static MaterialTag POTTED_SPRUCE_SAPLING = getMaterialPre1_13("FLOWER_POT", 4, "POTTED_SPRUCE_SAPLING");
    public final static MaterialTag POTTED_BIRCH_SAPLING = getMaterialPre1_13("FLOWER_POT", 5, "POTTED_BIRCH_SAPLING");
    public final static MaterialTag POTTED_JUNGLE_SAPLING = getMaterialPre1_13("FLOWER_POT", 6, "POTTED_JUNGLE_SAPLING");
    public final static MaterialTag POTTED_RED_MUSHROOM = getMaterialPre1_13("FLOWER_POT", 7, "POTTED_RED_MUSHROOM");
    public final static MaterialTag POTTED_BROWN_MUSHROOM = getMaterialPre1_13("FLOWER_POT", 8, "POTTED_BROWN_MUSHROOM");
    public final static MaterialTag POTTED_CACTUS = getMaterialPre1_13("FLOWER_POT", 9, "POTTED_CACTUS");
    public final static MaterialTag POTTED_SHRUB = getMaterialPre1_13("FLOWER_POT", 10, "POTTED_SHRUB");
    public final static MaterialTag POTTED_FERN = getMaterialPre1_13("FLOWER_POT", 11, "POTTED_FERN");
    public final static MaterialTag POTTED_ACACIA_SAPLING = getMaterialPre1_13("FLOWER_POT", 12, "POTTED_ACACIA_SAPLING");
    public final static MaterialTag POTTED_DARKOAK_SAPLING = getMaterialPre1_13("FLOWER_POT", 13, "POTTED_DARKOAK_SAPLING");

    // Steps
    public final static MaterialTag STONE_SLAB = getMaterialPre1_13("STEP", 0, "STONE_SLAB");
    public final static MaterialTag SANDSTONE_SLAB = getMaterialPre1_13("STEP", 1, "SANDSTONE_SLAB");
    public final static MaterialTag WOODEN_SLAB = getMaterialPre1_13("STEP", 2, "WOODEN_SLAB");
    public final static MaterialTag COBBLESTONE_SLAB = getMaterialPre1_13("STEP", 3, "COBBLESTONE_SLAB");
    public final static MaterialTag BRICKS_SLAB = getMaterialPre1_13("STEP", 4, "BRICKS_SLAB");
    public final static MaterialTag STONEBRICKS_SLAB = getMaterialPre1_13("STEP", 5, "STONEBRICKS_SLAB");
    public final static MaterialTag NETHERBRICK_SLAB = getMaterialPre1_13("STEP", 6, "NETHERBRICK_SLAB");
    public final static MaterialTag QUARTZ_SLAB = getMaterialPre1_13("STEP", 7, "QUARTZ_SLAB");
    public final static MaterialTag STONE_SLAB_UP = getMaterialPre1_13("STEP", 8, "STONE_SLAB_UP");
    public final static MaterialTag SANDSTONE_SLAB_UP = getMaterialPre1_13("STEP", 9, "SANDSTONE_SLAB_UP");
    public final static MaterialTag WOODEN_SLAB_UP = getMaterialPre1_13("STEP", 10, "WOODEN_SLAB_UP");
    public final static MaterialTag COBBLESTONE_SLAB_UP = getMaterialPre1_13("STEP", 11, "COBBLESTONE_SLAB_UP");
    public final static MaterialTag BRICKS_SLAB_UP = getMaterialPre1_13("STEP", 12, "BRICKS_SLAB_UP");
    public final static MaterialTag STONEBRICKS_SLAB_UP = getMaterialPre1_13("STEP", 13, "STONEBRICKS_SLAB_UP");
    public final static MaterialTag NETHERBRICK_SLAB_UP = getMaterialPre1_13("STEP", 14, "NETHERBRICK_SLAB_UP");
    public final static MaterialTag QUARTZ_SLAB_UP = getMaterialPre1_13("STEP", 15, "QUARTZ_SLAB_UP");

    // Wood Steps
    public final static MaterialTag OAK_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 0, "OAK_WOOD_SLAB");
    public final static MaterialTag SPRUCE_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 1, "SPRUCE_WOOD_SLAB");
    public final static MaterialTag BIRCH_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 2, "BIRCH_WOOD_SLAB");
    public final static MaterialTag JUNGLE_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 3, "JUNGLE_WOOD_SLAB");
    public final static MaterialTag ACACIA_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 4, "ACACIA_WOOD_SLAB");
    public final static MaterialTag DARKOAK_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 5, "DARKOAK_WOOD_SLAB");
    public final static MaterialTag OAK_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 8, "OAK_WOOD_SLAB_UP");
    public final static MaterialTag SPRUCE_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 9, "SPRUCE_WOOD_SLAB_UP");
    public final static MaterialTag BIRCH_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 10, "BIRCH_WOOD_SLAB_UP");
    public final static MaterialTag JUNGLE_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 11, "JUNGLE_WOOD_SLAB_UP");
    public final static MaterialTag ACACIA_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 12, "ACACIA_WOOD_SLAB_UP");
    public final static MaterialTag DARKOAK_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 13, "DARKOAK_WOOD_SLAB_UP");

    // Double Steps
    public final static MaterialTag STONE_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 0, "STONE_DOUBLESLAB");
    public final static MaterialTag SANDSTONE_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 1, "SANDSTONE_DOUBLESLAB");
    public final static MaterialTag WOODEN_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 2, "WOODEN_DOUBLESLAB");
    public final static MaterialTag COBBLESTONE_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 3, "COBBLESTONE_DOUBLESLAB");
    public final static MaterialTag BRICKS_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 4, "BRICKS_DOUBLESLAB");
    public final static MaterialTag STONEBRICKS_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 5, "STONEBRICKS_DOUBLESLAB");
    public final static MaterialTag NETHERBRICK_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 6, "NETHERBRICK_DOUBLESLAB");
    public final static MaterialTag QUARTZ_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 7, "QUARTZ_DOUBLESLAB");
    public final static MaterialTag STONE_SLABBALL = getMaterialPre1_13("DOUBLE_STEP", 8, "STONE_SLABBALL");
    public final static MaterialTag SANDSTONE_SLABBALL = getMaterialPre1_13("DOUBLE_STEP", 9, "SANDSTONE_SLABBALL");

    // Wood Double Steps
    public final static MaterialTag OAK_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 0, "OAK_WOOD_DOUBLESLAB");
    public final static MaterialTag SPRUCE_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 1, "SPRUCE_WOOD_DOUBLESLAB");
    public final static MaterialTag BIRCH_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 2, "BIRCH_WOOD_DOUBLESLAB");
    public final static MaterialTag JUNGLE_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 3, "JUNGLE_WOOD_DOUBLESLAB");
    public final static MaterialTag ACACIA_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 4, "ACACIA_WOOD_DOUBLESLAB");
    public final static MaterialTag DARKOAK_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 5, "DARKOAK_WOOD_DOUBLESLAB");

    // Skull Items
    public final static MaterialTag SKELETON_SKULL = getMaterialPre1_13("SKULL_ITEM", 0, "SKELETON_SKULL");
    public final static MaterialTag WITHERSKELETON_SKULL = getMaterialPre1_13("SKULL_ITEM", 1, "WITHERSKELETON_SKULL");
    public final static MaterialTag ZOMBIE_SKULL = getMaterialPre1_13("SKULL_ITEM", 2, "ZOMBIE_SKULL");
    public final static MaterialTag HUMAN_SKULL = getMaterialPre1_13("SKULL_ITEM", 3, "HUMAN_SKULL");
    public final static MaterialTag CREEPER_SKULL = getMaterialPre1_13("SKULL_ITEM", 4, "CREEPER_SKULL");

    // Monster Eggs
    public final static MaterialTag SKELETON_EGG = getMaterialPre1_13("MONSTER_EGG", 51, "SKELETON_EGG");

    // Fish
    public final static MaterialTag COOKED_FISH = getMaterialPre1_13("COOKED_FISH", 0, "COOKED_FISH");
    public final static MaterialTag COOKED_SALMON = getMaterialPre1_13("COOKED_FISH", 1, "COOKED_SALMON");
    public final static MaterialTag COOKED_CLOWNFISH = getMaterialPre1_13("COOKED_FISH", 2, "COOKED_CLOWNFISH");
    public final static MaterialTag COOKED_PUFFERFISH = getMaterialPre1_13("COOKED_FISH", 3, "COOKED_PUFFERFISH");
    public final static MaterialTag RAW_FISH = getMaterialPre1_13("RAW_FISH", 0, "RAW_FISH");
    public final static MaterialTag RAW_SALMON = getMaterialPre1_13("RAW_FISH", 1, "RAW_SALMON");
    public final static MaterialTag RAW_CLOWNFISH = getMaterialPre1_13("RAW_FISH", 2, "RAW_CLOWNFISH");
    public final static MaterialTag RAW_PUFFERFISH = getMaterialPre1_13("RAW_FISH", 3, "RAW_PUFFERFISH");

    // One-Offs (Don't have their own group)
    public final static MaterialTag CHARCOAL = getMaterialPre1_13("COAL", 1, "CHARCOAL");
    public final static MaterialTag RED_SAND = getMaterialPre1_13("SAND", 1, "RED_SAND");
    public final static MaterialTag COARSE_DIRT = getMaterialPre1_13("DIRT", 1, "COARSE_DIRT");
    public final static MaterialTag PODZOL = getMaterialPre1_13("DIRT", 2, "PODZOL");
    public final static MaterialTag MOSSY_COBBLE_WALL = getMaterialPre1_13("COBBLE_WALL", 1, "MOSSY_COBBLE_WALL");

    // Stone
    // GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ADESITE
    public final static MaterialTag GRANITE = getMaterialPre1_13("STONE", 1, "GRANITE");
    public final static MaterialTag POLISHED_GRANITE = getMaterialPre1_13("STONE", 2, "POLISHED_GRANITE");
    public final static MaterialTag DIORITE = getMaterialPre1_13("STONE", 3, "DIORITE");
    public final static MaterialTag POLISHED_DIORITE = getMaterialPre1_13("STONE", 4, "POLISHED_DIORITE");
    public final static MaterialTag ANDESITE = getMaterialPre1_13("STONE", 5, "ANDESITE");
    public final static MaterialTag POLISHED_ANDESITE = getMaterialPre1_13("STONE", 6, "POLISHED_ANDESITE");

    // Bed
    public final static MaterialTag WHITE_BED = getMaterialPre1_13("BED", 0, "WHITE_BED");
    public final static MaterialTag ORANGE_BED = getMaterialPre1_13("BED", 1, "ORANGE_BED");
    public final static MaterialTag MAGENTA_BED = getMaterialPre1_13("BED", 2, "MAGENTA_BED");
    public final static MaterialTag LIGHT_BLUE_BED = getMaterialPre1_13("BED", 3, "LIGHT_BLUE_BED");
    public final static MaterialTag YELLOW_BED = getMaterialPre1_13("BED", 4, "YELLOW_BED");
    public final static MaterialTag LIME_BED = getMaterialPre1_13("BED", 5, "LIME_BED");
    public final static MaterialTag PINK_BED = getMaterialPre1_13("BED", 6, "PINK_BED");
    public final static MaterialTag GRAY_BED = getMaterialPre1_13("BED", 7, "GRAY_BED");
    public final static MaterialTag LIGHT_GRAY_BED = getMaterialPre1_13("BED", 8, "LIGHT_GRAY_BED");
    public final static MaterialTag CYAN_BED = getMaterialPre1_13("BED", 9, "CYAN_BED");
    public final static MaterialTag PURPLE_BED = getMaterialPre1_13("BED", 10, "PURPLE_BED");
    public final static MaterialTag BLUE_BED = getMaterialPre1_13("BED", 11, "BLUE_BED");
    public final static MaterialTag BROWN_BED = getMaterialPre1_13("BED", 12, "BROWN_BED");
    public final static MaterialTag GREEN_BED = getMaterialPre1_13("BED", 13, "GREEN_BED");
    public final static MaterialTag RED_BED = getMaterialPre1_13("BED", 14, "RED_BED");
    public final static MaterialTag BLACK_BED = getMaterialPre1_13("BED", 15, "BLACK_BED");

    // -- Version Specific --

    // Concrete
    public final static MaterialTag WHITE_CONCRETE = getMaterial1_12("CONCRETE", 0, "WHITE_CONCRETE");
    public final static MaterialTag ORANGE_CONCRETE = getMaterial1_12("CONCRETE", 1, "ORANGE_CONCRETE");
    public final static MaterialTag MAGENTA_CONCRETE = getMaterial1_12("CONCRETE", 2, "MAGENTA_CONCRETE");
    public final static MaterialTag LIGHT_BLUE_CONCRETE = getMaterial1_12("CONCRETE", 3, "LIGHT_BLUE_CONCRETE");
    public final static MaterialTag YELLOW_CONCRETE = getMaterial1_12("CONCRETE", 4, "YELLOW_CONCRETE");
    public final static MaterialTag LIME_CONCRETE = getMaterial1_12("CONCRETE", 5, "LIME_CONCRETE");
    public final static MaterialTag PINK_CONCRETE = getMaterial1_12("CONCRETE", 6, "PINK_CONCRETE");
    public final static MaterialTag GRAY_CONCRETE = getMaterial1_12("CONCRETE", 7, "GRAY_CONCRETE");
    public final static MaterialTag LIGHT_GRAY_CONCRETE = getMaterial1_12("CONCRETE", 8, "LIGHT_GRAY_CONCRETE");
    public final static MaterialTag CYAN_CONCRETE = getMaterial1_12("CONCRETE", 9, "CYAN_CONCRETE");
    public final static MaterialTag PURPLE_CONCRETE = getMaterial1_12("CONCRETE", 10, "PURPLE_CONCRETE");
    public final static MaterialTag BLUE_CONCRETE = getMaterial1_12("CONCRETE", 11, "BLUE_CONCRETE");
    public final static MaterialTag BROWN_CONCRETE = getMaterial1_12("CONCRETE", 12, "BROWN_CONCRETE");
    public final static MaterialTag GREEN_CONCRETE = getMaterial1_12("CONCRETE", 13, "GREEN_CONCRETE");
    public final static MaterialTag RED_CONCRETE = getMaterial1_12("CONCRETE", 14, "RED_CONCRETE");
    public final static MaterialTag BLACK_CONCRETE = getMaterial1_12("CONCRETE", 15, "BLACK_CONCRETE");

    // Concrete Powder
    public final static MaterialTag WHITE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 0, "WHITE_CONCRETE_POWDER");
    public final static MaterialTag ORANGE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 1, "ORANGE_CONCRETE_POWDER");
    public final static MaterialTag MAGENTA_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 2, "MAGENTA_CONCRETE_POWDER");
    public final static MaterialTag LIGHT_BLUE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 3, "LIGHT_BLUE_CONCRETE_POWDER");
    public final static MaterialTag YELLOW_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 4, "YELLOW_CONCRETE_POWDER");
    public final static MaterialTag LIME_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 5, "LIME_CONCRETE_POWDER");
    public final static MaterialTag PINK_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 6, "PINK_CONCRETE_POWDER");
    public final static MaterialTag GRAY_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 7, "GRAY_CONCRETE_POWDER");
    public final static MaterialTag LIGHT_GRAY_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 8, "LIGHT_GRAY_CONCRETE_POWDER");
    public final static MaterialTag CYAN_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 9, "CYAN_CONCRETE_POWDER");
    public final static MaterialTag PURPLE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 10, "PURPLE_CONCRETE_POWDER");
    public final static MaterialTag BLUE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 11, "BLUE_CONCRETE_POWDER");
    public final static MaterialTag BROWN_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 12, "BROWN_CONCRETE_POWDER");
    public final static MaterialTag GREEN_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 13, "GREEN_CONCRETE_POWDER");
    public final static MaterialTag RED_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 14, "RED_CONCRETE_POWDER");
    public final static MaterialTag BLACK_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 15, "BLACK_CONCRETE_POWDER");

    // Version checks for version-specific materials
    public static MaterialTag getMaterial1_12(String material, int data, String name) {
        if (NMSHandler.getVersion() == NMSVersion.v1_12_R1) {
            return new MaterialTag(Material.valueOf(material), data).forceIdentifyAs(name);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            if (all_dMaterials == null) {
                all_dMaterials = new HashMap<>();
            }
            all_dMaterials.put(name, new MaterialTag(getBukkitMaterial(material), data));
        }
        return null;
    }

    public static MaterialTag getMaterialPre1_13(String material, int data, String name) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1)) {
            return new MaterialTag(Material.valueOf(material), data).forceIdentifyAs(name);
        }
        else {
            if (all_dMaterials == null) {
                all_dMaterials = new HashMap<>();
            }
            all_dMaterials.put(name, new MaterialTag(getBukkitMaterial(material), data));
        }
        return null;
    }

    public static Material getBukkitMaterial(String material) {
        Material result = Material.getMaterial(material);
        if (result == null) {
            result = Material.getMaterial(material, true);
        }
        return result;
    }

    private static final Map<Integer, Material> MATERIAL_BY_LEGACY_ID;

    static {
        MATERIAL_BY_LEGACY_ID = new HashMap<>();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            Map<String, Material> map = ReflectionHelper.getFieldValue(Material.class, "BY_NAME", null);
            for (Material material : map.values()) {
                if (material.isLegacy()) {
                    MATERIAL_BY_LEGACY_ID.put(material.getId(), material);
                }
            }
        }
        else {
            for (Material material : Material.values()) {
                MATERIAL_BY_LEGACY_ID.put(material.getId(), material);
            }
        }
    }

    @Deprecated
    public static Material getLegacyMaterial(int id) {
        return MATERIAL_BY_LEGACY_ID.get(id);
    }

    public static Map<Material, Map<Integer, MaterialTag>> material_varieties = new HashMap<>();

    public static Map<String, MaterialTag> all_dMaterials;

    /**
     * Registers a MaterialTag as a 'variety'. Upon construction of a MaterialTag, this
     * registry will be checked to see if a variety can be used instead of the traditional
     * enum/data format.
     * <p/>
     * MaterialTags in this list should probably 'forceIdentifyAs'.
     *
     * @param material the MaterialTag variety
     * @return the MaterialTag registered
     */
    public static MaterialTag registerVariety(MaterialTag material) {
        Map<Integer, MaterialTag> entry;
        // Get any existing entries for the Material, or make a new HashMap for entries.
        if (material_varieties.containsKey(material.getMaterial())) {
            entry = material_varieties.get(material.getMaterial());
        }
        else {
            entry = new HashMap<>();
        }
        // Put in new entry
        entry.put((int) material.getData(), material);
        // Return the MaterialTag
        material_varieties.put(material.getMaterial(), entry);
        if (all_dMaterials == null) {
            all_dMaterials = new HashMap<>();
        }
        all_dMaterials.put(material.realName().toUpperCase(), material);
        return material;
    }

    // Called on startup
    public static void _initialize() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            return;
        }
        for (MaterialTags material : MaterialTags.values()) {
            try {
                Field field = OldMaterialsHelper.class.getField(material.name());
                MaterialTag mat = (MaterialTag) field.get(null);
                if (mat != null) {
                    registerVariety(mat);
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
    }

    /**
     * Gets a MaterialTag from a bukkit Material.
     *
     * @param material the bukkit Material
     * @return a MaterialTag representation of the Material
     */
    public static MaterialTag getMaterialFrom(Material material) {
        return getMaterialFrom(material, 0);
    }

    /**
     * Gets a MaterialTag from a Bukkit Material/Data. MaterialTags can identify
     * as something more straight-forward than the traditional material,data format.
     * Example: wool,1 would return the ORANGE_WOOL MaterialTag.
     *
     * @param material the base Bukkit material
     * @param data     the datavalue to use
     * @return a MaterialTag representation of the input Bukkit material
     */
    public static MaterialTag getMaterialFrom(Material material, int data) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            if (data != 0) {
                // upvert old data valued materials
                if (material.isBlock()) {
                    material = NMSHandler.getInstance().getBlockHelper().getBlockData(material, (byte) data).getMaterial();
                }
            }
            // Forcible upvert from legacy
            if (material.isLegacy()) {
                material = Bukkit.getUnsafe().fromLegacy(material);
            }
            return new MaterialTag(material);
        }
        if (material_varieties.containsKey(material) && material_varieties.get(material).containsKey(data)) {
            return material_varieties.get(material).get(data);
        }
        return new MaterialTag(material, data);
    }
}
