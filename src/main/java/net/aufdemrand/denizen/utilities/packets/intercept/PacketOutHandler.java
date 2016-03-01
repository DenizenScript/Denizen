package net.aufdemrand.denizen.utilities.packets.intercept;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.events.player.PlayerReceivesMessageScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.server.ExecuteCommand;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.PlayerProfileEditor;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.EntityFakePlayer;
import net.aufdemrand.denizen.utilities.entity.HideEntity;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
// TODO: 1.9
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
                PacketPlayOutChat cPacket = (PacketPlayOutChat) packet;
                int pos = chat_position.getInt(cPacket);
                if (pos != 2) {
                    PlayerReceivesMessageScriptEvent event = PlayerReceivesMessageScriptEvent.instance;
                    // TODO: 1.9       event.message = new Element(((IChatBaseComponent) chat_message.get(cPacket)).c());
                    event.system = new Element(pos == 1);
                    event.player = dPlayer.mirrorBukkitPlayer(player.getBukkitEntity());
                    event.cancelled = false;
                    event.fire();
                    return event.cancelled;
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
                int entityId = named_spawn_entityId.getInt(nesPacket);
                if (entityIsHiding(player, entityId)) {
                    return true;
                }
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
            else if (packet instanceof PacketPlayOutSpawnEntity) {
                PacketPlayOutSpawnEntity sePacket = (PacketPlayOutSpawnEntity) packet;
                int entityId = spawn_entityId.getInt(sePacket);
                return entityIsHiding(player, entityId);
            }
            else if (packet instanceof PacketPlayOutSpawnEntityLiving) {
                PacketPlayOutSpawnEntityLiving selPacket = (PacketPlayOutSpawnEntityLiving) packet;
                int entityId = spawn_living_entityId.getInt(selPacket);
                return entityIsHiding(player, entityId);
            }
            else if (packet instanceof PacketPlayOutSpawnEntityPainting) {
                PacketPlayOutSpawnEntityPainting sepPacket = (PacketPlayOutSpawnEntityPainting) packet;
                int entityId = spawn_painting_entityId.getInt(sepPacket);
                return entityIsHiding(player, entityId);
            }
            else if (packet instanceof PacketPlayOutSpawnEntityExperienceOrb) {
                PacketPlayOutSpawnEntityExperienceOrb seePacket = (PacketPlayOutSpawnEntityExperienceOrb) packet;
                int entityId = spawn_experience_entityId.getInt(seePacket);
                return entityIsHiding(player, entityId);
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
                        // TODO: 1.9             ItemStack buyItem1 = serializer.i();
                        // TODO: 1.9        newSerializer.a(removeItemScriptLore(buyItem1));
                        // The item to be bought
                        // TODO: 1.9             ItemStack buyItem3 = serializer.i();
                        // TODO: 1.9          newSerializer.a(removeItemScriptLore(buyItem3));
                        // Whether there is a second item cost
                        boolean hasItem2 = serializer.readBoolean();
                        newSerializer.writeBoolean(hasItem2);
                        // The second item cost, if there is one
                        if (hasItem2) {
                            // TODO: 1.9              ItemStack buyItem2 = serializer.i();
                            // TODO: 1.9                newSerializer.a(removeItemScriptLore(buyItem2));
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
                    // TODO: 1.9         lore.a(i);
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

    private static boolean entityIsHiding(EntityPlayer player, int entityId) {
        UUID playerUUID = player.getUniqueID();
        if (!HideEntity.hiddenEntities.containsKey(playerUUID)) {
            return false;
        }
        EntityTracker tracker = ((WorldServer) player.world).tracker;
        EntityTrackerEntry entry = tracker.trackedEntities.get(entityId);
        if (entry == null) {
            return false;
        }
        // TODO: 1.9      UUID entityUUID = entry.tracker.getUniqueID();
// TODO: 1.9        if (HideEntity.hiddenEntities.get(playerUUID).contains(entityUUID)) {
        // TODO: 1.9          entry.clear(player);
        // TODO: 1.9          return true;
        // TODO: 1.9     }
        return false;
    }


    //////////////////////////////////
    //// Packet Fields
    ///////////

    private static final Field chat_message, chat_position;
    private static final Field set_slot_windowId, set_slot_slotId, set_slot_itemStack;
    private static final Field window_items_windowId, window_items_itemStackArray;
    private static final Field named_spawn_entityId, named_spawn_entityUUID;
    private static final Field spawn_entityId;
    private static final Field spawn_living_entityId;
    private static final Field spawn_painting_entityId;
    private static final Field spawn_experience_entityId;
    private static final Field custom_name, custom_serializer;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutChat.class);
        chat_message = fields.get("a");// TODO: 1.9
        chat_position = fields.get("b");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutSetSlot.class);
        set_slot_windowId = fields.get("a");// TODO: 1.9
        set_slot_slotId = fields.get("b");// TODO: 1.9
        set_slot_itemStack = fields.get("c");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutWindowItems.class);
        window_items_windowId = fields.get("a");// TODO: 1.9
        window_items_itemStackArray = fields.get("b");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutNamedEntitySpawn.class);
        named_spawn_entityId = fields.get("a");// TODO: 1.9
        named_spawn_entityUUID = fields.get("b");// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntity.class);
        spawn_entityId = fields.get("a"); // Other fields currently irrelevant// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityLiving.class);
        spawn_living_entityId = fields.get("a"); // Other fields currently irrelevant// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityPainting.class);
        spawn_painting_entityId = fields.get("a"); // Other fields currently irrelevant// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityExperienceOrb.class);
        spawn_experience_entityId = fields.get("a"); // Other fields currently irrelevant// TODO: 1.9

        fields = PacketHelper.registerFields(PacketPlayOutCustomPayload.class);
        custom_name = fields.get("a");// TODO: 1.9
        custom_serializer = fields.get("b");// TODO: 1.9
    }
}
