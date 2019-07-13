package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import org.bukkit.DyeColor;
import org.bukkit.entity.Shulker;

public class ShulkerHelper {

    public static DyeColor getColor(EntityTag shulker) {
        return ((Shulker) shulker.getBukkitEntity()).getColor();
    }

    public static void setColor(EntityTag shulker, DyeColor color) {
        ((Shulker) shulker.getBukkitEntity()).setColor(color);
    }
}
