package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemChangeMessage implements Listener {

    private static final Field slot_inventoryId, slot_slotId, slot_item;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutSetSlot.class);
        slot_inventoryId = fields.get("a");
        slot_slotId = fields.get("b");
        slot_item = fields.get("c");
        DenizenAPI.getCurrentInstance().getServer().getPluginManager().registerEvents(new ItemChangeMessage(),
                DenizenAPI.getCurrentInstance());
    }

    private static final Map<UUID, Integer> slotChanged = new HashMap<UUID, Integer>();

    public static PacketPlayOutSetSlot getSlotPacket(UUID player, int nmsSlot, ItemStack item) {
        PacketPlayOutSetSlot slotPacket = new PacketPlayOutSetSlot();
        try {
            slot_inventoryId.set(slotPacket, 0);
            slotChanged.put(player, nmsSlot);
            slot_slotId.set(slotPacket, nmsSlot);
            slot_item.set(slotPacket, CraftItemStack.asNMSCopy(item));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return slotPacket;
    }

    public static void sendMessage(Player player, String message) {
        ItemStack item = player.getItemInHand();
        // If the player is holding air, force a light gray stained glass pane,
        // which is probably the least intrusive
        if (item == null || item.getType() == Material.AIR) {
            item = new ItemStack(Material.STAINED_GLASS_PANE);
            item.getData().setData((byte) 8);
        }
        else
            item = item.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(message);
        item.setItemMeta(meta);
        PacketPlayOutSetSlot slotPacket = getSlotPacket(player.getUniqueId(),
                player.getInventory().getHeldItemSlot() + 36, item);
        PacketHelper.sendPacket(player, slotPacket);
    }

    public static void resetItem(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (slotChanged.containsKey(uuid)) {
            PacketPlayOutSetSlot slotPacket = getSlotPacket(uuid, slotChanged.get(uuid), player.getItemInHand());
            PacketHelper.sendPacket(player, slotPacket);
            slotChanged.remove(uuid);
        }
    }

    @EventHandler
    public void playerItemHeld(PlayerItemHeldEvent event) {
        resetItem(event.getPlayer());
    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player)
            resetItem((Player) event.getPlayer());
    }

    // Breaking blocks with tools and such will reset the item automatically...
    // sadly, this sends the display name of the real item when that happens.
    // This also occurs when right clicking
    // TODO: find a super hacky way around that?

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player != null && slotChanged.containsKey(player.getUniqueId()))
            slotChanged.remove(player.getUniqueId());
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player != null && slotChanged.containsKey(player.getUniqueId()))
            slotChanged.remove(player.getUniqueId());
    }
}
