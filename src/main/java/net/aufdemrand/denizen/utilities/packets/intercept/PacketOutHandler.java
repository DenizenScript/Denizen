package net.aufdemrand.denizen.utilities.packets.intercept;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.events.player.PlayerReceivesMessageScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.player.GlowCommand;
import net.aufdemrand.denizen.scripts.commands.server.ExecuteCommand;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.PlayerProfileEditor;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.EntityFakePlayer;
import net.aufdemrand.denizen.utilities.entity.HideEntity;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class PacketOutHandler {

    /**
     * Handles all packets going out from the server.
     *
     * @param player the player the packet is being sent to
     * @param packet the client-bound packet
     * @return whether to cancel sending the packet
     */
    public static boolean sendPacket(final EntityPlayer player, final Packet packet) {
        try {
            if (packet instanceof PacketPlayOutChat) {
                if (ExecuteCommand.silencedPlayers.contains(player.getUniqueID())) {
                    return true;
                }
                final PlayerReceivesMessageScriptEvent event = PlayerReceivesMessageScriptEvent.instance;
                if (event.loaded) {
                    FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            PacketPlayOutChat cPacket = (PacketPlayOutChat) packet;
                            int pos = chat_position.getInt(cPacket);
                            if (pos != 2) {
                                IChatBaseComponent baseComponent = (IChatBaseComponent) chat_message.get(cPacket);
                                boolean bungee = false;
                                if (baseComponent != null) {
                                    event.message = new Element(baseComponent.toPlainText());
                                    event.rawJson = new Element(IChatBaseComponent.ChatSerializer.a(baseComponent));
                                } else if (cPacket.components != null) {
                                    event.message = new Element(BaseComponent.toPlainText(cPacket.components));
                                    event.rawJson = new Element(ComponentSerializer.toString(cPacket.components));
                                    bungee = true;
                                }
                                event.system = new Element(pos == 1);
                                event.player = dPlayer.mirrorBukkitPlayer(player.getBukkitEntity());
                                event.cancelled = false;
                                event.fire();
                                if (event.messageModified) {
                                    if (!bungee) {
                                        chat_message.set(cPacket, new ChatComponentText(event.message.asString()));
                                    } else {
                                        cPacket.components = new BaseComponent[]{new TextComponent(event.message.asString())};
                                    }
                                } else if (event.rawJsonModified) {
                                    if (!bungee) {
                                        chat_message.set(cPacket, IChatBaseComponent.ChatSerializer.a(event.rawJson.asString()));
                                    } else {
                                        cPacket.components = ComponentSerializer.parse(event.rawJson.asString());
                                    }
                                }
                                return event.cancelled;
                            }
                            return false;
                        }
                    });
                    if (Bukkit.isPrimaryThread()) {
                        futureTask.run();
                    }
                    else {
                        Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), futureTask);
                    }
                    return futureTask.get();
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
            else if (packet instanceof PacketPlayOutEntityMetadata) {
                PacketPlayOutEntityMetadata emPacket = (PacketPlayOutEntityMetadata) packet;
                int eid = metadata_eid.getInt(emPacket);
                HashSet<UUID> players = GlowCommand.glowViewers.get(eid);
                // TODO: Check effect type against GLOWING (24)
                if (players == null) {
                    return false;
                }
                List<DataWatcher.Item<?>> items = (List<DataWatcher.Item<?>>) metadata_data.get(emPacket);
                for (DataWatcher.Item<?> it : items) {
                    if (it.a().a() == 0) {
                        // TODO: Instead of cancelling, casually strip out the 0x40 "Glowing" metadata rather than cancelling entirely?
                        return !players.contains(player.getUniqueID());
                    }
                }
                return false;
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
                        ItemStack buyItem1 = serializer.k();
                        newSerializer.a(removeItemScriptLore(buyItem1));
                        // The item to be bought
                        ItemStack buyItem3 = serializer.k();
                        newSerializer.a(removeItemScriptLore(buyItem3));
                        // Whether there is a second item cost
                        boolean hasItem2 = serializer.readBoolean();
                        newSerializer.writeBoolean(hasItem2);
                        // The second item cost, if there is one
                        if (hasItem2) {
                            ItemStack buyItem2 = serializer.k();
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
                    lore.remove(i);
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
        UUID entityUUID = entry.b().getUniqueID();
        if (HideEntity.hiddenEntities.get(playerUUID).contains(entityUUID)) {
            entry.clear(player);
            return true;
        }
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
    private static final Field metadata_eid, metadata_data;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutChat.class);
        chat_message = fields.get("a");
        chat_position = fields.get("b");

        fields = PacketHelper.registerFields(PacketPlayOutSetSlot.class);
        set_slot_windowId = fields.get("a");
        set_slot_slotId = fields.get("b");
        set_slot_itemStack = fields.get("c");

        fields = PacketHelper.registerFields(PacketPlayOutWindowItems.class);
        window_items_windowId = fields.get("a");
        window_items_itemStackArray = fields.get("b");

        fields = PacketHelper.registerFields(PacketPlayOutNamedEntitySpawn.class);
        named_spawn_entityId = fields.get("a");
        named_spawn_entityUUID = fields.get("b");

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntity.class);
        spawn_entityId = fields.get("a"); // Other fields currently irrelevant

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityLiving.class);
        spawn_living_entityId = fields.get("a"); // Other fields currently irrelevant

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityPainting.class);
        spawn_painting_entityId = fields.get("a"); // Other fields currently irrelevant

        fields = PacketHelper.registerFields(PacketPlayOutSpawnEntityExperienceOrb.class);
        spawn_experience_entityId = fields.get("a"); // Other fields currently irrelevant

        fields = PacketHelper.registerFields(PacketPlayOutCustomPayload.class);
        custom_name = fields.get("a");
        custom_serializer = fields.get("b");

        fields = PacketHelper.registerFields(PacketPlayOutEntityMetadata.class);
        metadata_eid = fields.get("a");
        metadata_data = fields.get("b");
    }
}
