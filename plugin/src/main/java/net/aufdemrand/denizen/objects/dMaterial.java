package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.tags.BukkitTagContext;
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

public class dMaterial implements dObject, Adjustable {

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

    // dMaterials are just made and disposed of for standard 'Materials', but these we will keep around since
    // they are special :)

    private static final Map<Integer, Material> MATERIAL_BY_LEGACY_ID;

    static {
        MATERIAL_BY_LEGACY_ID = new HashMap<Integer, Material>();
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

    public static dMaterial getMaterialPre1_13(String material, int data, String name) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1)) {
            return new dMaterial(Material.valueOf(material), data).forceIdentifyAs(name);
        }
        return null;
    }

    // Colored Wool
    public final static dMaterial WHITE_WOOL = getMaterialPre1_13("WOOL", 0, "WHITE_WOOL");
    public final static dMaterial ORANGE_WOOL = getMaterialPre1_13("WOOL", 1, "ORANGE_WOOL");
    public final static dMaterial MAGENTA_WOOL = getMaterialPre1_13("WOOL", 2, "MAGENTA_WOOL");
    public final static dMaterial LIGHT_BLUE_WOOL = getMaterialPre1_13("WOOL", 3, "LIGHT_BLUE_WOOL");
    public final static dMaterial YELLOW_WOOL = getMaterialPre1_13("WOOL", 4, "YELLOW_WOOL");
    public final static dMaterial LIME_WOOL = getMaterialPre1_13("WOOL", 5, "LIME_WOOL");
    public final static dMaterial PINK_WOOL = getMaterialPre1_13("WOOL", 6, "PINK_WOOL");
    public final static dMaterial GRAY_WOOL = getMaterialPre1_13("WOOL", 7, "GRAY_WOOL");
    public final static dMaterial LIGHT_GRAY_WOOL = getMaterialPre1_13("WOOL", 8, "LIGHT_GRAY_WOOL");
    public final static dMaterial CYAN_WOOL = getMaterialPre1_13("WOOL", 9, "CYAN_WOOL");
    public final static dMaterial PURPLE_WOOL = getMaterialPre1_13("WOOL", 10, "PURPLE_WOOL");
    public final static dMaterial BLUE_WOOL = getMaterialPre1_13("WOOL", 11, "BLUE_WOOL");
    public final static dMaterial BROWN_WOOL = getMaterialPre1_13("WOOL", 12, "BROWN_WOOL");
    public final static dMaterial GREEN_WOOL = getMaterialPre1_13("WOOL", 13, "GREEN_WOOL");
    public final static dMaterial RED_WOOL = getMaterialPre1_13("WOOL", 14, "RED_WOOL");
    public final static dMaterial BLACK_WOOL = getMaterialPre1_13("WOOL", 15, "BLACK_WOOL");

    // Colored Carpets
    public final static dMaterial WHITE_CARPET = getMaterialPre1_13("CARPET", 0, "WHITE_CARPET");
    public final static dMaterial ORANGE_CARPET = getMaterialPre1_13("CARPET", 1, "ORANGE_CARPET");
    public final static dMaterial MAGENTA_CARPET = getMaterialPre1_13("CARPET", 2, "MAGENTA_CARPET");
    public final static dMaterial LIGHT_BLUE_CARPET = getMaterialPre1_13("CARPET", 3, "LIGHT_BLUE_CARPET");
    public final static dMaterial YELLOW_CARPET = getMaterialPre1_13("CARPET", 4, "YELLOW_CARPET");
    public final static dMaterial LIME_CARPET = getMaterialPre1_13("CARPET", 5, "LIME_CARPET");
    public final static dMaterial PINK_CARPET = getMaterialPre1_13("CARPET", 6, "PINK_CARPET");
    public final static dMaterial GRAY_CARPET = getMaterialPre1_13("CARPET", 7, "GRAY_CARPET");
    public final static dMaterial LIGHT_GRAY_CARPET = getMaterialPre1_13("CARPET", 8, "LIGHT_GRAY_CARPET");
    public final static dMaterial CYAN_CARPET = getMaterialPre1_13("CARPET", 9, "CYAN_CARPET");
    public final static dMaterial PURPLE_CARPET = getMaterialPre1_13("CARPET", 10, "PURPLE_CARPET");
    public final static dMaterial BLUE_CARPET = getMaterialPre1_13("CARPET", 11, "BLUE_CARPET");
    public final static dMaterial BROWN_CARPET = getMaterialPre1_13("CARPET", 12, "BROWN_CARPET");
    public final static dMaterial GREEN_CARPET = getMaterialPre1_13("CARPET", 13, "GREEN_CARPET");
    public final static dMaterial RED_CARPET = getMaterialPre1_13("CARPET", 14, "RED_CARPET");
    public final static dMaterial BLACK_CARPET = getMaterialPre1_13("CARPET", 15, "BLACK_CARPET");

    // Colored Clay
    public final static dMaterial WHITE_CLAY = getMaterialPre1_13("STAINED_CLAY", 0, "WHITE_CLAY");
    public final static dMaterial ORANGE_CLAY = getMaterialPre1_13("STAINED_CLAY", 1, "ORANGE_CLAY");
    public final static dMaterial MAGENTA_CLAY = getMaterialPre1_13("STAINED_CLAY", 2, "MAGENTA_CLAY");
    public final static dMaterial LIGHT_BLUE_CLAY = getMaterialPre1_13("STAINED_CLAY", 3, "LIGHT_BLUE_CLAY");
    public final static dMaterial YELLOW_CLAY = getMaterialPre1_13("STAINED_CLAY", 4, "YELLOW_CLAY");
    public final static dMaterial LIME_CLAY = getMaterialPre1_13("STAINED_CLAY", 5, "LIME_CLAY");
    public final static dMaterial PINK_CLAY = getMaterialPre1_13("STAINED_CLAY", 6, "PINK_CLAY");
    public final static dMaterial GRAY_CLAY = getMaterialPre1_13("STAINED_CLAY", 7, "GRAY_CLAY");
    public final static dMaterial LIGHT_GRAY_CLAY = getMaterialPre1_13("STAINED_CLAY", 8, "LIGHT_GRAY_CLAY");
    public final static dMaterial CYAN_CLAY = getMaterialPre1_13("STAINED_CLAY", 9, "CYAN_CLAY");
    public final static dMaterial PURPLE_CLAY = getMaterialPre1_13("STAINED_CLAY", 10, "PURPLE_CLAY");
    public final static dMaterial BLUE_CLAY = getMaterialPre1_13("STAINED_CLAY", 11, "BLUE_CLAY");
    public final static dMaterial BROWN_CLAY = getMaterialPre1_13("STAINED_CLAY", 12, "BROWN_CLAY");
    public final static dMaterial GREEN_CLAY = getMaterialPre1_13("STAINED_CLAY", 13, "GREEN_CLAY");
    public final static dMaterial RED_CLAY = getMaterialPre1_13("STAINED_CLAY", 14, "RED_CLAY");
    public final static dMaterial BLACK_CLAY = getMaterialPre1_13("STAINED_CLAY", 15, "BLACK_CLAY");

    // Stained Glass
    public final static dMaterial WHITE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 0, "WHITE_STAINED_GLASS");
    public final static dMaterial ORANGE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 1, "ORANGE_STAINED_GLASS");
    public final static dMaterial MAGENTA_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 2, "MAGENTA_STAINED_GLASS");
    public final static dMaterial LIGHT_BLUE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 3, "LIGHT_BLUE_STAINED_GLASS");
    public final static dMaterial YELLOW_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 4, "YELLOW_STAINED_GLASS");
    public final static dMaterial LIME_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 5, "LIME_STAINED_GLASS");
    public final static dMaterial PINK_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 6, "PINK_STAINED_GLASS");
    public final static dMaterial GRAY_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 7, "GRAY_STAINED_GLASS");
    public final static dMaterial LIGHT_GRAY_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 8, "LIGHT_GRAY_STAINED_GLASS");
    public final static dMaterial CYAN_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 9, "CYAN_STAINED_GLASS");
    public final static dMaterial PURPLE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 10, "PURPLE_STAINED_GLASS");
    public final static dMaterial BLUE_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 11, "BLUE_STAINED_GLASS");
    public final static dMaterial BROWN_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 12, "BROWN_STAINED_GLASS");
    public final static dMaterial GREEN_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 13, "GREEN_STAINED_GLASS");
    public final static dMaterial RED_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 14, "RED_STAINED_GLASS");
    public final static dMaterial BLACK_STAINED_GLASS = getMaterialPre1_13("STAINED_GLASS", 15, "BLACK_STAINED_GLASS");

    // Stained Glass Panes
    public final static dMaterial WHITE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 0, "WHITE_STAINED_GLASS_PANE");
    public final static dMaterial ORANGE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 1, "ORANGE_STAINED_GLASS_PANE");
    public final static dMaterial MAGENTA_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 2, "MAGENTA_STAINED_GLASS_PANE");
    public final static dMaterial LIGHT_BLUE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 3, "LIGHT_BLUE_STAINED_GLASS_PANE");
    public final static dMaterial YELLOW_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 4, "YELLOW_STAINED_GLASS_PANE");
    public final static dMaterial LIME_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 5, "LIME_STAINED_GLASS_PANE");
    public final static dMaterial PINK_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 6, "PINK_STAINED_GLASS_PANE");
    public final static dMaterial GRAY_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 7, "GRAY_STAINED_GLASS_PANE");
    public final static dMaterial LIGHT_GRAY_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 8, "LIGHT_GRAY_STAINED_GLASS_PANE");
    public final static dMaterial CYAN_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 9, "CYAN_STAINED_GLASS_PANE");
    public final static dMaterial PURPLE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 10, "PURPLE_STAINED_GLASS_PANE");
    public final static dMaterial BLUE_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 11, "BLUE_STAINED_GLASS_PANE");
    public final static dMaterial BROWN_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 12, "BROWN_STAINED_GLASS_PANE");
    public final static dMaterial GREEN_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 13, "GREEN_STAINED_GLASS_PANE");
    public final static dMaterial RED_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 14, "RED_STAINED_GLASS_PANE");
    public final static dMaterial BLACK_STAINED_GLASS_PANE = getMaterialPre1_13("STAINED_GLASS_PANE", 15, "BLACK_STAINED_GLASS_PANE");

    // Planks
    public final static dMaterial OAK_PLANKS = getMaterialPre1_13("WOOD", 0, "OAK_PLANKS");
    public final static dMaterial SPRUCE_PLANKS = getMaterialPre1_13("WOOD", 1, "SPRUCE_PLANKS");
    public final static dMaterial BIRCH_PLANKS = getMaterialPre1_13("WOOD", 2, "BIRCH_PLANKS");
    public final static dMaterial JUNGLE_PLANKS = getMaterialPre1_13("WOOD", 3, "JUNGLE_PLANKS");
    public final static dMaterial ACACIA_PLANKS = getMaterialPre1_13("WOOD", 4, "ACACIA_PLANKS");
    public final static dMaterial DARKOAK_PLANKS = getMaterialPre1_13("WOOD", 5, "DARKOAK_PLANKS");

    // Saplings
    public final static dMaterial OAK_SAPLING = getMaterialPre1_13("SAPLING", 0, "OAK_SAPLING");
    public final static dMaterial SPRUCE_SAPLING = getMaterialPre1_13("SAPLING", 1, "SPRUCE_SAPLING");
    public final static dMaterial BIRCH_SAPLING = getMaterialPre1_13("SAPLING", 2, "BIRCH_SAPLING");
    public final static dMaterial JUNGLE_SAPLING = getMaterialPre1_13("SAPLING", 3, "JUNGLE_SAPLING");
    public final static dMaterial ACACIA_SAPLING = getMaterialPre1_13("SAPLING", 4, "ACACIA_SAPLING");
    public final static dMaterial DARKOAK_SAPLING = getMaterialPre1_13("SAPLING", 5, "DARKOAK_SAPLING");

    // Leaves
    public final static dMaterial OAK_LEAVES = getMaterialPre1_13("LEAVES", 0, "OAK_LEAVES");
    public final static dMaterial SPRUCE_LEAVES = getMaterialPre1_13("LEAVES", 1, "SPRUCE_LEAVES");
    public final static dMaterial BIRCH_LEAVES = getMaterialPre1_13("LEAVES", 2, "BIRCH_LEAVES");
    public final static dMaterial JUNGLE_LEAVES = getMaterialPre1_13("LEAVES", 3, "JUNGLE_LEAVES");
    public final static dMaterial ACACIA_LEAVES = getMaterialPre1_13("LEAVES_2", 0, "ACACIA_LEAVES");
    public final static dMaterial DARKOAK_LEAVES = getMaterialPre1_13("LEAVES_2", 1, "DARKOAK_LEAVES");
    public final static dMaterial PLACED_OAK_LEAVES = getMaterialPre1_13("LEAVES", 4, "PLACED_OAK_LEAVES");
    public final static dMaterial PLACED_SPRUCE_LEAVES = getMaterialPre1_13("LEAVES", 5, "PLACED_SPRUCE_LEAVES");
    public final static dMaterial PLACED_BIRCH_LEAVES = getMaterialPre1_13("LEAVES", 6, "PLACED_BIRCH_LEAVES");
    public final static dMaterial PLACED_JUNGLE_LEAVES = getMaterialPre1_13("LEAVES", 7, "PLACED_JUNGLE_LEAVES");
    public final static dMaterial PLACED_ACACIA_LEAVES = getMaterialPre1_13("LEAVES_2", 4, "PLACED_ACACIA_LEAVES");
    public final static dMaterial PLACED_DARKOAK_LEAVES = getMaterialPre1_13("LEAVES_2", 5, "PLACED_DARKOAK_LEAVES");

    // Logs
    public final static dMaterial OAK_LOG = getMaterialPre1_13("LOG", 0, "OAK_LOG");
    public final static dMaterial SPRUCE_LOG = getMaterialPre1_13("LOG", 1, "SPRUCE_LOG");
    public final static dMaterial BIRCH_LOG = getMaterialPre1_13("LOG", 2, "BIRCH_LOG");
    public final static dMaterial JUNGLE_LOG = getMaterialPre1_13("LOG", 3, "JUNGLE_LOG");
    public final static dMaterial ACACIA_LOG = getMaterialPre1_13("LOG_2", 0, "ACACIA_LOG");
    public final static dMaterial DARKOAK_LOG = getMaterialPre1_13("LOG_2", 1, "DARKOAK_LOG");
    public final static dMaterial OAK_LOG_EAST = getMaterialPre1_13("LOG", 4, "OAK_LOG_EAST");
    public final static dMaterial SPRUCE_LOG_EAST = getMaterialPre1_13("LOG", 5, "SPRUCE_LOG_EAST");
    public final static dMaterial BIRCH_LOG_EAST = getMaterialPre1_13("LOG", 6, "BIRCH_LOG_EAST");
    public final static dMaterial JUNGLE_LOG_EAST = getMaterialPre1_13("LOG", 7, "JUNGLE_LOG_EAST");
    public final static dMaterial ACACIA_LOG_EAST = getMaterialPre1_13("LOG_2", 4, "ACACIA_LOG_EAST");
    public final static dMaterial DARKOAK_LOG_EAST = getMaterialPre1_13("LOG_2", 5, "DARKOAK_LOG_EAST");
    public final static dMaterial OAK_LOG_NORTH = getMaterialPre1_13("LOG", 8, "OAK_LOG_NORTH");
    public final static dMaterial SPRUCE_LOG_NORTH = getMaterialPre1_13("LOG", 9, "SPRUCE_LOG_NORTH");
    public final static dMaterial BIRCH_LOG_NORTH = getMaterialPre1_13("LOG", 10, "BIRCH_LOG_NORTH");
    public final static dMaterial JUNGLE_LOG_NORTH = getMaterialPre1_13("LOG", 11, "JUNGLE_LOG_NORTH");
    public final static dMaterial ACACIA_LOG_NORTH = getMaterialPre1_13("LOG_2", 8, "ACACIA_LOG_NORTH");
    public final static dMaterial DARKOAK_LOG_NORTH = getMaterialPre1_13("LOG_2", 9, "DARKOAK_LOG_NORTH");
    public final static dMaterial OAK_LOG_BALL = getMaterialPre1_13("LOG", 12, "OAK_LOG_BALL");
    public final static dMaterial SPRUCE_LOG_BALL = getMaterialPre1_13("LOG", 13, "SPRUCE_LOG_BALL");
    public final static dMaterial BIRCH_LOG_BALL = getMaterialPre1_13("LOG", 14, "BIRCH_LOG_BALL");
    public final static dMaterial JUNGLE_LOG_BALL = getMaterialPre1_13("LOG", 15, "JUNGLE_LOG_BALL");
    public final static dMaterial ACACIA_LOG_BALL = getMaterialPre1_13("LOG_2", 12, "ACACIA_LOG_BALL");
    public final static dMaterial DARKOAK_LOG_BALL = getMaterialPre1_13("LOG_2", 13, "DARKOAK_LOG_BALL");

    // Sandstone
    public final static dMaterial CHISELED_SANDSTONE = getMaterialPre1_13("SANDSTONE", 1, "CHISELED_SANDSTONE");
    public final static dMaterial SMOOTH_SANDSTONE = getMaterialPre1_13("SANDSTONE", 2, "SMOOTH_SANDSTONE");

    // Stone Bricks
    public final static dMaterial STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 0, "STONE_BRICK");
    public final static dMaterial MOSSY_STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 1, "MOSSY_STONE_BRICK");
    public final static dMaterial CRACKED_STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 2, "CRACKED_STONE_BRICK");
    public final static dMaterial CHISELED_STONE_BRICK = getMaterialPre1_13("SMOOTH_BRICK", 3, "CHISELED_STONE_BRICK");

    // Quartz Block
    public final static dMaterial CHISELED_QUARTZ_BLOCK = getMaterialPre1_13("QUARTZ_BLOCK", 1, "CHISELED_QUARTZ_BLOCK");
    public final static dMaterial PILLAR_QUARTZ_BLOCK = getMaterialPre1_13("QUARTZ_BLOCK", 2, "PILLAR_QUARTZ_BLOCK");
    public final static dMaterial PILLAR_QUARTZ_BLOCK_EAST = getMaterialPre1_13("QUARTZ_BLOCK", 3, "PILLAR_QUARTZ_BLOCK_EAST");
    public final static dMaterial PILLAR_QUARTZ_BLOCK_NORTH = getMaterialPre1_13("QUARTZ_BLOCK", 4, "PILLAR_QUARTZ_BLOCK_NORTH");

    // Colored Ink
    public final static dMaterial INK = getMaterialPre1_13("INK_SACK", 0, "INK");
    public final static dMaterial RED_DYE = getMaterialPre1_13("INK_SACK", 1, "RED_DYE");
    public final static dMaterial GREEN_DYE = getMaterialPre1_13("INK_SACK", 2, "GREEN_DYE");
    public final static dMaterial COCOA_BEANS = getMaterialPre1_13("INK_SACK", 3, "COCOA_BEANS");
    public final static dMaterial LAPIS_LAZULI = getMaterialPre1_13("INK_SACK", 4, "LAPIS_LAZULI");
    public final static dMaterial PURPLE_DYE = getMaterialPre1_13("INK_SACK", 5, "PURPLE_DYE");
    public final static dMaterial CYAN_DYE = getMaterialPre1_13("INK_SACK", 6, "CYAN_DYE");
    public final static dMaterial LIGHT_GRAY_DYE = getMaterialPre1_13("INK_SACK", 7, "LIGHT_GRAY_DYE");
    public final static dMaterial GRAY_DYE = getMaterialPre1_13("INK_SACK", 8, "GRAY_DYE");
    public final static dMaterial PINK_DYE = getMaterialPre1_13("INK_SACK", 9, "PINK_DYE");
    public final static dMaterial LIME_DYE = getMaterialPre1_13("INK_SACK", 10, "LIME_DYE");
    public final static dMaterial YELLOW_DYE = getMaterialPre1_13("INK_SACK", 11, "YELLOW_DYE");
    public final static dMaterial LIGHT_BLUE_DYE = getMaterialPre1_13("INK_SACK", 12, "LIGHT_BLUE_DYE");
    public final static dMaterial MAGENTA_DYE = getMaterialPre1_13("INK_SACK", 13, "MAGENTA_DYE");
    public final static dMaterial ORANGE_DYE = getMaterialPre1_13("INK_SACK", 14, "ORANGE_DYE");
    public final static dMaterial BONE_MEAL = getMaterialPre1_13("INK_SACK", 15, "BONE_MEAL");

    // Tall Grass
    public final static dMaterial SHRUB = getMaterialPre1_13("LONG_GRASS", 0, "SHRUB");
    public final static dMaterial TALL_GRASS = getMaterialPre1_13("LONG_GRASS", 1, "TALL_GRASS");
    public final static dMaterial FERN = getMaterialPre1_13("LONG_GRASS", 2, "FERN");

    // Flowers
    public final static dMaterial POPPY = getMaterialPre1_13("RED_ROSE", 0, "POPPY");
    public final static dMaterial BLUE_ORCHID = getMaterialPre1_13("RED_ROSE", 1, "BLUE_ORCHID");
    public final static dMaterial ALLIUM = getMaterialPre1_13("RED_ROSE", 2, "ALLIUM");
    public final static dMaterial AZURE_BLUET = getMaterialPre1_13("RED_ROSE", 3, "AZURE_BLUET");
    public final static dMaterial RED_TULIP = getMaterialPre1_13("RED_ROSE", 4, "RED_TULIP");
    public final static dMaterial ORANGE_TULIP = getMaterialPre1_13("RED_ROSE", 5, "ORANGE_TULIP");
    public final static dMaterial WHITE_TULIP = getMaterialPre1_13("RED_ROSE", 6, "WHITE_TULIP");
    public final static dMaterial PINK_TULIP = getMaterialPre1_13("RED_ROSE", 7, "PINK_TULIP");
    public final static dMaterial OXEYE_DAISY = getMaterialPre1_13("RED_ROSE", 8, "OXEYE_DAISY");

    // Double-tall Plants
    public final static dMaterial SUNFLOWER = getMaterialPre1_13("DOUBLE_PLANT", 0, "SUNFLOWER");
    public final static dMaterial LILAC = getMaterialPre1_13("DOUBLE_PLANT", 1, "LILAC");
    public final static dMaterial DOUBLE_TALLGRASS = getMaterialPre1_13("DOUBLE_PLANT", 2, "DOUBLE_TALLGRASS");
    public final static dMaterial LARGE_FERN = getMaterialPre1_13("DOUBLE_PLANT", 3, "LARGE_FERN");
    public final static dMaterial ROSE_BUSH = getMaterialPre1_13("DOUBLE_PLANT", 4, "ROSE_BUSH");
    public final static dMaterial PEONY = getMaterialPre1_13("DOUBLE_PLANT", 5, "PEONY");
    public final static dMaterial DOUBLEPLANT_TOP = getMaterialPre1_13("DOUBLE_PLANT", 8, "DOUBLEPLANT_TOP");

    // Potted Plants
    public final static dMaterial EMPTY_POT = getMaterialPre1_13("FLOWER_POT", 0, "EMPTY_POT");
    public final static dMaterial POTTED_POPPY = getMaterialPre1_13("FLOWER_POT", 1, "POTTED_POPPY");
    public final static dMaterial POTTED_DAISY = getMaterialPre1_13("FLOWER_POT", 2, "POTTED_DAISY");
    public final static dMaterial POTTED_OAK_SAPLING = getMaterialPre1_13("FLOWER_POT", 3, "POTTED_OAK_SAPLING");
    public final static dMaterial POTTED_SPRUCE_SAPLING = getMaterialPre1_13("FLOWER_POT", 4, "POTTED_SPRUCE_SAPLING");
    public final static dMaterial POTTED_BIRCH_SAPLING = getMaterialPre1_13("FLOWER_POT", 5, "POTTED_BIRCH_SAPLING");
    public final static dMaterial POTTED_JUNGLE_SAPLING = getMaterialPre1_13("FLOWER_POT", 6, "POTTED_JUNGLE_SAPLING");
    public final static dMaterial POTTED_RED_MUSHROOM = getMaterialPre1_13("FLOWER_POT", 7, "POTTED_RED_MUSHROOM");
    public final static dMaterial POTTED_BROWN_MUSHROOM = getMaterialPre1_13("FLOWER_POT", 8, "POTTED_BROWN_MUSHROOM");
    public final static dMaterial POTTED_CACTUS = getMaterialPre1_13("FLOWER_POT", 9, "POTTED_CACTUS");
    public final static dMaterial POTTED_SHRUB = getMaterialPre1_13("FLOWER_POT", 10, "POTTED_SHRUB");
    public final static dMaterial POTTED_FERN = getMaterialPre1_13("FLOWER_POT", 11, "POTTED_FERN");
    public final static dMaterial POTTED_ACACIA_SAPLING = getMaterialPre1_13("FLOWER_POT", 12, "POTTED_ACACIA_SAPLING");
    public final static dMaterial POTTED_DARKOAK_SAPLING = getMaterialPre1_13("FLOWER_POT", 13, "POTTED_DARKOAK_SAPLING");

    // Steps
    public final static dMaterial STONE_SLAB = getMaterialPre1_13("STEP", 0, "STONE_SLAB");
    public final static dMaterial SANDSTONE_SLAB = getMaterialPre1_13("STEP", 1, "SANDSTONE_SLAB");
    public final static dMaterial WOODEN_SLAB = getMaterialPre1_13("STEP", 2, "WOODEN_SLAB");
    public final static dMaterial COBBLESTONE_SLAB = getMaterialPre1_13("STEP", 3, "COBBLESTONE_SLAB");
    public final static dMaterial BRICKS_SLAB = getMaterialPre1_13("STEP", 4, "BRICKS_SLAB");
    public final static dMaterial STONEBRICKS_SLAB = getMaterialPre1_13("STEP", 5, "STONEBRICKS_SLAB");
    public final static dMaterial NETHERBRICK_SLAB = getMaterialPre1_13("STEP", 6, "NETHERBRICK_SLAB");
    public final static dMaterial QUARTZ_SLAB = getMaterialPre1_13("STEP", 7, "QUARTZ_SLAB");
    public final static dMaterial STONE_SLAB_UP = getMaterialPre1_13("STEP", 8, "STONE_SLAB_UP");
    public final static dMaterial SANDSTONE_SLAB_UP = getMaterialPre1_13("STEP", 9, "SANDSTONE_SLAB_UP");
    public final static dMaterial WOODEN_SLAB_UP = getMaterialPre1_13("STEP", 10, "WOODEN_SLAB_UP");
    public final static dMaterial COBBLESTONE_SLAB_UP = getMaterialPre1_13("STEP", 11, "COBBLESTONE_SLAB_UP");
    public final static dMaterial BRICKS_SLAB_UP = getMaterialPre1_13("STEP", 12, "BRICKS_SLAB_UP");
    public final static dMaterial STONEBRICKS_SLAB_UP = getMaterialPre1_13("STEP", 13, "STONEBRICKS_SLAB_UP");
    public final static dMaterial NETHERBRICK_SLAB_UP = getMaterialPre1_13("STEP", 14, "NETHERBRICK_SLAB_UP");
    public final static dMaterial QUARTZ_SLAB_UP = getMaterialPre1_13("STEP", 15, "QUARTZ_SLAB_UP");

    // Wood Steps
    public final static dMaterial OAK_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 0, "OAK_WOOD_SLAB");
    public final static dMaterial SPRUCE_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 1, "SPRUCE_WOOD_SLAB");
    public final static dMaterial BIRCH_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 2, "BIRCH_WOOD_SLAB");
    public final static dMaterial JUNGLE_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 3, "JUNGLE_WOOD_SLAB");
    public final static dMaterial ACACIA_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 4, "ACACIA_WOOD_SLAB");
    public final static dMaterial DARKOAK_WOOD_SLAB = getMaterialPre1_13("WOOD_STEP", 5, "DARKOAK_WOOD_SLAB");
    public final static dMaterial OAK_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 8, "OAK_WOOD_SLAB_UP");
    public final static dMaterial SPRUCE_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 9, "SPRUCE_WOOD_SLAB_UP");
    public final static dMaterial BIRCH_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 10, "BIRCH_WOOD_SLAB_UP");
    public final static dMaterial JUNGLE_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 11, "JUNGLE_WOOD_SLAB_UP");
    public final static dMaterial ACACIA_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 12, "ACACIA_WOOD_SLAB_UP");
    public final static dMaterial DARKOAK_WOOD_SLAB_UP = getMaterialPre1_13("WOOD_STEP", 13, "DARKOAK_WOOD_SLAB_UP");

    // Double Steps
    public final static dMaterial STONE_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 0, "STONE_DOUBLESLAB");
    public final static dMaterial SANDSTONE_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 1, "SANDSTONE_DOUBLESLAB");
    public final static dMaterial WOODEN_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 2, "WOODEN_DOUBLESLAB");
    public final static dMaterial COBBLESTONE_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 3, "COBBLESTONE_DOUBLESLAB");
    public final static dMaterial BRICKS_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 4, "BRICKS_DOUBLESLAB");
    public final static dMaterial STONEBRICKS_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 5, "STONEBRICKS_DOUBLESLAB");
    public final static dMaterial NETHERBRICK_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 6, "NETHERBRICK_DOUBLESLAB");
    public final static dMaterial QUARTZ_DOUBLESLAB = getMaterialPre1_13("DOUBLE_STEP", 7, "QUARTZ_DOUBLESLAB");
    public final static dMaterial STONE_SLABBALL = getMaterialPre1_13("DOUBLE_STEP", 8, "STONE_SLABBALL");
    public final static dMaterial SANDSTONE_SLABBALL = getMaterialPre1_13("DOUBLE_STEP", 9, "SANDSTONE_SLABBALL");

    // Wood Double Steps
    public final static dMaterial OAK_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 0, "OAK_WOOD_DOUBLESLAB");
    public final static dMaterial SPRUCE_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 1, "SPRUCE_WOOD_DOUBLESLAB");
    public final static dMaterial BIRCH_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 2, "BIRCH_WOOD_DOUBLESLAB");
    public final static dMaterial JUNGLE_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 3, "JUNGLE_WOOD_DOUBLESLAB");
    public final static dMaterial ACACIA_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 4, "ACACIA_WOOD_DOUBLESLAB");
    public final static dMaterial DARKOAK_WOOD_DOUBLESLAB = getMaterialPre1_13("WOOD_DOUBLE_STEP", 5, "DARKOAK_WOOD_DOUBLESLAB");

    // Skull Items
    public final static dMaterial SKELETON_SKULL = getMaterialPre1_13("SKULL_ITEM", 0, "SKELETON_SKULL");
    public final static dMaterial WITHERSKELETON_SKULL = getMaterialPre1_13("SKULL_ITEM", 1, "WITHERSKELETON_SKULL");
    public final static dMaterial ZOMBIE_SKULL = getMaterialPre1_13("SKULL_ITEM", 2, "ZOMBIE_SKULL");
    public final static dMaterial HUMAN_SKULL = getMaterialPre1_13("SKULL_ITEM", 3, "HUMAN_SKULL");
    public final static dMaterial CREEPER_SKULL = getMaterialPre1_13("SKULL_ITEM", 4, "CREEPER_SKULL");

    // Monster Eggs
    // TODO: I will add rest of eggs (I needed this one quick for a project)
    public final static dMaterial SKELETON_EGG = getMaterialPre1_13("MONSTER_EGG", 51, "SKELETON_EGG");

    // Fish
    public final static dMaterial COOKED_FISH = getMaterialPre1_13("COOKED_FISH", 0, "COOKED_FISH");
    public final static dMaterial COOKED_SALMON = getMaterialPre1_13("COOKED_FISH", 1, "COOKED_SALMON");
    public final static dMaterial COOKED_CLOWNFISH = getMaterialPre1_13("COOKED_FISH", 2, "COOKED_CLOWNFISH");
    public final static dMaterial COOKED_PUFFERFISH = getMaterialPre1_13("COOKED_FISH", 3, "COOKED_PUFFERFISH");
    public final static dMaterial RAW_FISH = getMaterialPre1_13("RAW_FISH", 0, "RAW_FISH");
    public final static dMaterial RAW_SALMON = getMaterialPre1_13("RAW_FISH", 1, "RAW_SALMON");
    public final static dMaterial RAW_CLOWNFISH = getMaterialPre1_13("RAW_FISH", 2, "RAW_CLOWNFISH");
    public final static dMaterial RAW_PUFFERFISH = getMaterialPre1_13("RAW_FISH", 3, "RAW_PUFFERFISH");

    // One-Offs (Don't have their own group)
    public final static dMaterial CHARCOAL = getMaterialPre1_13("COAL", 1, "CHARCOAL");
    public final static dMaterial RED_SAND = getMaterialPre1_13("SAND", 1, "RED_SAND");
    public final static dMaterial COARSE_DIRT = getMaterialPre1_13("DIRT", 1, "COARSE_DIRT");
    public final static dMaterial PODZOL = getMaterialPre1_13("DIRT", 2, "PODZOL");
    public final static dMaterial MOSSY_COBBLE_WALL = getMaterialPre1_13("COBBLE_WALL", 1, "MOSSY_COBBLE_WALL");

    // Stone
    // GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ADESITE
    public final static dMaterial GRANITE = getMaterialPre1_13("STONE", 1, "GRANITE");
    public final static dMaterial POLISHED_GRANITE = getMaterialPre1_13("STONE", 2, "POLISHED_GRANITE");
    public final static dMaterial DIORITE = getMaterialPre1_13("STONE", 3, "DIORITE");
    public final static dMaterial POLISHED_DIORITE = getMaterialPre1_13("STONE", 4, "POLISHED_DIORITE");
    public final static dMaterial ANDESITE = getMaterialPre1_13("STONE", 5, "ANDESITE");
    public final static dMaterial POLISHED_ANDESITE = getMaterialPre1_13("STONE", 6, "POLISHED_ANDESITE");

    // Bed
    public final static dMaterial WHITE_BED = getMaterialPre1_13("BED", 0, "WHITE_BED");
    public final static dMaterial ORANGE_BED = getMaterialPre1_13("BED", 1, "ORANGE_BED");
    public final static dMaterial MAGENTA_BED = getMaterialPre1_13("BED", 2, "MAGENTA_BED");
    public final static dMaterial LIGHT_BLUE_BED = getMaterialPre1_13("BED", 3, "LIGHT_BLUE_BED");
    public final static dMaterial YELLOW_BED = getMaterialPre1_13("BED", 4, "YELLOW_BED");
    public final static dMaterial LIME_BED = getMaterialPre1_13("BED", 5, "LIME_BED");
    public final static dMaterial PINK_BED = getMaterialPre1_13("BED", 6, "PINK_BED");
    public final static dMaterial GRAY_BED = getMaterialPre1_13("BED", 7, "GRAY_BED");
    public final static dMaterial LIGHT_GRAY_BED = getMaterialPre1_13("BED", 8, "LIGHT_GRAY_BED");
    public final static dMaterial CYAN_BED = getMaterialPre1_13("BED", 9, "CYAN_BED");
    public final static dMaterial PURPLE_BED = getMaterialPre1_13("BED", 10, "PURPLE_BED");
    public final static dMaterial BLUE_BED = getMaterialPre1_13("BED", 11, "BLUE_BED");
    public final static dMaterial BROWN_BED = getMaterialPre1_13("BED", 12, "BROWN_BED");
    public final static dMaterial GREEN_BED = getMaterialPre1_13("BED", 13, "GREEN_BED");
    public final static dMaterial RED_BED = getMaterialPre1_13("BED", 14, "RED_BED");
    public final static dMaterial BLACK_BED = getMaterialPre1_13("BED", 15, "BLACK_BED");

    // Version checks for version-specific materials
    public static dMaterial getMaterial1_12(String material, int data, String name) {
        if (NMSHandler.getVersion() == NMSVersion.v1_12_R1) {
            return new dMaterial(Material.valueOf(material), data).forceIdentifyAs(name);
        }
        return null;
    }

    // Concrete
    public final static dMaterial WHITE_CONCRETE = getMaterial1_12("CONCRETE", 0, "WHITE_CONCRETE");
    public final static dMaterial ORANGE_CONCRETE = getMaterial1_12("CONCRETE", 1, "ORANGE_CONCRETE");
    public final static dMaterial MAGENTA_CONCRETE = getMaterial1_12("CONCRETE", 2, "MAGENTA_CONCRETE");
    public final static dMaterial LIGHT_BLUE_CONCRETE = getMaterial1_12("CONCRETE", 3, "LIGHT_BLUE_CONCRETE");
    public final static dMaterial YELLOW_CONCRETE = getMaterial1_12("CONCRETE", 4, "YELLOW_CONCRETE");
    public final static dMaterial LIME_CONCRETE = getMaterial1_12("CONCRETE", 5, "LIME_CONCRETE");
    public final static dMaterial PINK_CONCRETE = getMaterial1_12("CONCRETE", 6, "PINK_CONCRETE");
    public final static dMaterial GRAY_CONCRETE = getMaterial1_12("CONCRETE", 7, "GRAY_CONCRETE");
    public final static dMaterial LIGHT_GRAY_CONCRETE = getMaterial1_12("CONCRETE", 8, "LIGHT_GRAY_CONCRETE");
    public final static dMaterial CYAN_CONCRETE = getMaterial1_12("CONCRETE", 9, "CYAN_CONCRETE");
    public final static dMaterial PURPLE_CONCRETE = getMaterial1_12("CONCRETE", 10, "PURPLE_CONCRETE");
    public final static dMaterial BLUE_CONCRETE = getMaterial1_12("CONCRETE", 11, "BLUE_CONCRETE");
    public final static dMaterial BROWN_CONCRETE = getMaterial1_12("CONCRETE", 12, "BROWN_CONCRETE");
    public final static dMaterial GREEN_CONCRETE = getMaterial1_12("CONCRETE", 13, "GREEN_CONCRETE");
    public final static dMaterial RED_CONCRETE = getMaterial1_12("CONCRETE", 14, "RED_CONCRETE");
    public final static dMaterial BLACK_CONCRETE = getMaterial1_12("CONCRETE", 15, "BLACK_CONCRETE");

    // Concrete Powder
    public final static dMaterial WHITE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 0, "WHITE_CONCRETE_POWDER");
    public final static dMaterial ORANGE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 1, "ORANGE_CONCRETE_POWDER");
    public final static dMaterial MAGENTA_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 2, "MAGENTA_CONCRETE_POWDER");
    public final static dMaterial LIGHT_BLUE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 3, "LIGHT_BLUE_CONCRETE_POWDER");
    public final static dMaterial YELLOW_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 4, "YELLOW_CONCRETE_POWDER");
    public final static dMaterial LIME_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 5, "LIME_CONCRETE_POWDER");
    public final static dMaterial PINK_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 6, "PINK_CONCRETE_POWDER");
    public final static dMaterial GRAY_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 7, "GRAY_CONCRETE_POWDER");
    public final static dMaterial LIGHT_GRAY_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 8, "LIGHT_GRAY_CONCRETE_POWDER");
    public final static dMaterial CYAN_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 9, "CYAN_CONCRETE_POWDER");
    public final static dMaterial PURPLE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 10, "PURPLE_CONCRETE_POWDER");
    public final static dMaterial BLUE_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 11, "BLUE_CONCRETE_POWDER");
    public final static dMaterial BROWN_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 12, "BROWN_CONCRETE_POWDER");
    public final static dMaterial GREEN_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 13, "GREEN_CONCRETE_POWDER");
    public final static dMaterial RED_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 14, "RED_CONCRETE_POWDER");
    public final static dMaterial BLACK_CONCRETE_POWDER = getMaterial1_12("CONCRETE_POWDER", 15, "BLACK_CONCRETE_POWDER");

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
                if (mat != null) {
                    registerVariety(mat);
                }
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
        if (material.isBlock()) {
            material = NMSHandler.getInstance().getBlockHelper().getBlockData(material, (byte) data).getMaterial();
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

        string = string.toUpperCase();
        if (string.startsWith("M@")) {
            string = string.substring("M@".length());
        }
        if (string.equals("RANDOM")) {
            return getMaterialFrom(Material.values()[CoreUtilities.getRandom().nextInt(Material.values().length)]);
        }
        int index = string.indexOf(',');
        if (index < 0) {
            index = string.indexOf(':');
        }
        int data = 0;
        if (index >= 0) {
            data = aH.getIntegerFrom(string.substring(index + 1));
            string = string.substring(0, index);
        }
        Material m = Material.getMaterial(string);
        if (m != null) {
            if ((context == null || context.debug) && index >= 0) {
                dB.log("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use relevant properties instead.");
            }
            return getMaterialFrom(m, data);
        }
        dMaterial mat = all_dMaterials.get(string);
        if (mat != null) {
            if ((context == null || context.debug) && index >= 0) {
                dB.log("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use relevant properties instead.");
            }
            if (data == 0) {
                return mat;
            }
            return getMaterialFrom(mat.material, data);
        }
        int matid = aH.getIntegerFrom(string);
        if (matid != 0) {
            // It's always an error (except in the 'matches' call) to use a material ID number instead of a name.
            if (context != noDebugContext) {
                dB.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use material names instead.");
            }
            m = getLegacyMaterial(matid);
            if (m != null) {
                return getMaterialFrom(m, data);
            }
        }
        return null;
    }

    public static dMaterial quickOfNamed(String string) {
        string = string.toUpperCase();
        int index = string.indexOf(',');
        if (index < 0) {
            index = string.indexOf(':');
        }
        int data = 0;
        if (index >= 0) {
            data = aH.getIntegerFrom(string.substring(index + 1));
            string = string.substring(0, index);
        }
        Material m = Material.getMaterial(string);
        if (m != null) {
            return getMaterialFrom(m, data);
        }
        dMaterial mat = all_dMaterials.get(string);
        if (mat != null) {
            if (data == 0) {
                return mat;
            }
            return getMaterialFrom(mat.material, data);
        }
        return null;
    }

    public static TagContext noDebugContext = new BukkitTagContext(null, null, false, null, false, null);

    /**
     * Determine whether a string is a valid material.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {
        arg = arg.toUpperCase();
        if (arg.startsWith("m@")) {
            return true;
        }
        if (valueOf(arg, noDebugContext) != null) {
            return true;
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
            return getMaterial() == ((dMaterial) object).getMaterial()
                    && getData((byte) 0) == ((dMaterial) object).getData((byte) 0);
        }
        else {
            dMaterial parsed = valueOf(object.toString());
            return equals(parsed);
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


    public byte getData(byte fallback) {
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
            // TODO: 1.13 - ??? are these meant to be legacy fallbacks or what
            String name = material.name();
            return name.equals("SAPLING") || name.equals("HUGE_MUSHROOM_1") || name.equals("HUGE_MUSHROOM_2");
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
            return "m@" + CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return "m@" + CoreUtilities.toLowerCase(material.name());
    }

    public String identifyFull() {
        if (forcedIdentity != null) {
            return "m@" + forcedIdentityLow + (getData() != null ? "," + getData() : "");
        }
        if (getData() != null && getData() > 0) {
            return "m@" + CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return "m@" + CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public String identifySimple() {
        if (forcedIdentity != null) {
            return "m@" + forcedIdentityLow;
        }
        return "m@" + CoreUtilities.toLowerCase(material.name());
    }

    public String identifyNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        if (getData() != null && getData() > 0) {
            return CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifySimpleNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifyFullNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow + (getData() != null ? "," + getData() : "");
        }
        if (getData() != null && getData() > 0) {
            return CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public String toString() {
        return identify();
    }

    public String realName() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dB.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use material names instead.");
                return new Element(((dMaterial) object).material.getId())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        registerTag("data", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (attribute.context == null || attribute.context.debug) {
                    dB.log("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use relevant properties instead.");
                }
                return new Element(((dMaterial) object).getData())
                        .getAttribute(attribute.fulfill(1));
            }
        });

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
                return new Element(((dMaterial) object).material.isSolid())
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
        // @attribute <m@material.block_resistance>
        // @returns Element(Decimal)
        // @mechanism dMaterial.block_resistance
        // @description
        // Returns the explosion resistance for all blocks of this material type.
        // -->
        registerTag("block_resistance", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dMaterial material = (dMaterial) object;
                if (!NMSHandler.getInstance().getBlockHelper().hasBlock(material.getMaterial())) {
                    dB.echoError("Provided material does not have a placeable block.");
                    return null;
                }
                return new Element(NMSHandler.getInstance().getBlockHelper().getBlockResistance(material.getMaterial()))
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
                        CoreUtilities.toLowerCase(((dMaterial) object).material.name()))
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

    @Override
    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to a material!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dMaterial
        // @name block_resistance
        // @input Element(Decimal)
        // @description
        // Sets the explosion resistance for all blocks of this material type.
        // @tags
        // <m@material.block_resistance>
        // -->
        if (mechanism.matches("block_resistance") && mechanism.requireFloat()) {
            if (!NMSHandler.getInstance().getBlockHelper().setBlockResistance(material, mechanism.getValue().asFloat())) {
                dB.echoError("Provided material does not have a placeable block.");
            }
        }

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled()) {
                break;
            }
        }

        if (!mechanism.fulfilled()) {
            mechanism.reportInvalid();
        }
    }
}
