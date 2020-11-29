package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import org.bukkit.Material;

public class MaterialCompat {

    public static boolean isStandingSign(Material material) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            switch (material) {
                case CRIMSON_SIGN:
                case WARPED_SIGN:
                    return true;
            }
        }
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

    public static boolean isWallSign(Material material) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            switch (material) {
                case CRIMSON_WALL_SIGN:
                case WARPED_WALL_SIGN:
                    return true;
            }
        }
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

    public static boolean isAnySign(Material material) {
        return isStandingSign(material) || isWallSign(material);
    }

    public static boolean isBannerOrShield(Material material) {
        return material == Material.SHIELD || material.name().endsWith("_BANNER");
    }
}
