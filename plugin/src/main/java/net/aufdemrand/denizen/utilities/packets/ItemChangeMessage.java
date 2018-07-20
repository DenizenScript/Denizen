package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemChangeMessage implements Listener {

    static {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager().registerEvents(new ItemChangeMessage(),
                DenizenAPI.getCurrentInstance());
    }

    private static final Map<UUID, Integer> slotChanged = new HashMap<UUID, Integer>();

    public static void sendMessage(Player player, String message) {
        ItemStack item = NMSHandler.getInstance().getEntityHelper().getItemInHand(player);
        // If the player is holding air, force a light gray stained glass pane,
        // which is probably the least intrusive
        if (item == null || item.getType() == Material.AIR) {
            // TODO: better method?
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R1)) {
                item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            }
            else {
                item = new MaterialData(Material.valueOf("STAINED_GLASS_PANE"), DyeColor.GRAY.getDyeData()).toItemStack();
            }
        }
        else {
            item = item.clone();
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(message);
        item.setItemMeta(meta);
        int slot = player.getInventory().getHeldItemSlot() + 36;
        NMSHandler.getInstance().getPacketHelper().setSlot(player, slot, item, true);
        slotChanged.put(player.getUniqueId(), slot);
    }

    public static void resetItem(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (slotChanged.containsKey(uuid)) {
            int slot = slotChanged.get(uuid);
            ItemStack itemStack = NMSHandler.getInstance().getEntityHelper().getItemInHand(player);
            NMSHandler.getInstance().getPacketHelper().setSlot(player, slot, itemStack, true);
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
        if (player != null && slotChanged.containsKey(player.getUniqueId())) {
            slotChanged.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player != null && slotChanged.containsKey(player.getUniqueId())) {
            slotChanged.remove(player.getUniqueId());
        }
    }
}
