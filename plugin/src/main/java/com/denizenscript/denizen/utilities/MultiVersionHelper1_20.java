package com.denizenscript.denizen.utilities;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.EnumHelper;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.block.sign.Side;

public class MultiVersionHelper1_20 {
    public static boolean isSide(String side) {
        return side != null && EnumHelper.get(Side.class).valuesMapLower.containsKey(EnumHelper.cleanKey(side));
    }
    public static ElementTag sideString(String side) {
        return new ElementTag(Side.valueOf(side));
    }

    public static boolean isAnyHangingSign(Material material) {
        switch (material) {
            case CRIMSON_HANGING_SIGN:
            case WARPED_HANGING_SIGN:
            case ACACIA_HANGING_SIGN:
            case BIRCH_HANGING_SIGN:
            case DARK_OAK_HANGING_SIGN:
            case JUNGLE_HANGING_SIGN:
            case OAK_HANGING_SIGN:
            case SPRUCE_HANGING_SIGN:
            case MANGROVE_HANGING_SIGN:
            case CHERRY_HANGING_SIGN:
            case BAMBOO_HANGING_SIGN:
                return true;
            default:
                return false;
        }
    }
    public static boolean isAnySign(Material material) {
        return material == Material.CHERRY_SIGN || material == Material.CHERRY_WALL_SIGN
            || material == Material.BAMBOO_SIGN || material == Material.BAMBOO_WALL_SIGN
            || isAnyHangingSign(material);
    }
}
