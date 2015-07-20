package net.aufdemrand.denizen.utilities.packets.intercept;

import io.netty.buffer.Unpooled;
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
                window_items_itemStackArray.set(wiPacket, itemStacks);
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
            else if (packet instanceof PacketPlayOutCustomPayload) {
                PacketPlayOutCustomPayload cPacket = (PacketPlayOutCustomPayload) packet;
                String name = (String) custom_name.get(cPacket);
                if (name.equals("MC|TrList")) {
                    PacketDataSerializer serializer = (PacketDataSerializer) custom_serializer.get(cPacket);
                    PacketDataSerializer newSerializer = new PacketDataSerializer(Unpooled.buffer());
                    // Container number, we don't need this
                    newSerializer.writeInt(serializer.readInt());
                    // Number of trades
                    byte trades = serializer.readByte();
                    newSerializer.writeByte(trades);
                    // The trades
                    for (int i = 0; i < trades; i++) {
                        // The first item cost
                        ItemStack buyItem1 = serializer.i();
                        newSerializer.a(removeItemScriptLore(buyItem1));
                        // The item to be bought
                        ItemStack buyItem3 = serializer.i();
                        newSerializer.a(removeItemScriptLore(buyItem3));
                        // Whether there is a second item cost
                        boolean hasItem2 = serializer.readBoolean();
                        newSerializer.writeBoolean(hasItem2);
                        // The second item cost, if there is one
                        ItemStack buyItem2 = null;
                        if (hasItem2) {
                            buyItem2 = serializer.i();
                            newSerializer.a(removeItemScriptLore(buyItem2));
                        }
                        // Has used max times
                        boolean usedMax = serializer.readBoolean();
                        newSerializer.writeBoolean(usedMax);
                        // Current uses
                        int uses = serializer.readInt();
                        newSerializer.writeInt(uses);
                        // Max uses
                        int maxUses = serializer.readInt();
                        newSerializer.writeInt(maxUses);
                    }
                    custom_serializer.set(cPacket, newSerializer);
                }
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return false;
    }

    private static ItemStack removeItemScriptLore(ItemStack itemStack) throws Exception {
        if (itemStack != null && itemStack.hasTag() && !itemStack.getTag().isEmpty()) {
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
    private static final Field custom_name, custom_serializer;

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

        fields = PacketHelper.registerFields(PacketPlayOutCustomPayload.class);
        custom_name = fields.get("a");
        custom_serializer = fields.get("b");
    }
}
