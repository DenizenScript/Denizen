package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import org.bukkit.entity.Fox;

public class FoxHelper {

    public static String getColor(EntityTag foxTag) {
        Fox fox = (Fox) foxTag.getBukkitEntity();
        return fox.getFoxType().name();
    }

    public static void setColor(EntityTag foxTag, String color) {
        Fox fox = (Fox) foxTag.getBukkitEntity();
        fox.setFoxType(Fox.Type.valueOf(color.toUpperCase()));
    }
}
