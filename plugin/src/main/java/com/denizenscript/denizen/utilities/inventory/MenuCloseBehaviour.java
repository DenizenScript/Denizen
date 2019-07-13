package com.denizenscript.denizen.utilities.inventory;

import org.bukkit.entity.Player;

public interface MenuCloseBehaviour {

    /**
     * Called when a player closes a menu
     *
     * @param player The player closing the menu
     */
    void onClose(Player player);
}
