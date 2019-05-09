package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.nms.interfaces.PlayerHelper;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.utilities.DenizenAPI;
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

    public static Map<String, InventoryScriptContainer> inventory_scripts = new ConcurrentHashMap<>(8, 0.9f, 1);
    public static Map<String, dInventory> notableInventories = new HashMap<>();
    public static Map<Inventory, String> tempInventoryScripts = new HashMap<>();

    public InventoryScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static void _savePlayerInventories() {
        PlayerHelper playerHelper = NMSHandler.getInstance().getPlayerHelper();
        for (Map.Entry<UUID, PlayerInventory> offlineInv : ImprovedOfflinePlayer.offlineInventories.entrySet()) {
            playerHelper.getOfflineData(offlineInv.getKey()).setInventory(offlineInv.getValue());
        }
        for (Map.Entry<UUID, Inventory> offlineEnderChest : ImprovedOfflinePlayer.offlineEnderChests.entrySet()) {
            playerHelper.getOfflineData(offlineEnderChest.getKey()).setEnderChest(offlineEnderChest.getValue());
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerHelper playerHelper = NMSHandler.getInstance().getPlayerHelper();
        if (ImprovedOfflinePlayer.offlineInventories.containsKey(uuid)) {
            playerHelper.getOfflineData(uuid).setInventory(ImprovedOfflinePlayer.offlineInventories.get(uuid));
            ImprovedOfflinePlayer.offlineInventories.remove(uuid);
        }
        if (ImprovedOfflinePlayer.offlineEnderChests.containsKey(uuid)) {
            playerHelper.getOfflineData(uuid).setEnderChest(ImprovedOfflinePlayer.offlineEnderChests.get(uuid));
            ImprovedOfflinePlayer.offlineEnderChests.remove(uuid);
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
