package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;

import java.util.Arrays;

public class TropicalFishHelper {

    public static String getColor(EntityTag fishTag) {
        TropicalFish fish = ((TropicalFish) fishTag.getBukkitEntity());
        return new ListTag(Arrays.asList(fish.getPattern().name(), fish.getBodyColor().name(), fish.getPattern().name())).identify();
    }

    public static void setColor(EntityTag fishTag, String color) {
        ListTag list = ListTag.valueOf(color);
        TropicalFish fish = ((TropicalFish) fishTag.getBukkitEntity());
        fish.setPattern(TropicalFish.Pattern.valueOf(list.get(0).toUpperCase()));
        if (list.size() > 1) {
            fish.setBodyColor(DyeColor.valueOf(list.get(1).toUpperCase()));
        }
        if (list.size() > 2) {
            fish.setPatternColor(DyeColor.valueOf(list.get(2).toUpperCase()));
        }
    }
}
