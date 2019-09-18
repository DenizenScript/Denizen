package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.Advancement;
import com.denizenscript.denizen.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class AdvancementHelper {

    public static org.bukkit.advancement.Advancement getAdvancement(String name) {
        return Bukkit.getAdvancement(Utilities.parseNamespacedKey(name));
    }

    public abstract void register(Advancement advancement);

    public abstract void unregister(Advancement advancement);

    public abstract void grant(Advancement advancement, Player player);

    public abstract void revoke(Advancement advancement, Player player);

    public abstract void update(Player player);
}
