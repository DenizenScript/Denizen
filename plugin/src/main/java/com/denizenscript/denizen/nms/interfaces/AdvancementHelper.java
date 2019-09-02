package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.Advancement;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public abstract class AdvancementHelper {

    public static org.bukkit.advancement.Advancement getAdvancement(String name) {
        NamespacedKey key;
        int colonIndex = name.indexOf(':');
        if (colonIndex != -1) {
            key = new NamespacedKey(name.substring(0, colonIndex), name.substring(colonIndex + 1));
        }
        else {
            key = NamespacedKey.minecraft(name);
        }
        return Bukkit.getAdvancement(key);
    }

    public abstract void register(Advancement advancement);

    public abstract void unregister(Advancement advancement);

    public abstract void grant(Advancement advancement, Player player);

    public abstract void revoke(Advancement advancement, Player player);

    public abstract void update(Player player);
}
