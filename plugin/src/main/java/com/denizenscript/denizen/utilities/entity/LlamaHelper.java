package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import org.bukkit.entity.Llama;

public class LlamaHelper {

    public static String llamaColorName(EntityTag entity) {
        return ((Llama) entity.getBukkitEntity()).getColor().name();
    }

    public static void setLlamaColor(EntityTag entity, String color) {
        ((Llama) entity.getBukkitEntity()).setColor(Llama.Color.valueOf(color.toUpperCase()));
    }
}
