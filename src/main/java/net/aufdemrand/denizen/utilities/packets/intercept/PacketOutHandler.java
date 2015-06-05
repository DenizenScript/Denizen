package net.aufdemrand.denizen.utilities.packets.intercept;

import net.aufdemrand.denizen.scripts.commands.server.ExecuteCommand;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.PlayerProfileEditor;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.EntityFakePlayer;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class PacketOutHandler {

    /**
     * Handles all packets going out from the server.
     *
     * @param player the player the packet is being sent to
     * @param packet the client-bound packet
     * @return whether to cancel sending the packet
     */
    public static boolean handle(final EntityPlayer player, Packet packet) {
        try {
            if (packet instanceof PacketPlayOutChat) {
                if (ExecuteCommand.silencedPlayers.contains(player.getUniqueID())) {
                    return true;
                }
            }
            else if (packet instanceof PacketPlayOutSetSlot) {
                PacketPlayOutSetSlot ssPacket = (PacketPlayOutSetSlot) packet;
                // int windowId = set_slot_windowId.getInt(ssPacket);
                // int slotId = set_slot_slotId.getInt(ssPacket);
                ItemStack itemStack = (ItemStack) set_slot_itemStack.get(ssPacket);
                set_slot_itemStack.set(ssPacket, removeItemScriptLore(itemStack));
            }
            else if (packet instanceof PacketPlayOutWindowItems) {
                PacketPlayOutWindowItems wiPacket = (PacketPlayOutWindowItems) packet;
                // int windowId = set_slot_windowId.getInt(wiPacket);
                ItemStack[] itemStacks = (ItemStack[]) window_items_itemStackArray.get(wiPacket);
                for (int i = 0; i < itemStacks.length; i++) {
                    itemStacks[i] = removeItemScriptLore(itemStacks[i]);
                }
            }
            else if (packet instanceof PacketPlayOutNamedEntitySpawn) {
                PacketPlayOutNamedEntitySpawn nesPacket = (PacketPlayOutNamedEntitySpawn) packet;
                // int entityId = named_spawn_entityId.getInt(nesPacket);
                UUID entityUUID = (UUID) named_spawn_entityUUID.get(nesPacket);
                final Entity entity = ((WorldServer) player.getWorld()).getEntity(entityUUID);
                if (entity instanceof EntityFakePlayer) {
                    player.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                            (EntityFakePlayer) entity));
                    Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
                        @Override
                        public void run() {
                            player.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                                    (EntityFakePlayer) entity));
                        }
                    }, 5);
                }
            }
            else if (packet instanceof PacketPlayOutPlayerInfo) {
                PlayerProfileEditor.updatePlayerProfiles((PacketPlayOutPlayerInfo) packet);
            }
        } catch (Exception e) {
            dB.echoError(e);
        }
        return false;
    }

    private static ItemStack removeItemScriptLore(ItemStack itemStack) throws Exception{
        if (itemStack != null && itemStack.getTag() != null && !itemStack.getTag().isEmpty()) {
            NBTTagCompound tag = itemStack.getTag();
            NBTTagCompound display = tag.getCompound("display");
            NBTTagList lore = (NBTTagList) display.get("Lore");
            if (lore == null || lore.isEmpty()) {
                return itemStack;
            }
            String hash = null;
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.getString(i);
                if (line.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                    hash = line;
                    lore.a(i);
                    break;
                }
            }
            if (hash != null) {
                display.set("Lore", lore);
                tag.set("display", display);
                tag.setString("Denizen Item Script", hash);
                itemStack.setTag(tag);
            }
        }
        return itemStack;
    }


    //////////////////////////////////
    //// Packet Fields
    ///////////

    private static final Field set_slot_windowId, set_slot_slotId, set_slot_itemStack;
    private static final Field window_items_windowId, window_items_itemStackArray;
    private static final Field named_spawn_entityId, named_spawn_entityUUID;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutSetSlot.class);
        set_slot_windowId = fields.get("a");
        set_slot_slotId = fields.get("b");
        set_slot_itemStack = fields.get("c");

        fields = PacketHelper.registerFields(PacketPlayOutWindowItems.class);
        window_items_windowId = fields.get("a");
        window_items_itemStackArray = fields.get("b");

        fields = PacketHelper.registerFields(PacketPlayOutNamedEntitySpawn.class);
        named_spawn_entityId = fields.get("a");
        named_spawn_entityUUID = fields.get("b");
    }
}
