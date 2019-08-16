package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;

public class CatHelper {

    public static String getColor(EntityTag catTag) {
        Cat cat = (Cat) catTag.getBukkitEntity();
        return cat.getCatType().name() + "|" + cat.getCollarColor().name();
    }

    public static void setColor(EntityTag catTag, String color) {
        Cat cat = (Cat) catTag.getBukkitEntity();
        ListTag list = ListTag.valueOf(color);
        cat.setCatType(Cat.Type.valueOf(list.get(0).toUpperCase()));
        if (list.size() > 1) {
            cat.setCollarColor(DyeColor.valueOf(list.get(1).toUpperCase()));
        }
    }
}
