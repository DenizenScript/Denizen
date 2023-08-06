package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;

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

    private final static List<UUID> toClearOfflinePlayers = new ArrayList<>();

    public static void savePlayerInventories() {
        for (ImprovedOfflinePlayer player : ImprovedOfflinePlayer.offlinePlayers.values()) {
            if (player.inventory != null) { // TODO: optimize - remove inventories when no longer in use?
                player.setInventory(player.inventory);
            }
            if (player.enderchest != null) {
                player.setEnderChest(player.enderchest);
            }
            if (player.modified) {
                player.saveToFile();
            }
            if (player.timeLastLoaded + Settings.worldPlayerDataMaxCacheTicks < DenizenCore.currentTimeMonotonicMillis) {
                toClearOfflinePlayers.add(player.player);
            }
        }
        for (UUID id : toClearOfflinePlayers) {
            ImprovedOfflinePlayer.offlinePlayers.remove(id);
        }
        toClearOfflinePlayers.clear();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        ImprovedOfflinePlayer.invalidateNow(event.getPlayer().getUniqueId());
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
