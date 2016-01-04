package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.utilities.blocks.SafeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dMaterial implements dObject {

    final static Pattern materialPattern = Pattern.compile("(?:m@)?(\\w+)[,:]?(\\d+)?", Pattern.CASE_INSENSITIVE);

    // Will be called a lot, no need to construct/deconstruct.
    public final static dMaterial AIR = new dMaterial(Material.AIR);


    /////////////////
    // dMaterial Varieties
    ///////////////

    // dMaterial 'extra materials' for making 'data variety' materials easier to work with. Register materials
    // that aren't included in the bukkit Material enum here to make lookup easier.

    public static enum dMaterials {
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
        GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ANDESITE, COARSE_DIRT
    }

    // dMaterials are just made and disposed of for standard 'Materials', but these we will keep around since
    // they are special :)

    // Colored Wool
    public final static dMaterial WHITE_WOOL = new dMaterial(Material.WOOL, 0).forceIdentifyAs("WHITE_WOOL");
    public final static dMaterial ORANGE_WOOL = new dMaterial(Material.WOOL, 1).forceIdentifyAs("ORANGE_WOOL");
    public final static dMaterial MAGENTA_WOOL = new dMaterial(Material.WOOL, 2).forceIdentifyAs("MAGENTA_WOOL");
    public final static dMaterial LIGHT_BLUE_WOOL = new dMaterial(Material.WOOL, 3).forceIdentifyAs("LIGHT_BLUE_WOOL");
    public final static dMaterial YELLOW_WOOL = new dMaterial(Material.WOOL, 4).forceIdentifyAs("YELLOW_WOOL");
    public final static dMaterial LIME_WOOL = new dMaterial(Material.WOOL, 5).forceIdentifyAs("LIME_WOOL");
    public final static dMaterial PINK_WOOL = new dMaterial(Material.WOOL, 6).forceIdentifyAs("PINK_WOOL");
    public final static dMaterial GRAY_WOOL = new dMaterial(Material.WOOL, 7).forceIdentifyAs("GRAY_WOOL");
    public final static dMaterial LIGHT_GRAY_WOOL = new dMaterial(Material.WOOL, 8).forceIdentifyAs("LIGHT_GRAY_WOOL");
    public final static dMaterial CYAN_WOOL = new dMaterial(Material.WOOL, 9).forceIdentifyAs("CYAN_WOOL");
    public final static dMaterial PURPLE_WOOL = new dMaterial(Material.WOOL, 10).forceIdentifyAs("PURPLE_WOOL");
    public final static dMaterial BLUE_WOOL = new dMaterial(Material.WOOL, 11).forceIdentifyAs("BLUE_WOOL");
    public final static dMaterial BROWN_WOOL = new dMaterial(Material.WOOL, 12).forceIdentifyAs("BROWN_WOOL");
    public final static dMaterial GREEN_WOOL = new dMaterial(Material.WOOL, 13).forceIdentifyAs("GREEN_WOOL");
    public final static dMaterial RED_WOOL = new dMaterial(Material.WOOL, 14).forceIdentifyAs("RED_WOOL");
    public final static dMaterial BLACK_WOOL = new dMaterial(Material.WOOL, 15).forceIdentifyAs("BLACK_WOOL");

    // Colored Carpets
    public final static dMaterial WHITE_CARPET = new dMaterial(Material.CARPET, 0).forceIdentifyAs("WHITE_CARPET");
    public final static dMaterial ORANGE_CARPET = new dMaterial(Material.CARPET, 1).forceIdentifyAs("ORANGE_CARPET");
    public final static dMaterial MAGENTA_CARPET = new dMaterial(Material.CARPET, 2).forceIdentifyAs("MAGENTA_CARPET");
    public final static dMaterial LIGHT_BLUE_CARPET = new dMaterial(Material.CARPET, 3).forceIdentifyAs("LIGHT_BLUE_CARPET");
    public final static dMaterial YELLOW_CARPET = new dMaterial(Material.CARPET, 4).forceIdentifyAs("YELLOW_CARPET");
    public final static dMaterial LIME_CARPET = new dMaterial(Material.CARPET, 5).forceIdentifyAs("LIME_CARPET");
    public final static dMaterial PINK_CARPET = new dMaterial(Material.CARPET, 6).forceIdentifyAs("PINK_CARPET");
    public final static dMaterial GRAY_CARPET = new dMaterial(Material.CARPET, 7).forceIdentifyAs("GRAY_CARPET");
    public final static dMaterial LIGHT_GRAY_CARPET = new dMaterial(Material.CARPET, 8).forceIdentifyAs("LIGHT_GRAY_CARPET");
    public final static dMaterial CYAN_CARPET = new dMaterial(Material.CARPET, 9).forceIdentifyAs("CYAN_CARPET");
    public final static dMaterial PURPLE_CARPET = new dMaterial(Material.CARPET, 10).forceIdentifyAs("PURPLE_CARPET");
    public final static dMaterial BLUE_CARPET = new dMaterial(Material.CARPET, 11).forceIdentifyAs("BLUE_CARPET");
    public final static dMaterial BROWN_CARPET = new dMaterial(Material.CARPET, 12).forceIdentifyAs("BROWN_CARPET");
    public final static dMaterial GREEN_CARPET = new dMaterial(Material.CARPET, 13).forceIdentifyAs("GREEN_CARPET");
    public final static dMaterial RED_CARPET = new dMaterial(Material.CARPET, 14).forceIdentifyAs("RED_CARPET");
    public final static dMaterial BLACK_CARPET = new dMaterial(Material.CARPET, 15).forceIdentifyAs("BLACK_CARPET");

    // Colored Clay
    public final static dMaterial WHITE_CLAY = new dMaterial(Material.STAINED_CLAY, 0).forceIdentifyAs("WHITE_CLAY");
    public final static dMaterial ORANGE_CLAY = new dMaterial(Material.STAINED_CLAY, 1).forceIdentifyAs("ORANGE_CLAY");
    public final static dMaterial MAGENTA_CLAY = new dMaterial(Material.STAINED_CLAY, 2).forceIdentifyAs("MAGENTA_CLAY");
    public final static dMaterial LIGHT_BLUE_CLAY = new dMaterial(Material.STAINED_CLAY, 3).forceIdentifyAs("LIGHT_BLUE_CLAY");
    public final static dMaterial YELLOW_CLAY = new dMaterial(Material.STAINED_CLAY, 4).forceIdentifyAs("YELLOW_CLAY");
    public final static dMaterial LIME_CLAY = new dMaterial(Material.STAINED_CLAY, 5).forceIdentifyAs("LIME_CLAY");
    public final static dMaterial PINK_CLAY = new dMaterial(Material.STAINED_CLAY, 6).forceIdentifyAs("PINK_CLAY");
    public final static dMaterial GRAY_CLAY = new dMaterial(Material.STAINED_CLAY, 7).forceIdentifyAs("GRAY_CLAY");
    public final static dMaterial LIGHT_GRAY_CLAY = new dMaterial(Material.STAINED_CLAY, 8).forceIdentifyAs("LIGHT_GRAY_CLAY");
    public final static dMaterial CYAN_CLAY = new dMaterial(Material.STAINED_CLAY, 9).forceIdentifyAs("CYAN_CLAY");
    public final static dMaterial PURPLE_CLAY = new dMaterial(Material.STAINED_CLAY, 10).forceIdentifyAs("PURPLE_CLAY");
    public final static dMaterial BLUE_CLAY = new dMaterial(Material.STAINED_CLAY, 11).forceIdentifyAs("BLUE_CLAY");
    public final static dMaterial BROWN_CLAY = new dMaterial(Material.STAINED_CLAY, 12).forceIdentifyAs("BROWN_CLAY");
    public final static dMaterial GREEN_CLAY = new dMaterial(Material.STAINED_CLAY, 13).forceIdentifyAs("GREEN_CLAY");
    public final static dMaterial RED_CLAY = new dMaterial(Material.STAINED_CLAY, 14).forceIdentifyAs("RED_CLAY");
    public final static dMaterial BLACK_CLAY = new dMaterial(Material.STAINED_CLAY, 15).forceIdentifyAs("BLACK_CLAY");

    // Stained Glass
    public final static dMaterial WHITE_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 0).forceIdentifyAs("WHITE_STAINED_GLASS");
    public final static dMaterial ORANGE_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 1).forceIdentifyAs("ORANGE_STAINED_GLASS");
    public final static dMaterial MAGENTA_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 2).forceIdentifyAs("MAGENTA_STAINED_GLASS");
    public final static dMaterial LIGHT_BLUE_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 3).forceIdentifyAs("LIGHT_BLUE_STAINED_GLASS");
    public final static dMaterial YELLOW_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 4).forceIdentifyAs("YELLOW_STAINED_GLASS");
    public final static dMaterial LIME_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 5).forceIdentifyAs("LIME_STAINED_GLASS");
    public final static dMaterial PINK_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 6).forceIdentifyAs("PINK_STAINED_GLASS");
    public final static dMaterial GRAY_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 7).forceIdentifyAs("GRAY_STAINED_GLASS");
    public final static dMaterial LIGHT_GRAY_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 8).forceIdentifyAs("LIGHT_GRAY_STAINED_GLASS");
    public final static dMaterial CYAN_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 9).forceIdentifyAs("CYAN_STAINED_GLASS");
    public final static dMaterial PURPLE_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 10).forceIdentifyAs("PURPLE_STAINED_GLASS");
    public final static dMaterial BLUE_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 11).forceIdentifyAs("BLUE_STAINED_GLASS");
    public final static dMaterial BROWN_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 12).forceIdentifyAs("BROWN_STAINED_GLASS");
    public final static dMaterial GREEN_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 13).forceIdentifyAs("GREEN_STAINED_GLASS");
    public final static dMaterial RED_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 14).forceIdentifyAs("RED_STAINED_GLASS");
    public final static dMaterial BLACK_STAINED_GLASS = new dMaterial(Material.STAINED_GLASS, 15).forceIdentifyAs("BLACK_STAINED_GLASS");

    // Stained Glass Panes
    public final static dMaterial WHITE_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 0).forceIdentifyAs("WHITE_STAINED_GLASS_PANE");
    public final static dMaterial ORANGE_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 1).forceIdentifyAs("ORANGE_STAINED_GLASS_PANE");
    public final static dMaterial MAGENTA_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 2).forceIdentifyAs("MAGENTA_STAINED_GLASS_PANE");
    public final static dMaterial LIGHT_BLUE_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 3).forceIdentifyAs("LIGHT_BLUE_STAINED_GLASS_PANE");
    public final static dMaterial YELLOW_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 4).forceIdentifyAs("YELLOW_STAINED_GLASS_PANE");
    public final static dMaterial LIME_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 5).forceIdentifyAs("LIME_STAINED_GLASS_PANE");
    public final static dMaterial PINK_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 6).forceIdentifyAs("PINK_STAINED_GLASS_PANE");
    public final static dMaterial GRAY_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 7).forceIdentifyAs("GRAY_STAINED_GLASS_PANE");
    public final static dMaterial LIGHT_GRAY_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 8).forceIdentifyAs("LIGHT_GRAY_STAINED_GLASS_PANE");
    public final static dMaterial CYAN_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 9).forceIdentifyAs("CYAN_STAINED_GLASS_PANE");
    public final static dMaterial PURPLE_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 10).forceIdentifyAs("PURPLE_STAINED_GLASS_PANE");
    public final static dMaterial BLUE_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 11).forceIdentifyAs("BLUE_STAINED_GLASS_PANE");
    public final static dMaterial BROWN_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 12).forceIdentifyAs("BROWN_STAINED_GLASS_PANE");
    public final static dMaterial GREEN_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 13).forceIdentifyAs("GREEN_STAINED_GLASS_PANE");
    public final static dMaterial RED_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 14).forceIdentifyAs("RED_STAINED_GLASS_PANE");
    public final static dMaterial BLACK_STAINED_GLASS_PANE = new dMaterial(Material.STAINED_GLASS_PANE, 15).forceIdentifyAs("BLACK_STAINED_GLASS_PANE");

    // Planks
    public final static dMaterial OAK_PLANKS = new dMaterial(Material.WOOD, 0).forceIdentifyAs("OAK_PLANKS");
    public final static dMaterial SPRUCE_PLANKS = new dMaterial(Material.WOOD, 1).forceIdentifyAs("SPRUCE_PLANKS");
    public final static dMaterial BIRCH_PLANKS = new dMaterial(Material.WOOD, 2).forceIdentifyAs("BIRCH_PLANKS");
    public final static dMaterial JUNGLE_PLANKS = new dMaterial(Material.WOOD, 3).forceIdentifyAs("JUNGLE_PLANKS");
    public final static dMaterial ACACIA_PLANKS = new dMaterial(Material.WOOD, 4).forceIdentifyAs("ACACIA_PLANKS");
    public final static dMaterial DARKOAK_PLANKS = new dMaterial(Material.WOOD, 5).forceIdentifyAs("DARKOAK_PLANKS");

    // Saplings
    public final static dMaterial OAK_SAPLING = new dMaterial(Material.SAPLING, 0).forceIdentifyAs("OAK_SAPLING");
    public final static dMaterial SPRUCE_SAPLING = new dMaterial(Material.SAPLING, 1).forceIdentifyAs("SPRUCE_SAPLING");
    public final static dMaterial BIRCH_SAPLING = new dMaterial(Material.SAPLING, 2).forceIdentifyAs("BIRCH_SAPLING");
    public final static dMaterial JUNGLE_SAPLING = new dMaterial(Material.SAPLING, 3).forceIdentifyAs("JUNGLE_SAPLING");
    public final static dMaterial ACACIA_SAPLING = new dMaterial(Material.SAPLING, 4).forceIdentifyAs("ACACIA_SAPLING");
    public final static dMaterial DARKOAK_SAPLING = new dMaterial(Material.SAPLING, 5).forceIdentifyAs("DARKOAK_SAPLING");

    // Leaves
    public final static dMaterial OAK_LEAVES = new dMaterial(Material.LEAVES, 0).forceIdentifyAs("OAK_LEAVES");
    public final static dMaterial SPRUCE_LEAVES = new dMaterial(Material.LEAVES, 1).forceIdentifyAs("SPRUCE_LEAVES");
    public final static dMaterial BIRCH_LEAVES = new dMaterial(Material.LEAVES, 2).forceIdentifyAs("BIRCH_LEAVES");
    public final static dMaterial JUNGLE_LEAVES = new dMaterial(Material.LEAVES, 3).forceIdentifyAs("JUNGLE_LEAVES");
    public final static dMaterial ACACIA_LEAVES = new dMaterial(Material.LEAVES_2, 0).forceIdentifyAs("ACACIA_LEAVES");
    public final static dMaterial DARKOAK_LEAVES = new dMaterial(Material.LEAVES_2, 1).forceIdentifyAs("DARKOAK_LEAVES");
    public final static dMaterial PLACED_OAK_LEAVES = new dMaterial(Material.LEAVES, 4).forceIdentifyAs("PLACED_OAK_LEAVES");
    public final static dMaterial PLACED_SPRUCE_LEAVES = new dMaterial(Material.LEAVES, 5).forceIdentifyAs("PLACED_SPRUCE_LEAVES");
    public final static dMaterial PLACED_BIRCH_LEAVES = new dMaterial(Material.LEAVES, 6).forceIdentifyAs("PLACED_BIRCH_LEAVES");
    public final static dMaterial PLACED_JUNGLE_LEAVES = new dMaterial(Material.LEAVES, 7).forceIdentifyAs("PLACED_JUNGLE_LEAVES");
    public final static dMaterial PLACED_ACACIA_LEAVES = new dMaterial(Material.LEAVES_2, 4).forceIdentifyAs("PLACED_ACACIA_LEAVES");
    public final static dMaterial PLACED_DARKOAK_LEAVES = new dMaterial(Material.LEAVES_2, 5).forceIdentifyAs("PLACED_DARKOAK_LEAVES");

    // Logs
    public final static dMaterial OAK_LOG = new dMaterial(Material.LOG, 0).forceIdentifyAs("OAK_LOG");
    public final static dMaterial SPRUCE_LOG = new dMaterial(Material.LOG, 1).forceIdentifyAs("SPRUCE_LOG");
    public final static dMaterial BIRCH_LOG = new dMaterial(Material.LOG, 2).forceIdentifyAs("BIRCH_LOG");
    public final static dMaterial JUNGLE_LOG = new dMaterial(Material.LOG, 3).forceIdentifyAs("JUNGLE_LOG");
    public final static dMaterial ACACIA_LOG = new dMaterial(Material.LOG_2, 0).forceIdentifyAs("ACACIA_LOG");
    public final static dMaterial DARKOAK_LOG = new dMaterial(Material.LOG_2, 1).forceIdentifyAs("DARKOAK_LOG");
    public final static dMaterial OAK_LOG_EAST = new dMaterial(Material.LOG, 4).forceIdentifyAs("OAK_LOG_EAST");
    public final static dMaterial SPRUCE_LOG_EAST = new dMaterial(Material.LOG, 5).forceIdentifyAs("SPRUCE_LOG_EAST");
    public final static dMaterial BIRCH_LOG_EAST = new dMaterial(Material.LOG, 6).forceIdentifyAs("BIRCH_LOG_EAST");
    public final static dMaterial JUNGLE_LOG_EAST = new dMaterial(Material.LOG, 7).forceIdentifyAs("JUNGLE_LOG_EAST");
    public final static dMaterial ACACIA_LOG_EAST = new dMaterial(Material.LOG_2, 4).forceIdentifyAs("ACACIA_LOG_EAST");
    public final static dMaterial DARKOAK_LOG_EAST = new dMaterial(Material.LOG_2, 5).forceIdentifyAs("DARKOAK_LOG_EAST");
    public final static dMaterial OAK_LOG_NORTH = new dMaterial(Material.LOG, 8).forceIdentifyAs("OAK_LOG_NORTH");
    public final static dMaterial SPRUCE_LOG_NORTH = new dMaterial(Material.LOG, 9).forceIdentifyAs("SPRUCE_LOG_NORTH");
    public final static dMaterial BIRCH_LOG_NORTH = new dMaterial(Material.LOG, 10).forceIdentifyAs("BIRCH_LOG_NORTH");
    public final static dMaterial JUNGLE_LOG_NORTH = new dMaterial(Material.LOG, 11).forceIdentifyAs("JUNGLE_LOG_NORTH");
    public final static dMaterial ACACIA_LOG_NORTH = new dMaterial(Material.LOG_2, 8).forceIdentifyAs("ACACIA_LOG_NORTH");
    public final static dMaterial DARKOAK_LOG_NORTH = new dMaterial(Material.LOG_2, 9).forceIdentifyAs("DARKOAK_LOG_NORTH");
    public final static dMaterial OAK_LOG_BALL = new dMaterial(Material.LOG, 12).forceIdentifyAs("OAK_LOG_BALL");
    public final static dMaterial SPRUCE_LOG_BALL = new dMaterial(Material.LOG, 13).forceIdentifyAs("SPRUCE_LOG_BALL");
    public final static dMaterial BIRCH_LOG_BALL = new dMaterial(Material.LOG, 14).forceIdentifyAs("BIRCH_LOG_BALL");
    public final static dMaterial JUNGLE_LOG_BALL = new dMaterial(Material.LOG, 15).forceIdentifyAs("JUNGLE_LOG_BALL");
    public final static dMaterial ACACIA_LOG_BALL = new dMaterial(Material.LOG_2, 12).forceIdentifyAs("ACACIA_LOG_BALL");
    public final static dMaterial DARKOAK_LOG_BALL = new dMaterial(Material.LOG_2, 13).forceIdentifyAs("DARKOAK_LOG_BALL");

    // Sandstone
    public final static dMaterial CHISELED_SANDSTONE = new dMaterial(Material.SANDSTONE, 1).forceIdentifyAs("CHISELED_SANDSTONE");
    public final static dMaterial SMOOTH_SANDSTONE = new dMaterial(Material.SANDSTONE, 2).forceIdentifyAs("SMOOTH_SANDSTONE");

    // Stone Bricks
    public final static dMaterial STONE_BRICK = new dMaterial(Material.SMOOTH_BRICK, 0).forceIdentifyAs("STONE_BRICK");
    public final static dMaterial MOSSY_STONE_BRICK = new dMaterial(Material.SMOOTH_BRICK, 1).forceIdentifyAs("MOSSY_STONE_BRICK");
    public final static dMaterial CRACKED_STONE_BRICK = new dMaterial(Material.SMOOTH_BRICK, 2).forceIdentifyAs("CRACKED_STONE_BRICK");
    public final static dMaterial CHISELED_STONE_BRICK = new dMaterial(Material.SMOOTH_BRICK, 3).forceIdentifyAs("CHISELED_STONE_BRICK");

    // Quartz Block
    public final static dMaterial CHISELED_QUARTZ_BLOCK = new dMaterial(Material.QUARTZ_BLOCK, 1).forceIdentifyAs("CHISELED_QUARTZ_BLOCK");
    public final static dMaterial PILLAR_QUARTZ_BLOCK = new dMaterial(Material.QUARTZ_BLOCK, 2).forceIdentifyAs("PILLAR_QUARTZ_BLOCK");
    public final static dMaterial PILLAR_QUARTZ_BLOCK_EAST = new dMaterial(Material.QUARTZ_BLOCK, 3).forceIdentifyAs("PILLAR_QUARTZ_BLOCK_EAST");
    public final static dMaterial PILLAR_QUARTZ_BLOCK_NORTH = new dMaterial(Material.QUARTZ_BLOCK, 4).forceIdentifyAs("PILLAR_QUARTZ_BLOCK_NORTH");

    // Colored Ink
    public final static dMaterial INK = new dMaterial(Material.INK_SACK, 0).forceIdentifyAs("INK");
    public final static dMaterial RED_DYE = new dMaterial(Material.INK_SACK, 1).forceIdentifyAs("RED_DYE");
    public final static dMaterial GREEN_DYE = new dMaterial(Material.INK_SACK, 2).forceIdentifyAs("GREEN_DYE");
    public final static dMaterial COCOA_BEANS = new dMaterial(Material.INK_SACK, 3).forceIdentifyAs("COCOA_BEANS");
    public final static dMaterial LAPIS_LAZULI = new dMaterial(Material.INK_SACK, 4).forceIdentifyAs("LAPIS_LAZULI");
    public final static dMaterial PURPLE_DYE = new dMaterial(Material.INK_SACK, 5).forceIdentifyAs("PURPLE_DYE");
    public final static dMaterial CYAN_DYE = new dMaterial(Material.INK_SACK, 6).forceIdentifyAs("CYAN_DYE");
    public final static dMaterial LIGHT_GRAY_DYE = new dMaterial(Material.INK_SACK, 7).forceIdentifyAs("LIGHT_GRAY_DYE");
    public final static dMaterial GRAY_DYE = new dMaterial(Material.INK_SACK, 8).forceIdentifyAs("GRAY_DYE");
    public final static dMaterial PINK_DYE = new dMaterial(Material.INK_SACK, 9).forceIdentifyAs("PINK_DYE");
    public final static dMaterial LIME_DYE = new dMaterial(Material.INK_SACK, 10).forceIdentifyAs("LIME_DYE");
    public final static dMaterial YELLOW_DYE = new dMaterial(Material.INK_SACK, 11).forceIdentifyAs("YELLOW_DYE");
    public final static dMaterial LIGHT_BLUE_DYE = new dMaterial(Material.INK_SACK, 12).forceIdentifyAs("LIGHT_BLUE_DYE");
    public final static dMaterial MAGENTA_DYE = new dMaterial(Material.INK_SACK, 13).forceIdentifyAs("MAGENTA_DYE");
    public final static dMaterial ORANGE_DYE = new dMaterial(Material.INK_SACK, 14).forceIdentifyAs("ORANGE_DYE");
    public final static dMaterial BONE_MEAL = new dMaterial(Material.INK_SACK, 15).forceIdentifyAs("BONE_MEAL");

    // Tall Grass
    public final static dMaterial SHRUB = new dMaterial(Material.LONG_GRASS, 0).forceIdentifyAs("SHRUB");
    public final static dMaterial TALL_GRASS = new dMaterial(Material.LONG_GRASS, 1).forceIdentifyAs("TALL_GRASS");
    public final static dMaterial FERN = new dMaterial(Material.LONG_GRASS, 2).forceIdentifyAs("FERN");

    // Flowers
    public final static dMaterial POPPY = new dMaterial(Material.RED_ROSE, 0).forceIdentifyAs("POPPY");
    public final static dMaterial BLUE_ORCHID = new dMaterial(Material.RED_ROSE, 1).forceIdentifyAs("BLUE_ORCHID");
    public final static dMaterial ALLIUM = new dMaterial(Material.RED_ROSE, 2).forceIdentifyAs("ALLIUM");
    public final static dMaterial AZURE_BLUET = new dMaterial(Material.RED_ROSE, 3).forceIdentifyAs("AZURE_BLUET");
    public final static dMaterial RED_TULIP = new dMaterial(Material.RED_ROSE, 4).forceIdentifyAs("RED_TULIP");
    public final static dMaterial ORANGE_TULIP = new dMaterial(Material.RED_ROSE, 5).forceIdentifyAs("ORANGE_TULIP");
    public final static dMaterial WHITE_TULIP = new dMaterial(Material.RED_ROSE, 6).forceIdentifyAs("WHITE_TULIP");
    public final static dMaterial PINK_TULIP = new dMaterial(Material.RED_ROSE, 7).forceIdentifyAs("PINK_TULIP");
    public final static dMaterial OXEYE_DAISY = new dMaterial(Material.RED_ROSE, 8).forceIdentifyAs("OXEYE_DAISY");

    // Double-tall Plants
    public final static dMaterial SUNFLOWER = new dMaterial(Material.DOUBLE_PLANT, 0).forceIdentifyAs("SUNFLOWER");
    public final static dMaterial LILAC = new dMaterial(Material.DOUBLE_PLANT, 1).forceIdentifyAs("LILAC");
    public final static dMaterial DOUBLE_TALLGRASS = new dMaterial(Material.DOUBLE_PLANT, 2).forceIdentifyAs("DOUBLE_TALLGRASS");
    public final static dMaterial LARGE_FERN = new dMaterial(Material.DOUBLE_PLANT, 3).forceIdentifyAs("LARGE_FERN");
    public final static dMaterial ROSE_BUSH = new dMaterial(Material.DOUBLE_PLANT, 4).forceIdentifyAs("ROSE_BUSH");
    public final static dMaterial PEONY = new dMaterial(Material.DOUBLE_PLANT, 5).forceIdentifyAs("PEONY");
    public final static dMaterial DOUBLEPLANT_TOP = new dMaterial(Material.DOUBLE_PLANT, 8).forceIdentifyAs("DOUBLEPLANT_TOP");

    // Potted Plants
    public final static dMaterial EMPTY_POT = new dMaterial(Material.FLOWER_POT, 0).forceIdentifyAs("EMPTY_POT");
    public final static dMaterial POTTED_POPPY = new dMaterial(Material.FLOWER_POT, 1).forceIdentifyAs("POTTED_POPPY");
    public final static dMaterial POTTED_DAISY = new dMaterial(Material.FLOWER_POT, 2).forceIdentifyAs("POTTED_DAISY");
    public final static dMaterial POTTED_OAK_SAPLING = new dMaterial(Material.FLOWER_POT, 3).forceIdentifyAs("POTTED_OAK_SAPLING");
    public final static dMaterial POTTED_SPRUCE_SAPLING = new dMaterial(Material.FLOWER_POT, 4).forceIdentifyAs("POTTED_SPRUCE_SAPLING");
    public final static dMaterial POTTED_BIRCH_SAPLING = new dMaterial(Material.FLOWER_POT, 5).forceIdentifyAs("POTTED_BIRCH_SAPLING");
    public final static dMaterial POTTED_JUNGLE_SAPLING = new dMaterial(Material.FLOWER_POT, 6).forceIdentifyAs("POTTED_JUNGLE_SAPLING");
    public final static dMaterial POTTED_RED_MUSHROOM = new dMaterial(Material.FLOWER_POT, 7).forceIdentifyAs("POTTED_RED_MUSHROOM");
    public final static dMaterial POTTED_BROWN_MUSHROOM = new dMaterial(Material.FLOWER_POT, 8).forceIdentifyAs("POTTED_BROWN_MUSHROOM");
    public final static dMaterial POTTED_CACTUS = new dMaterial(Material.FLOWER_POT, 9).forceIdentifyAs("POTTED_CACTUS");
    public final static dMaterial POTTED_SHRUB = new dMaterial(Material.FLOWER_POT, 10).forceIdentifyAs("POTTED_SHRUB");
    public final static dMaterial POTTED_FERN = new dMaterial(Material.FLOWER_POT, 11).forceIdentifyAs("POTTED_FERN");
    public final static dMaterial POTTED_ACACIA_SAPLING = new dMaterial(Material.FLOWER_POT, 12).forceIdentifyAs("POTTED_ACACIA_SAPLING");
    public final static dMaterial POTTED_DARKOAK_SAPLING = new dMaterial(Material.FLOWER_POT, 13).forceIdentifyAs("POTTED_DARKOAK_SAPLING");

    // Steps
    public final static dMaterial STONE_SLAB = new dMaterial(Material.STEP, 0).forceIdentifyAs("STONE_SLAB");
    public final static dMaterial SANDSTONE_SLAB = new dMaterial(Material.STEP, 1).forceIdentifyAs("SANDSTONE_SLAB");
    public final static dMaterial WOODEN_SLAB = new dMaterial(Material.STEP, 2).forceIdentifyAs("WOODEN_SLAB");
    public final static dMaterial COBBLESTONE_SLAB = new dMaterial(Material.STEP, 3).forceIdentifyAs("COBBLESTONE_SLAB");
    public final static dMaterial BRICKS_SLAB = new dMaterial(Material.STEP, 4).forceIdentifyAs("BRICKS_SLAB");
    public final static dMaterial STONEBRICKS_SLAB = new dMaterial(Material.STEP, 5).forceIdentifyAs("STONEBRICKS_SLAB");
    public final static dMaterial NETHERBRICK_SLAB = new dMaterial(Material.STEP, 6).forceIdentifyAs("NETHERBRICK_SLAB");
    public final static dMaterial QUARTZ_SLAB = new dMaterial(Material.STEP, 7).forceIdentifyAs("QUARTZ_SLAB");
    public final static dMaterial STONE_SLAB_UP = new dMaterial(Material.STEP, 8).forceIdentifyAs("STONE_SLAB_UP");
    public final static dMaterial SANDSTONE_SLAB_UP = new dMaterial(Material.STEP, 9).forceIdentifyAs("SANDSTONE_SLAB_UP");
    public final static dMaterial WOODEN_SLAB_UP = new dMaterial(Material.STEP, 10).forceIdentifyAs("WOODEN_SLAB_UP");
    public final static dMaterial COBBLESTONE_SLAB_UP = new dMaterial(Material.STEP, 11).forceIdentifyAs("COBBLESTONE_SLAB_UP");
    public final static dMaterial BRICKS_SLAB_UP = new dMaterial(Material.STEP, 12).forceIdentifyAs("BRICKS_SLAB_UP");
    public final static dMaterial STONEBRICKS_SLAB_UP = new dMaterial(Material.STEP, 13).forceIdentifyAs("STONEBRICKS_SLAB_UP");
    public final static dMaterial NETHERBRICK_SLAB_UP = new dMaterial(Material.STEP, 14).forceIdentifyAs("NETHERBRICK_SLAB_UP");
    public final static dMaterial QUARTZ_SLAB_UP = new dMaterial(Material.STEP, 15).forceIdentifyAs("QUARTZ_SLAB_UP");

    // Wood Steps
    public final static dMaterial OAK_WOOD_SLAB = new dMaterial(Material.WOOD_STEP, 0).forceIdentifyAs("OAK_WOOD_SLAB");
    public final static dMaterial SPRUCE_WOOD_SLAB = new dMaterial(Material.WOOD_STEP, 1).forceIdentifyAs("SPRUCE_WOOD_SLAB");
    public final static dMaterial BIRCH_WOOD_SLAB = new dMaterial(Material.WOOD_STEP, 2).forceIdentifyAs("BIRCH_WOOD_SLAB");
    public final static dMaterial JUNGLE_WOOD_SLAB = new dMaterial(Material.WOOD_STEP, 3).forceIdentifyAs("JUNGLE_WOOD_SLAB");
    public final static dMaterial ACACIA_WOOD_SLAB = new dMaterial(Material.WOOD_STEP, 4).forceIdentifyAs("ACACIA_WOOD_SLAB");
    public final static dMaterial DARKOAK_WOOD_SLAB = new dMaterial(Material.WOOD_STEP, 5).forceIdentifyAs("DARKOAK_WOOD_SLAB");
    public final static dMaterial OAK_WOOD_SLAB_UP = new dMaterial(Material.WOOD_STEP, 8).forceIdentifyAs("OAK_WOOD_SLAB_UP");
    public final static dMaterial SPRUCE_WOOD_SLAB_UP = new dMaterial(Material.WOOD_STEP, 9).forceIdentifyAs("SPRUCE_WOOD_SLAB_UP");
    public final static dMaterial BIRCH_WOOD_SLAB_UP = new dMaterial(Material.WOOD_STEP, 10).forceIdentifyAs("BIRCH_WOOD_SLAB_UP");
    public final static dMaterial JUNGLE_WOOD_SLAB_UP = new dMaterial(Material.WOOD_STEP, 11).forceIdentifyAs("JUNGLE_WOOD_SLAB_UP");
    public final static dMaterial ACACIA_WOOD_SLAB_UP = new dMaterial(Material.WOOD_STEP, 12).forceIdentifyAs("ACACIA_WOOD_SLAB_UP");
    public final static dMaterial DARKOAK_WOOD_SLAB_UP = new dMaterial(Material.WOOD_STEP, 13).forceIdentifyAs("DARKOAK_WOOD_SLAB_UP");

    // Double Steps
    public final static dMaterial STONE_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 0).forceIdentifyAs("STONE_DOUBLESLAB");
    public final static dMaterial SANDSTONE_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 1).forceIdentifyAs("SANDSTONE_DOUBLESLAB");
    public final static dMaterial WOODEN_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 2).forceIdentifyAs("WOODEN_DOUBLESLAB");
    public final static dMaterial COBBLESTONE_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 3).forceIdentifyAs("COBBLESTONE_DOUBLESLAB");
    public final static dMaterial BRICKS_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 4).forceIdentifyAs("BRICKS_DOUBLESLAB");
    public final static dMaterial STONEBRICKS_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 5).forceIdentifyAs("STONEBRICKS_DOUBLESLAB");
    public final static dMaterial NETHERBRICK_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 6).forceIdentifyAs("NETHERBRICK_DOUBLESLAB");
    public final static dMaterial QUARTZ_DOUBLESLAB = new dMaterial(Material.DOUBLE_STEP, 7).forceIdentifyAs("QUARTZ_DOUBLESLAB");
    public final static dMaterial STONE_SLABBALL = new dMaterial(Material.DOUBLE_STEP, 8).forceIdentifyAs("STONE_SLABBALL");
    public final static dMaterial SANDSTONE_SLABBALL = new dMaterial(Material.DOUBLE_STEP, 9).forceIdentifyAs("SANDSTONE_SLABBALL");

    // Wood Double Steps
    public final static dMaterial OAK_WOOD_DOUBLESLAB = new dMaterial(Material.WOOD_DOUBLE_STEP, 0).forceIdentifyAs("OAK_WOOD_DOUBLESLAB");
    public final static dMaterial SPRUCE_WOOD_DOUBLESLAB = new dMaterial(Material.WOOD_DOUBLE_STEP, 1).forceIdentifyAs("SPRUCE_WOOD_DOUBLESLAB");
    public final static dMaterial BIRCH_WOOD_DOUBLESLAB = new dMaterial(Material.WOOD_DOUBLE_STEP, 2).forceIdentifyAs("BIRCH_WOOD_DOUBLESLAB");
    public final static dMaterial JUNGLE_WOOD_DOUBLESLAB = new dMaterial(Material.WOOD_DOUBLE_STEP, 3).forceIdentifyAs("JUNGLE_WOOD_DOUBLESLAB");
    public final static dMaterial ACACIA_WOOD_DOUBLESLAB = new dMaterial(Material.WOOD_DOUBLE_STEP, 4).forceIdentifyAs("ACACIA_WOOD_DOUBLESLAB");
    public final static dMaterial DARKOAK_WOOD_DOUBLESLAB = new dMaterial(Material.WOOD_DOUBLE_STEP, 5).forceIdentifyAs("DARKOAK_WOOD_DOUBLESLAB");

    // Skull Items
    public final static dMaterial SKELETON_SKULL = new dMaterial(Material.SKULL_ITEM, 0).forceIdentifyAs("SKELETON_SKULL");
    public final static dMaterial WITHERSKELETON_SKULL = new dMaterial(Material.SKULL_ITEM, 1).forceIdentifyAs("WITHERSKELETON_SKULL");
    public final static dMaterial ZOMBIE_SKULL = new dMaterial(Material.SKULL_ITEM, 2).forceIdentifyAs("ZOMBIE_SKULL");
    public final static dMaterial HUMAN_SKULL = new dMaterial(Material.SKULL_ITEM, 3).forceIdentifyAs("HUMAN_SKULL");
    public final static dMaterial CREEPER_SKULL = new dMaterial(Material.SKULL_ITEM, 4).forceIdentifyAs("CREEPER_SKULL");

    // Monster Eggs
    // TODO: I will add rest of eggs (I needed this one quick for a project)
    public final static dMaterial SKELETON_EGG = new dMaterial(Material.MONSTER_EGG, 51).forceIdentifyAs("SKELETON_EGG");

    // Fish
    public final static dMaterial COOKED_FISH = new dMaterial(Material.COOKED_FISH, 0).forceIdentifyAs("COOKED_FISH");
    public final static dMaterial COOKED_SALMON = new dMaterial(Material.COOKED_FISH, 1).forceIdentifyAs("COOKED_SALMON");
    public final static dMaterial COOKED_CLOWNFISH = new dMaterial(Material.COOKED_FISH, 2).forceIdentifyAs("COOKED_CLOWNFISH");
    public final static dMaterial COOKED_PUFFERFISH = new dMaterial(Material.COOKED_FISH, 3).forceIdentifyAs("COOKED_PUFFERFISH");
    public final static dMaterial RAW_FISH = new dMaterial(Material.RAW_FISH, 0).forceIdentifyAs("RAW_FISH");
    public final static dMaterial RAW_SALMON = new dMaterial(Material.RAW_FISH, 1).forceIdentifyAs("RAW_SALMON");
    public final static dMaterial RAW_CLOWNFISH = new dMaterial(Material.RAW_FISH, 2).forceIdentifyAs("RAW_CLOWNFISH");
    public final static dMaterial RAW_PUFFERFISH = new dMaterial(Material.RAW_FISH, 3).forceIdentifyAs("RAW_PUFFERFISH");

    // One-Offs (Don't have their own group)
    public final static dMaterial CHARCOAL = new dMaterial(Material.COAL, 1).forceIdentifyAs("CHARCOAL");
    public final static dMaterial RED_SAND = new dMaterial(Material.SAND, 1).forceIdentifyAs("RED_SAND");
    public final static dMaterial COARSE_DIRT = new dMaterial(Material.DIRT, 1).forceIdentifyAs("COARSE_DIRT");
    public final static dMaterial PODZOL = new dMaterial(Material.DIRT, 2).forceIdentifyAs("PODZOL");
    public final static dMaterial MOSSY_COBBLE_WALL = new dMaterial(Material.COBBLE_WALL, 1).forceIdentifyAs("MOSSY_COBBLE_WALL");

    // Stone
    // GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ADESITE
    public final static dMaterial GRANITE = new dMaterial(Material.STONE, 1).forceIdentifyAs("GRANITE");
    public final static dMaterial POLISHED_GRANITE = new dMaterial(Material.STONE, 2).forceIdentifyAs("POLISHED_GRANITE");
    public final static dMaterial DIORITE = new dMaterial(Material.STONE, 3).forceIdentifyAs("DIORITE");
    public final static dMaterial POLISHED_DIORITE = new dMaterial(Material.STONE, 4).forceIdentifyAs("POLISHED_DIORITE");
    public final static dMaterial ANDESITE = new dMaterial(Material.STONE, 5).forceIdentifyAs("ANDESITE");
    public final static dMaterial POLISHED_ANDESITE = new dMaterial(Material.STONE, 6).forceIdentifyAs("POLISHED_ANDESITE");

    // TODO: The following would be walls of useless materials, make properties for these instead of custom mats
    // Step rotations [rotation=(north/west/south/east)(up/down)] for each of the step blocks
    // Rotations for chests/furnaces/pumpkins/cocoa/etc [rotation=(north/south/east/west)] for each of those types


    // Built on startup's call to initialize_ based on the dMaterials enum and available
    // 'final static' dMaterial fields.
    // valueOf and getMaterialFrom will check this to turn 'wool,1' into 'orange_wool'
    public static Map<Material, Map<Integer, dMaterial>> material_varieties = new HashMap<Material, Map<Integer, dMaterial>>();

    public static Map<String, dMaterial> all_dMaterials = new HashMap<String, dMaterial>();

    /**
     * Registers a dMaterial as a 'variety'. Upon construction of a dMaterial, this
     * registry will be checked to see if a variety can be used instead of the traditional
     * enum/data format.
     * <p/>
     * dMaterials in this list should probably 'forceIdentifyAs'.
     *
     * @param material the dMaterial variety
     * @return the dMaterial registered
     */
    public static dMaterial registerVariety(dMaterial material) {
        Map<Integer, dMaterial> entry;
        // Get any existing entries for the Material, or make a new HashMap for entries.
        if (material_varieties.containsKey(material.getMaterial())) {
            entry = material_varieties.get(material.getMaterial());
        }
        else {
            entry = new HashMap<Integer, dMaterial>();
        }
        // Put in new entry
        entry.put((int) material.data, material);
        // Return the dMaterial
        material_varieties.put(material.getMaterial(), entry);
        all_dMaterials.put(material.realName().toUpperCase(), material);
        return material;
    }

    // Called on startup
    public static void _initialize() {
        for (dMaterials material : dMaterials.values()) {
            try {
                Field field = dMaterial.class.getField(material.name());
                dMaterial mat = (dMaterial) field.get(null);
                registerVariety(mat);
            }
            catch (Exception e) {
                dB.echoError(e);
            }
        }
    }

    // dMaterials that are registered as a 'variety' will need to identify as
    // something more specific than the traditional enum/data information.
    private String forcedIdentity = null;
    private String forcedIdentityLow = null;

    /**
     * Forces the dMaterial to identify as something other than the enum value
     * of the material. This should be used on materials that are being registered
     * as a variety.
     *
     * @param string the name of the new identity
     * @return the identified dMaterial
     */
    private dMaterial forceIdentifyAs(String string) {
        forcedIdentity = string;
        forcedIdentityLow = CoreUtilities.toLowerCase(string);
        return this;
    }


    /**
     * Gets a dMaterial from a bukkit Material.
     *
     * @param material the bukkit Material
     * @return a dMaterial representation of the Material
     */
    public static dMaterial getMaterialFrom(Material material) {
        return getMaterialFrom(material, 0);
    }

    /**
     * Gets a dMaterial from a Bukkit Material/Data. dMaterials can identify
     * as something more straight-forward than the traditional material,data format.
     * Example: wool,1 would return the ORANGE_WOOL dMaterial.
     *
     * @param material the base Bukkit material
     * @param data     the datavalue to use
     * @return a dMaterial representation of the input Bukkit material
     */
    public static dMaterial getMaterialFrom(Material material, int data) {
        if (material == Material.AIR) {
            return AIR;
        }
        if (material_varieties.containsKey(material)) {
            if (material_varieties.get(material).containsKey(data)) {
                return material_varieties.get(material).get(data);
            }
        }

        return new dMaterial(material, data);
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////


    public static dMaterial valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Material Object from a string form.
     *
     * @param string the string
     * @return a Material, or null if incorrectly formatted
     */
    @Fetchable("m")
    public static dMaterial valueOf(String string, TagContext context) {

        if (string.toLowerCase().matches("random")
                || string.toLowerCase().matches("m@random")) {

            // Get a random material
            return new dMaterial(Material.values()[CoreUtilities.getRandom().nextInt(Material.values().length)]);
        }

        Matcher m = materialPattern.matcher(string);

        if (m.matches()) {
            int data = 0;
            if (m.group(2) != null) {
                data = aH.getIntegerFrom(m.group(2));
            }

            String materialName = m.group(1);

            if (aH.matchesInteger(materialName)) {
                return dMaterial.getMaterialFrom(Material.getMaterial(aH.getIntegerFrom(materialName)), data);
            }
            else {
                // Iterate through Materials
                for (Material material : Material.values()) {
                    if (material.name().equalsIgnoreCase(materialName)) {
                        return dMaterial.getMaterialFrom(material, data);
                    }
                }

                // Iterate through dMaterials
                dMaterial mat = all_dMaterials.get(materialName.toUpperCase());
                if (mat != null) {
                    return mat;
                }
            }
        }

        // No match
        return null;
    }

    /**
     * Determine whether a string is a valid material.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {

        // Avoid case sensitivity
        arg = arg.toUpperCase();

        if (arg.startsWith("M@")) {
            return true;
        }

        if (arg.matches("(?:M@)?RANDOM")) {
            return true;
        }

        Matcher m = materialPattern.matcher(arg);

        if (m.matches()) {

            String materialName = m.group(1);

            // If this argument is in an integer, return true if it does not
            // exceed the number of materials in Bukkit
            if (aH.matchesInteger(materialName)) {
                if (aH.getIntegerFrom(arg) < Material.values().length) {
                    return true;
                }
            }

            // Check if this argument matches a Material or a special stored
            // dMaterial's name
            else {
                // Iterate through Bukkit Materials
                for (Material material : Material.values()) {
                    if (material.name().equalsIgnoreCase(materialName)) {
                        return true;
                    }
                }

                // Iterate through dMaterials
                if (all_dMaterials.get(materialName) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param object object-fetchable String of a valid dMaterial, or a dMaterial object
     * @return true if the dMaterials are the same.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof dMaterial) {
            return ((dMaterial) object).identify().equals(this.identify());
        }
        else {
            dMaterial parsed = valueOf(object.toString());
            return parsed != null && parsed.identify().equals(this.identify());
        }
    }


    ///////////////
    //   Constructors
    /////////////

    private dMaterial(Material material, int data) {
        this.material = material;

        if (data < 0) {
            this.data = null;
        }
        else {
            this.data = (byte) data;
        }
    }

    private dMaterial(Material material) {
        this(material, 0);
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Material

    private Material material;
    private Byte data = 0;

    public Material getMaterial() {
        return material;
    }

    public String name() {
        return material.name();
    }


    public Byte getData(byte fallback) {
        if (data == null) {
            return fallback;
        }
        else {
            return data;
        }
    }

    public Byte getData() {
        return data;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean matchesMaterialData(MaterialData data) {
        // If this material has data, check datas
        if (hasData()) {
            return (material == data.getItemType() && this.data == data.getData());
        }

        // Else, return matched itemType/materialType
        else {
            return material == data.getItemType();
        }
    }

    public MaterialData getMaterialData() {
        return new MaterialData(material, data != null ? data : 0);
    }

    public boolean isStructure() {
        try {
            TreeType.valueOf(material.toString());
            return true;
        }
        catch (Exception e) {
            switch (material) {
                case SAPLING:
                case HUGE_MUSHROOM_1:
                case HUGE_MUSHROOM_2:
                    return true;
                default:
                    return false;
            }
        }
    }

    String prefix = "material";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "Material";
    }

    @Override
    public String identify() {
        if (forcedIdentity != null) {
            return "m@" + forcedIdentityLow;
        }
        if (getData() != null && getData() > 0) {
            return "m@" + material.name().toLowerCase() + "," + getData();
        }
        return "m@" + material.name().toLowerCase();
    }

    public String identifyFull() {
        if (forcedIdentity != null) {
            return "m@" + forcedIdentityLow + (getData() != null ? "," + getData() : "");
        }
        if (getData() != null && getData() > 0) {
            return "m@" + material.name().toLowerCase() + "," + getData();
        }
        return "m@" + material.name().toLowerCase();
    }

    @Override
    public String identifySimple() {
        if (forcedIdentity != null) {
            return "m@" + forcedIdentityLow;
        }
        return "m@" + material.name().toLowerCase();
    }

    public String identifyNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        if (getData() != null && getData() > 0) {
            return material.name().toLowerCase() + "," + getData();
        }
        return material.name().toLowerCase();
    }

    public String identifySimpleNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        return material.name().toLowerCase();
    }

    public String identifyFullNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow + (getData() != null ? "," + getData() : "");
        }
        if (getData() != null && getData() > 0) {
            return material.name().toLowerCase() + "," + getData();
        }
        return material.name().toLowerCase();
    }

    @Override
    public String toString() {
        return identify();
    }

    public String realName() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        return material.name().toLowerCase();
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <m@material.has_gravity>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is affected by gravity.
        // -->
        registerTag("has_gravity", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.hasGravity())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.id>
        // @returns Element(Number)
        // @description
        // Returns the material's ID.
        // -->
        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.getId())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_block>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a placeable block.
        // -->
        registerTag("is_block", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isBlock())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_burnable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that can burn away.
        // -->
        registerTag("is_burnable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isBurnable())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_edible>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is edible.
        // -->
        registerTag("is_edible", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isEdible())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_flammable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that can catch fire.
        // -->
        registerTag("is_flammable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isFlammable())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_occluding>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that completely blocks vision.
        // -->
        registerTag("is_occluding", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isOccluding())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_record>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a playable music disc.
        // -->
        registerTag("is_record", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isRecord())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_solid>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that is solid (cannot be walked through).
        // -->
        registerTag("is_solid", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(!SafeBlock.blockIsSafe(((dMaterial) object).material))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_transparent>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that does not block any light.
        // -->
        registerTag("is_transparent", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isTransparent())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.max_durability>
        // @returns Element(Number)
        // @description
        // Returns the maximum durability of this material.
        // -->
        registerTag("max_durability", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.getMaxDurability())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.max_stack_size>
        // @returns Element(Number)
        // @description
        // Returns the maximum amount of this material that can be held in a stack.
        // -->
        registerTag("max_stack_size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.getMaxStackSize())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_made_of[<material>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the material is a variety of the specified material.
        // Example: <m@red_wool.is_made_of[m@wool]> will return true.
        // -->
        registerTag("is_made_of", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dMaterial compared = dMaterial.valueOf(attribute.getContext(1));
                if (compared == null) {
                    return Element.FALSE.getAttribute(attribute.fulfill(1));
                }
                else {
                    return new Element(((dMaterial) object).material == compared.getMaterial())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });

        // <--[tag]
        // @attribute <m@material.bukkit_enum>
        // @returns Element
        // @description
        // Returns the bukkit Material enum value. For example: <m@birch_sapling.bukkit_enum>
        // will return 'sapling'
        // -->
        registerTag("bukkit_enum", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.name>
        // @returns Element
        // @description
        // Returns the name of the material.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).forcedIdentity != null ? ((dMaterial) object).forcedIdentityLow :
                        ((dMaterial) object).material.name().toLowerCase())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.full>
        // @returns Element
        // @description
        // Returns the material's full identification.
        // -->
        registerTag("full", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (((dMaterial) object).hasData()) {
                    return new Element(((dMaterial) object).identifyFull())
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new Element(((dMaterial) object).identify())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });

        // <--[tag]
        // @attribute <m@material.data>
        // @returns Element(Number)
        // @description
        // Returns the bukkit Material data value. For example: <m@red_clay.data>
        // will return '14'. Note: This kind of 'material identification' has been deprecated
        // by bukkit and should be used sparingly.
        // -->
        registerTag("data", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).getData())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.item>
        // @returns dItem
        // @description
        // Returns an item of the material.
        // -->
        registerTag("item", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new dItem(((dMaterial) object), 1)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.type>
        // @returns Element
        // @description
        // Always returns 'Material' for dMaterial objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Material").getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<String, TagRunnable>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }
}
