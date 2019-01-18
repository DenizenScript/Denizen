package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.objects.dEntity;
import org.bukkit.entity.Llama;

public class LlamaHelper {

    public static String llamaColorName(dEntity entity) {
        return ((Llama) entity.getBukkitEntity()).getColor().name();
    }

    public static void setLlamaColor(dEntity entity, String color) {
        ((Llama) entity.getBukkitEntity()).setColor(Llama.Color.valueOf(color.toUpperCase()));
    }
}
