package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.dEntity;
import org.bukkit.entity.Llama;

public class LlamaHelper {

    public static String llamaColorName(dEntity entity) {
        return ((Llama) entity.getBukkitEntity()).getColor().name();
    }

    public static void setLlamaColor(dEntity entity, String color) {
        ((Llama) entity.getBukkitEntity()).setColor(Llama.Color.valueOf(color.toUpperCase()));
    }
}
