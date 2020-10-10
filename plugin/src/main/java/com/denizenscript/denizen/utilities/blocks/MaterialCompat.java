package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import org.bukkit.Material;

public class MaterialCompat {

    public static Material SIGN; // in 1.14, set to OAK_SIGN
    public static Material WALL_SIGN; // in 1.14, set to OAK_WALL_SIGN

    static {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {
            SIGN = Material.OAK_SIGN;
            WALL_SIGN = Material.OAK_WALL_SIGN;
        }
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_13)) {
            // split in 1.14
            SIGN = Material.valueOf("SIGN");
            WALL_SIGN = Material.valueOf("WALL_SIGN");
        }
    }

    public static boolean isStandingSign(Material material) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            switch (material) {
                case CRIMSON_SIGN:
                case WARPED_SIGN:
                    return true;
            }
        }
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            switch (material) {
                case CRIMSON_WALL_SIGN:
                case WARPED_WALL_SIGN:
                    return true;
            }
        }
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

    public static boolean isBannerOrShield(Material material) {
        return material == Material.SHIELD || material.name().endsWith("_BANNER");
    }
}
