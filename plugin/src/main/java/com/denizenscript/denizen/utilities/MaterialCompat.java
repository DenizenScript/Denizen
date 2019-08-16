package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.utilities.blocks.OldMaterialsHelper;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class MaterialCompat {

    // Materials changed in 1.13
    public static Material COMMAND_BLOCK;
    public static Material END_PORTAL;
    public static Material FIREWORK_ROCKET;
    public static Material IRON_DOOR;
    public static Material NETHER_FENCE;
    public static Material NETHER_PORTAL;
    public static Material OAK_DOOR;
    public static Material OAK_FENCE;
    public static Material OAK_TRAPDOOR;
    public static Material SIGN; // in 1.14, set to OAK_SIGN
    public static Material WRITABLE_BOOK;

    // Combined materials in 1.13 - generally, methods should be used over these
    private static Material COMPARATOR;
    private static Material REPEATER;

    // Pre-combination materials
    private static Material BURNING_FURNACE;
    private static Material DIODE_BLOCK_OFF;
    private static Material DIODE_BLOCK_ON;
    private static Material REDSTONE_COMPARATOR_OFF;
    private static Material REDSTONE_COMPARATOR_ON;

    // Materials split in 1.13 - generally, methods should be used over these
    private static Material BANNER;
    private static Material SKULL_ITEM;
    private static Material STAINED_GLASS_PANE;
    private static Material STANDING_BANNER;
    private static Material WALL_BANNER;

    // Materials split in 1.14
    public static Material WALL_SIGN; // in 1.14, set to OAK_WALL_SIGN

    static {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {
            SIGN = Material.OAK_SIGN;
            WALL_SIGN = Material.OAK_WALL_SIGN;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            COMMAND_BLOCK = Material.COMMAND_BLOCK;
            COMPARATOR = Material.COMPARATOR;
            END_PORTAL = Material.END_PORTAL;
            FIREWORK_ROCKET = Material.FIREWORK_ROCKET;
            IRON_DOOR = Material.IRON_DOOR;
            NETHER_FENCE = Material.NETHER_BRICK_FENCE;
            NETHER_PORTAL = Material.NETHER_PORTAL;
            OAK_DOOR = Material.OAK_DOOR;
            OAK_FENCE = Material.OAK_FENCE;
            OAK_TRAPDOOR = Material.OAK_TRAPDOOR;
            REPEATER = Material.REPEATER;
            if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_13)) {
                // split in 1.14
                SIGN = Material.valueOf("SIGN");
                WALL_SIGN = Material.valueOf("WALL_SIGN");
            }
            WRITABLE_BOOK = Material.WRITABLE_BOOK;
        }
        else {
            BANNER = Material.valueOf("BANNER");
            BURNING_FURNACE = Material.valueOf("BURNING_FURNACE");
            COMMAND_BLOCK = Material.valueOf("COMMAND");
            DIODE_BLOCK_OFF = Material.valueOf("DIODE_BLOCK_OFF");
            DIODE_BLOCK_ON = Material.valueOf("DIODE_BLOCK_ON");
            END_PORTAL = Material.valueOf("ENDER_PORTAL");
            FIREWORK_ROCKET = Material.valueOf("FIREWORK");
            IRON_DOOR = Material.valueOf("IRON_DOOR_BLOCK");
            NETHER_PORTAL = Material.valueOf("PORTAL");
            OAK_DOOR = Material.valueOf("WOODEN_DOOR");
            OAK_FENCE = Material.valueOf("FENCE");
            OAK_TRAPDOOR = Material.valueOf("TRAP_DOOR");
            REDSTONE_COMPARATOR_OFF = Material.valueOf("REDSTONE_COMPARATOR_OFF");
            REDSTONE_COMPARATOR_ON = Material.valueOf("REDSTONE_COMPARATOR_ON");
            SIGN = Material.valueOf("SIGN_POST");
            SKULL_ITEM = Material.valueOf("SKULL_ITEM");
            STAINED_GLASS_PANE = Material.valueOf("STAINED_GLASS_PANE");
            STANDING_BANNER = Material.valueOf("STANDING_BANNER");
            WALL_BANNER = Material.valueOf("WALL_BANNER");
            WRITABLE_BOOK = Material.valueOf("BOOK_AND_QUILL");
        }
    }

    public static boolean isComparator(Material material) {
        return material == COMPARATOR || material == REDSTONE_COMPARATOR_OFF || material == REDSTONE_COMPARATOR_ON;
    }

    public static boolean isFurnace(Material material) {
        return material == Material.FURNACE || material == BURNING_FURNACE;
    }

    public static boolean isRepeater(Material material) {
        return material == REPEATER || material == DIODE_BLOCK_OFF || material == DIODE_BLOCK_ON;
    }

    public static boolean isStandingSign(Material material) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {
            switch (material) {
                case ACACIA_SIGN:
                case BIRCH_SIGN:
                case DARK_OAK_SIGN:
                case JUNGLE_SIGN:
                case OAK_SIGN:
                case SPRUCE_SIGN:
                    return true;
                default:
                    return false;
            }
        }
        return material == SIGN;
    }

    public static boolean isWallSign(Material material) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {
            switch (material) {
                case ACACIA_WALL_SIGN:
                case BIRCH_WALL_SIGN:
                case DARK_OAK_WALL_SIGN:
                case JUNGLE_WALL_SIGN:
                case OAK_WALL_SIGN:
                case SPRUCE_WALL_SIGN:
                    return true;
                default:
                    return false;
            }
        }
        return material == WALL_SIGN;
    }

    public static boolean isAnySign(Material material) {
        return isStandingSign(material) || isWallSign(material);
    }

    public static ItemStack createGrayPane() {
        if (STAINED_GLASS_PANE == null) {
            return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        else {
            return new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, DyeColor.GRAY.getDyeData());
        }
    }

    public static ItemStack createPlayerHead() {
        if (SKULL_ITEM == null) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
        else {
            return new ItemStack(SKULL_ITEM, 1, (byte) 3);
        }
    }

    public static ItemStack updateItem(int oldMat) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            Material mat = Bukkit.getUnsafe().fromLegacy(OldMaterialsHelper.getLegacyMaterial(oldMat));
            return new ItemStack(mat);
        }
        return new ItemStack(OldMaterialsHelper.getLegacyMaterial(oldMat));
    }

    public static ItemStack updateItem(int oldMat, byte bit) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            BlockData blockDat = Bukkit.getUnsafe().fromLegacy(OldMaterialsHelper.getLegacyMaterial(oldMat), bit);
            Material mat = blockDat.getMaterial();
            return new ItemStack(mat);
        }
        return new ItemStack(OldMaterialsHelper.getLegacyMaterial(oldMat), bit);
    }

    public static boolean isBannerOrShield(Material material) {
        if (material == Material.SHIELD) {
            return true;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            switch (material) {
                case BLACK_BANNER:
                case BLUE_BANNER:
                case BROWN_BANNER:
                case CYAN_BANNER:
                case GRAY_BANNER:
                case GREEN_BANNER:
                case LIME_BANNER:
                case LIGHT_BLUE_BANNER:
                case LIGHT_GRAY_BANNER:
                case MAGENTA_BANNER:
                case ORANGE_BANNER:
                case PINK_BANNER:
                case PURPLE_BANNER:
                case RED_BANNER:
                case WHITE_BANNER:
                case YELLOW_BANNER:
                case BLACK_WALL_BANNER:
                case BLUE_WALL_BANNER:
                case BROWN_WALL_BANNER:
                case CYAN_WALL_BANNER:
                case GRAY_WALL_BANNER:
                case GREEN_WALL_BANNER:
                case LIME_WALL_BANNER:
                case LIGHT_BLUE_WALL_BANNER:
                case LIGHT_GRAY_WALL_BANNER:
                case MAGENTA_WALL_BANNER:
                case ORANGE_WALL_BANNER:
                case PINK_WALL_BANNER:
                case PURPLE_WALL_BANNER:
                case RED_WALL_BANNER:
                case WHITE_WALL_BANNER:
                case YELLOW_WALL_BANNER:
                    return true;
                default:
                    return false;
            }
        }
        else {
            return material == BANNER || material == STANDING_BANNER || material == WALL_BANNER;
        }
    }
}
