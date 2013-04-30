package net.aufdemrand.denizen.utilities.inventory;

import org.bukkit.entity.Player;

public interface MenuCloseBehaviour {

    /**
     * Called when a player closes a menu
     *
     * @param player The player closing the menu
     */
    public void onClose(Player player);
    
}
