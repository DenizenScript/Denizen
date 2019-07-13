package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.dEntity;
import org.bukkit.DyeColor;
import org.bukkit.entity.Shulker;

public class ShulkerHelper {

    public static DyeColor getColor(dEntity shulker) {
        return ((Shulker) shulker.getBukkitEntity()).getColor();
    }

    public static void setColor(dEntity shulker, DyeColor color) {
        ((Shulker) shulker.getBukkitEntity()).setColor(color);
    }
}
