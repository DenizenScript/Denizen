package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.nbt.ImprovedOfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryScriptHelper implements Listener {

    public static Map<String, InventoryScriptContainer> inventory_scripts = new ConcurrentHashMap<String, InventoryScriptContainer>(8, 0.9f, 1);
    public static Map<UUID, PlayerInventory> offlineInventories = new HashMap<UUID, PlayerInventory>();
    public static Map<UUID, Inventory> offlineEnderChests = new HashMap<UUID, Inventory>();
    public static Map<String, dInventory> notableInventories = new HashMap<String, dInventory>();
    public static Map<Inventory, String> tempInventoryScripts = new HashMap<Inventory, String>();

    public InventoryScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static void _savePlayerInventories() {
        for (Map.Entry<UUID, PlayerInventory> offlineInv : offlineInventories.entrySet()) {
            new ImprovedOfflinePlayer(offlineInv.getKey()).setInventory(offlineInv.getValue());
        }
        for (Map.Entry<UUID, Inventory> offlineEnderChest : offlineEnderChests.entrySet()) {
            new ImprovedOfflinePlayer(offlineEnderChest.getKey()).setEnderChest(offlineEnderChest.getValue());
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (offlineInventories.containsKey(uuid)) {
            new ImprovedOfflinePlayer(uuid).setInventory(offlineInventories.get(uuid));
            offlineInventories.remove(uuid);
        }
        if (offlineEnderChests.containsKey(uuid)) {
            new ImprovedOfflinePlayer(uuid).setEnderChest(offlineEnderChests.get(uuid));
            offlineEnderChests.remove(uuid);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (tempInventoryScripts.containsKey(inventory) && inventory.getViewers().isEmpty()) {
            tempInventoryScripts.remove(inventory);
        }
    }
}
