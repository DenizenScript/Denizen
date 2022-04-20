package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemChangeMessage implements Listener {

    static {
        Denizen.getInstance().getServer().getPluginManager().registerEvents(new ItemChangeMessage(),
                Denizen.getInstance());
    }

    private static final Map<UUID, Integer> slotChanged = new HashMap<>();

    public static void sendMessage(Player player, String message) {
        ItemStack item = player.getEquipment().getItemInMainHand();
        // If the player is holding air, force a light gray stained glass pane,
        // which is probably the least intrusive
        if (item == null || item.getType() == Material.AIR) {
            item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        }
        else {
            item = item.clone();
        }
        ItemTag itemTag = new ItemTag(item);
        NMSHandler.itemHelper.setDisplayName(itemTag, message);
        int slot = player.getInventory().getHeldItemSlot() + 36;
        NMSHandler.packetHelper.setSlot(player, slot, item, true);
        slotChanged.put(player.getUniqueId(), slot);
    }

    public static void resetItem(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (slotChanged.containsKey(uuid)) {
            int slot = slotChanged.get(uuid);
            ItemStack itemStack = player.getEquipment().getItemInMainHand();
            NMSHandler.packetHelper.setSlot(player, slot, itemStack, true);
            slotChanged.remove(uuid);
        }
    }

    @EventHandler
    public void playerItemHeld(PlayerItemHeldEvent event) {
        resetItem(event.getPlayer());
    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            resetItem((Player) event.getPlayer());
        }
    }

    // Breaking blocks with tools and such will reset the item automatically...
    // sadly, this sends the display name of the real item when that happens.
    // This also occurs when right clicking
    // TODO: find a super hacky way around that?
    // TODO: Use a packet override for the above

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            slotChanged.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            slotChanged.remove(player.getUniqueId());
        }
    }
}
