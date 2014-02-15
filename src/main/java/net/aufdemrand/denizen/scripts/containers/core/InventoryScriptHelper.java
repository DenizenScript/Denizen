package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.nbt.ImprovedOfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryScriptHelper implements Listener {

    public static Map<String, InventoryScriptContainer> inventory_scripts = new ConcurrentHashMap<String, InventoryScriptContainer>(8, 0.9f, 1);
    public static HashMap<String, PlayerInventory> offlineInventories = new HashMap<String, PlayerInventory>();

    public InventoryScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static void _savePlayerInventories() {
        for (Map.Entry<String, PlayerInventory> offlineInv : offlineInventories.entrySet())
            new ImprovedOfflinePlayer(offlineInv.getKey()).setInventory(offlineInv.getValue());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String name = event.getPlayer().getName();
        if (offlineInventories.containsKey(name)) {
            new ImprovedOfflinePlayer(name).setInventory(offlineInventories.get(name));
            offlineInventories.remove(name);
        }
    }

}
