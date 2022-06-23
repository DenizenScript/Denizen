package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class InventoryScriptHelper implements Listener {

    public static boolean isPersonalSpecialInv(InventoryType type) {
        return type == InventoryType.ANVIL || type == InventoryType.WORKBENCH;
    }

    public static boolean isPersonalSpecialInv(Inventory inv) {
        return isPersonalSpecialInv(inv.getType());
    }

    public static Map<Inventory, InventoryTag> notedInventories = new HashMap<>();

    public static HashMap<String, InventoryScriptContainer> inventoryScripts = new HashMap<>();

    public InventoryScriptHelper() {
        Denizen.getInstance().getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
    }

    public static void _savePlayerInventories() {
        for (Map.Entry<UUID, PlayerInventory> offlineInv : ImprovedOfflinePlayer.offlineInventories.entrySet()) {
            NMSHandler.playerHelper.getOfflineData(offlineInv.getKey()).setInventory(offlineInv.getValue());
        }
        for (Map.Entry<UUID, Inventory> offlineEnderChest : ImprovedOfflinePlayer.offlineEnderChests.entrySet()) {
            NMSHandler.playerHelper.getOfflineData(offlineEnderChest.getKey()).setEnderChest(offlineEnderChest.getValue());
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (ImprovedOfflinePlayer.offlineInventories.containsKey(uuid)) {
            NMSHandler.playerHelper.getOfflineData(uuid).setInventory(ImprovedOfflinePlayer.offlineInventories.get(uuid));
            ImprovedOfflinePlayer.offlineInventories.remove(uuid);
        }
        if (ImprovedOfflinePlayer.offlineEnderChests.containsKey(uuid)) {
            NMSHandler.playerHelper.getOfflineData(uuid).setEnderChest(ImprovedOfflinePlayer.offlineEnderChests.get(uuid));
            ImprovedOfflinePlayer.offlineEnderChests.remove(uuid);
        }
    }

    public static HashSet<ClickType> allowedClicks = new HashSet<>(Arrays.asList(ClickType.CONTROL_DROP, ClickType.CREATIVE, ClickType.DROP, ClickType.LEFT,
            ClickType.MIDDLE, ClickType.NUMBER_KEY, ClickType.RIGHT, ClickType.WINDOW_BORDER_LEFT, ClickType.WINDOW_BORDER_RIGHT));

    public static boolean isGUI(Inventory inv) {
        InventoryTag inventory = InventoryTag.mirrorBukkitInventory(inv);
        if (inventory.getIdHolder() instanceof ScriptTag) {
            if (((InventoryScriptContainer) ((ScriptTag) inventory.getIdHolder()).getContainer()).gui) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerClicks(InventoryClickEvent event) {
        if (event.getRawSlot() >= event.getInventory().getSize() || event.getRawSlot() < 0) {
            if (allowedClicks.contains(event.getClick())) {
                return;
            }
        }
        if (isGUI(event.getInventory())) {
            event.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
                ((Player) event.getWhoClicked()).updateInventory();
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDrags(InventoryDragEvent event) {
        if (isGUI(event.getInventory())) {
            boolean anyInTop = false;
            for (int slot : event.getRawSlots()) {
                if (slot < event.getInventory().getSize()) {
                    anyInTop = true;
                    break;
                }
            }
            if (anyInTop) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCloses(InventoryCloseEvent event) {
        if (isPersonalSpecialInv(event.getInventory()) && isGUI(event.getInventory())) {
            event.getInventory().clear();
        }
    }
}
