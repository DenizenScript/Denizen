package com.denizenscript.denizen.nms.v1_16.impl.network.handlers;

import com.denizenscript.denizen.events.player.PlayerReceivesMessageScriptEvent;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizen.nms.v1_16.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_16.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_16.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.packets.PacketOutChatImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.packets.PacketOutEntityMetadataImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.packets.PacketOutSpawnEntityImpl;
import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.scripts.commands.entity.SneakCommand;
import com.denizenscript.denizen.scripts.commands.player.DisguiseCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizen.utilities.entity.HideEntitiesHelper;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizen.utilities.packets.HideParticles;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.crypto.Cipher;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.*;

public class DenizenNetworkManagerImpl extends NetworkManager {

    public static void copyPacket(Packet<?> original, Packet<?> newPacket) {
        try {
            PacketDataSerializer copier = new PacketDataSerializer(Unpooled.buffer());
            original.b(copier);
            newPacket.a(copier);
        }
        catch (IOException ex) {
            Debug.echoError(ex);
        }
    }

    public final NetworkManager oldManager;
    public final DenizenPacketListenerImpl packetListener;
    public final EntityPlayer player;
    public int packetsSent, packetsReceived;

    public DenizenNetworkManagerImpl(EntityPlayer entityPlayer, NetworkManager oldManager) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = new DenizenPacketListenerImpl(this, entityPlayer);
        oldManager.setPacketListener(packetListener);
        this.player = this.packetListener.player;
    }

    public static void setNetworkManager(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        setNetworkManager(playerConnection, new DenizenNetworkManagerImpl(entityPlayer, playerConnection.networkManager));
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
        oldManager.channelActive(channelhandlercontext);
    }

    @Override
    public void setProtocol(EnumProtocol enumprotocol) {
        oldManager.setProtocol(enumprotocol);
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelhandlercontext) throws Exception {
        oldManager.channelInactive(channelhandlercontext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
        oldManager.exceptionCaught(channelhandlercontext, throwable);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet packet) throws Exception {
        // TODO: Check mapping update. Previously overrode 'a', and channelRead0 was overriden separately.
        if (oldManager.channel.isOpen()) {
            try {
                packet.a(this.packetListener);
            }
            catch (Exception e) {
                // Do nothing
            }
        }
    }

    @Override
    public void setPacketListener(PacketListener packetlistener) {
        oldManager.setPacketListener(packetlistener);
    }

    public static Field ENTITY_ID_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("a");
    public static Field ENTITY_ID_PACKVELENT = ReflectionHelper.getFields(PacketPlayOutEntityVelocity.class).get("a");
    public static Field ENTITY_ID_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("a");
    public static Field ENTITY_ID_NAMEDENTSPAWN = ReflectionHelper.getFields(PacketPlayOutNamedEntitySpawn.class).get("a");
    public static Field ENTITY_ID_SPAWNENT = ReflectionHelper.getFields(PacketPlayOutSpawnEntity.class).get("a");
    public static Field ENTITY_ID_SPAWNENTLIVING = ReflectionHelper.getFields(PacketPlayOutSpawnEntityLiving.class).get("a");
    public static Field POS_X_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("b");
    public static Field POS_Y_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("c");
    public static Field POS_Z_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("d");
    public static Field YAW_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("e");
    public static Field PITCH_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("f");
    public static Field POS_X_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("b");
    public static Field POS_Y_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("c");
    public static Field POS_Z_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("d");
    public static Field YAW_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("e");
    public static Field PITCH_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("f");
    public static Field BLOCKPOS_BLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutBlockChange.class).get("a");
    public static Field SECTIONPOS_MULTIBLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutMultiBlockChange.class).get("a");
    public static Field OFFSETARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutMultiBlockChange.class).get("b");
    public static Field BLOCKARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutMultiBlockChange.class).get("c");
    public static Field CHUNKX_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("a");
    public static Field CHUNKZ_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("b");
    public static Field BLOCKPOS_BLOCKBREAK = ReflectionHelper.getFields(PacketPlayOutBlockBreak.class).get("c");
    public static Field BLOCKDATA_BLOCKBREAK = ReflectionHelper.getFields(PacketPlayOutBlockBreak.class).get("d");
    public static Field ENTITY_METADATA_EID = ReflectionHelper.getFields(PacketPlayOutEntityMetadata.class).get("a");
    public static Field ENTITY_METADATA_LIST = ReflectionHelper.getFields(PacketPlayOutEntityMetadata.class).get("b");
    public static Field WORLD_PARTICLES_PARTICLETYPE = ReflectionHelper.getFields(PacketPlayOutWorldParticles.class).get("j");
    public static Field ENTITY_EQUIPMENT_EID = ReflectionHelper.getFields(PacketPlayOutEntityEquipment.class).get("a");
    public static Field ENTITY_EQUIPMENT_DATALIST = ReflectionHelper.getFields(PacketPlayOutEntityEquipment.class).get("b");
    public static Field ENTITY_STATUS_EID = ReflectionHelper.getFields(PacketPlayOutEntityStatus.class).get("a");
    public static Field ENTITY_STATUS_CODE = ReflectionHelper.getFields(PacketPlayOutEntityStatus.class).get("b");
    public static Field WINDOW_ITEMS_WINDOW = ReflectionHelper.getFields(PacketPlayOutWindowItems.class).get("a");
    public static Field WINDOW_ITEMS_CONTENTS = ReflectionHelper.getFields(PacketPlayOutWindowItems.class).get("b");
    public static Field SET_SLOT_WINDOW = ReflectionHelper.getFields(PacketPlayOutSetSlot.class).get("a");
    public static Field SET_SLOT_SLOT = ReflectionHelper.getFields(PacketPlayOutSetSlot.class).get("b");

    public static Object duplo(Object a) {
        try {
            Class clazz = a.getClass();
            Object reter = clazz.newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                f.set(reter, f.get(a));
            }
            Class subc = clazz;
            while (subc.getSuperclass() != null) {
                subc = subc.getSuperclass();
                for (Field f : subc.getDeclaredFields()) {
                    f.setAccessible(true);
                    f.set(reter, f.get(a));
                }
            }
            return reter;
        }
        catch (Exception e) {
            Debug.echoError(e);
            return null;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        sendPacket(packet, null);
    }

    @Override
    public void sendPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        if (NMSHandler.debugPackets) {
            Debug.log("Packet: " + packet.getClass().getCanonicalName() + " sent to " + player.getName());
        }
        packetsSent++;
        if (processAttachToForPacket(packet)
            || processHiddenEntitiesForPacket(packet)
            || processPacketHandlerForPacket(packet)
            || processMirrorForPacket(packet)
            || processParticlesForPacket(packet)
            || processDisguiseForPacket(packet, genericfuturelistener)
            || processMetadataChangesForPacket(packet, genericfuturelistener)
            || processEquipmentForPacket(packet, genericfuturelistener)
            || processShowFakeForPacket(packet, genericfuturelistener)) {
            return;
        }
        processBlockLightForPacket(packet);
        oldManager.sendPacket(packet, genericfuturelistener);
    }

    public boolean processEquipmentForPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof PacketPlayOutEntityEquipment) {
                HashMap<UUID, FakeEquipCommand.EquipmentOverride> playersMap = FakeEquipCommand.overrides.get(player.getUniqueID());
                if (playersMap == null) {
                    return false;
                }
                int eid = ENTITY_EQUIPMENT_EID.getInt(packet);
                Entity ent = player.world.getEntity(eid);
                if (ent == null) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = playersMap.get(ent.getUniqueID());
                if (override == null) {
                    return false;
                }
                List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>((List) ENTITY_EQUIPMENT_DATALIST.get(packet));
                PacketPlayOutEntityEquipment newPacket = new PacketPlayOutEntityEquipment();
                ENTITY_EQUIPMENT_EID.setInt(newPacket, eid);
                ENTITY_EQUIPMENT_DATALIST.set(newPacket, equipment);
                for (int i = 0; i < equipment.size(); i++) {
                    Pair<EnumItemSlot, ItemStack> pair =  equipment.get(i);
                    ItemStack use = pair.getSecond();
                    switch (pair.getFirst()) {
                        case MAINHAND:
                            use = override.hand == null ? use : CraftItemStack.asNMSCopy(override.hand.getItemStack());
                            break;
                        case OFFHAND:
                            use = override.offhand == null ? use : CraftItemStack.asNMSCopy(override.offhand.getItemStack());
                            break;
                        case CHEST:
                            use = override.chest == null ? use : CraftItemStack.asNMSCopy(override.chest.getItemStack());
                            break;
                        case HEAD:
                            use = override.head == null ? use : CraftItemStack.asNMSCopy(override.head.getItemStack());
                            break;
                        case LEGS:
                            use = override.legs == null ? use : CraftItemStack.asNMSCopy(override.legs.getItemStack());
                            break;
                        case FEET:
                            use = override.boots == null ? use : CraftItemStack.asNMSCopy(override.boots.getItemStack());
                            break;
                    }
                    equipment.set(i, new Pair<>(pair.getFirst(), use));
                }
                oldManager.sendPacket(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof PacketPlayOutEntityStatus) {
                HashMap<UUID, FakeEquipCommand.EquipmentOverride> playersMap = FakeEquipCommand.overrides.get(player.getUniqueID());
                if (playersMap == null) {
                    return false;
                }
                int eid = ENTITY_STATUS_EID.getInt(packet);
                Entity ent = player.world.getEntity(eid);
                if (!(ent instanceof EntityLiving)) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = playersMap.get(ent.getUniqueID());
                if (override == null || (override.hand == null && override.offhand == null)) {
                    return false;
                }
                if (ENTITY_STATUS_CODE.getByte(packet) != (byte) 55) {
                    return false;
                }
                List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>();
                ItemStack hand = override.hand != null ? CraftItemStack.asNMSCopy(override.hand.getItemStack()) : ((EntityLiving) ent).getItemInMainHand();
                ItemStack offhand = override.offhand != null ? CraftItemStack.asNMSCopy(override.offhand.getItemStack()) : ((EntityLiving) ent).getItemInOffHand();
                equipment.add(new Pair<>(EnumItemSlot.MAINHAND, hand));
                equipment.add(new Pair<>(EnumItemSlot.OFFHAND, offhand));
                PacketPlayOutEntityEquipment newPacket = new PacketPlayOutEntityEquipment(eid, equipment);
                oldManager.sendPacket(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof PacketPlayOutWindowItems) {
                HashMap<UUID, FakeEquipCommand.EquipmentOverride> playersMap = FakeEquipCommand.overrides.get(player.getUniqueID());
                if (playersMap == null) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = playersMap.get(player.getUniqueID());
                if (override == null) {
                    return false;
                }
                int window = WINDOW_ITEMS_WINDOW.getInt(packet);
                if (window != 0) {
                    return false;
                }
                List<ItemStack> items = (List<ItemStack>) WINDOW_ITEMS_CONTENTS.get(packet);
                if (override.head != null) {
                    items.set(5, CraftItemStack.asNMSCopy(override.head.getItemStack()));
                }
                if (override.chest != null) {
                    items.set(6, CraftItemStack.asNMSCopy(override.chest.getItemStack()));
                }
                if (override.legs != null) {
                    items.set(7, CraftItemStack.asNMSCopy(override.legs.getItemStack()));
                }
                if (override.boots != null) {
                    items.set(8, CraftItemStack.asNMSCopy(override.boots.getItemStack()));
                }
                if (override.offhand != null) {
                    items.set(45, CraftItemStack.asNMSCopy(override.offhand.getItemStack()));
                }
                if (override.hand != null) {
                    items.set(player.inventory.itemInHandIndex + 36, CraftItemStack.asNMSCopy(override.hand.getItemStack()));
                }
                PacketPlayOutWindowItems newPacket = new PacketPlayOutWindowItems();
                WINDOW_ITEMS_WINDOW.setInt(newPacket, window);
                WINDOW_ITEMS_CONTENTS.set(newPacket, items);
                oldManager.sendPacket(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof PacketPlayOutSetSlot) {
                HashMap<UUID, FakeEquipCommand.EquipmentOverride> playersMap = FakeEquipCommand.overrides.get(player.getUniqueID());
                if (playersMap == null) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = playersMap.get(player.getUniqueID());
                if (override == null) {
                    return false;
                }
                int window = SET_SLOT_WINDOW.getInt(packet);
                if (window != 0) {
                    return false;
                }
                int slot = SET_SLOT_SLOT.getInt(packet);
                org.bukkit.inventory.ItemStack item = null;
                if (slot == 5 && override.head != null) {
                    item = override.head.getItemStack();
                }
                else if (slot == 6 && override.chest != null) {
                    item = override.chest.getItemStack();
                }
                else if (slot == 7 && override.legs != null) {
                    item = override.legs.getItemStack();
                }
                else if (slot == 8 && override.boots != null) {
                    item = override.boots.getItemStack();
                }
                else if (slot == 45 && override.offhand != null) {
                    item = override.offhand.getItemStack();
                }
                else if (slot == player.inventory.itemInHandIndex + 36 && override.hand != null) {
                    item = override.hand.getItemStack();
                }
                if (item == null) {
                    return false;
                }
                PacketPlayOutSetSlot newPacket = new PacketPlayOutSetSlot(window, slot, CraftItemStack.asNMSCopy(item));
                oldManager.sendPacket(newPacket, genericfuturelistener);
                return true;
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    public boolean processParticlesForPacket(Packet<?> packet) {
        if (HideParticles.hidden.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof PacketPlayOutWorldParticles) {
                HashSet<Particle> hidden = HideParticles.hidden.get(player.getUniqueID());
                if (hidden == null) {
                    return false;
                }
                ParticleParam particle = (ParticleParam) WORLD_PARTICLES_PARTICLETYPE.get(packet);
                Particle bukkitParticle = CraftParticle.toBukkit(particle);
                if (hidden.contains(bukkitParticle)) {
                    return true;
                }
                return false;
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    private boolean antiDuplicate = false;

    public boolean processDisguiseForPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        if (DisguiseCommand.disguises.isEmpty() || antiDuplicate) {
            return false;
        }
        try {
            if (packet instanceof PacketPlayOutEntityMetadata) {
                PacketPlayOutEntityMetadata metadataPacket = (PacketPlayOutEntityMetadata) packet;
                int eid = ENTITY_METADATA_EID.getInt(metadataPacket);
                Entity ent = player.world.getEntity(eid);
                if (ent == null) {
                    return false;
                }
                HashMap<UUID, DisguiseCommand.TrackedDisguise> playerMap = DisguiseCommand.disguises.get(ent.getUniqueID());
                if (playerMap == null) {
                    return false;
                }
                DisguiseCommand.TrackedDisguise disguise = playerMap.get(player.getUniqueID());
                if (disguise == null) {
                    disguise = playerMap.get(null);
                    if (disguise == null) {
                        return false;
                    }
                }
                if (ent.getId() == player.getId()) {
                    if (!disguise.shouldFake) {
                        return false;
                    }
                    List<DataWatcher.Item<?>> data = (List<DataWatcher.Item<?>>) ENTITY_METADATA_LIST.get(metadataPacket);
                    for (DataWatcher.Item item : data) {
                        DataWatcherObject<?> watcherObject = item.a();
                        int watcherId = watcherObject.a();
                        if (watcherId == 0) { // Entity flags
                            PacketPlayOutEntityMetadata altPacket = new PacketPlayOutEntityMetadata();
                            copyPacket(metadataPacket, altPacket);
                            data = new ArrayList<>(data);
                            ENTITY_METADATA_LIST.set(altPacket, data);
                            data.remove(item);
                            byte flags = (byte) item.b();
                            flags |= 0x20; // Invisible flag
                            data.add(new DataWatcher.Item(watcherObject, flags));
                            PacketPlayOutEntityMetadata updatedPacket = getModifiedMetadataFor(altPacket);
                            oldManager.sendPacket(updatedPacket == null ? altPacket : updatedPacket, genericfuturelistener);
                            return true;
                        }
                    }
                }
                else {
                    PacketPlayOutEntityMetadata altPacket = new PacketPlayOutEntityMetadata(ent.getId(), ((CraftEntity) disguise.toOthers.entity.entity).getHandle().getDataWatcher(), true);
                    oldManager.sendPacket(altPacket, genericfuturelistener);
                    return true;
                }
                return false;
            }
            int ider = -1;
            if (packet instanceof PacketPlayOutNamedEntitySpawn) {
                ider = ENTITY_ID_NAMEDENTSPAWN.getInt(packet);
            }
            else if (packet instanceof PacketPlayOutSpawnEntity) {
                ider = ENTITY_ID_SPAWNENT.getInt(packet);
            }
            else if (packet instanceof PacketPlayOutSpawnEntityLiving) {
                ider = ENTITY_ID_SPAWNENTLIVING.getInt(packet);
            }
            if (ider != -1) {
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                HashMap<UUID, DisguiseCommand.TrackedDisguise> playerMap = DisguiseCommand.disguises.get(e.getUniqueID());
                if (playerMap == null) {
                    return false;
                }
                DisguiseCommand.TrackedDisguise disguise = playerMap.get(player.getUniqueID());
                if (disguise == null) {
                    disguise = playerMap.get(null);
                    if (disguise == null) {
                        return false;
                    }
                }
                antiDuplicate = true;
                disguise.sendTo(Collections.singletonList(new PlayerTag(player.getBukkitEntity())));
                antiDuplicate = false;
                return true;
            }
        }
        catch (Throwable ex) {
            antiDuplicate = false;
            Debug.echoError(ex);
        }
        return false;
    }

    public PacketPlayOutEntityMetadata getModifiedMetadataFor(PacketPlayOutEntityMetadata metadataPacket) {
        if (!RenameCommand.hasAnyDynamicRenames() && SneakCommand.forceSetSneak.isEmpty()) {
            return null;
        }
        try {
            int eid = ENTITY_METADATA_EID.getInt(metadataPacket);
            Entity ent = player.world.getEntity(eid);
            if (ent == null) {
                return null; // If it doesn't exist on-server, it's definitely not relevant, so move on
            }
            String nameToApply = RenameCommand.getCustomNameFor(ent.getUniqueID(), player.getBukkitEntity(), false);
            Boolean forceSneak = SneakCommand.shouldSneak(ent.getUniqueID(), player.getUniqueID());
            if (nameToApply == null && forceSneak == null) {
                return null;
            }
            List<DataWatcher.Item<?>> data = new ArrayList<>((List<DataWatcher.Item<?>>) ENTITY_METADATA_LIST.get(metadataPacket));
            boolean any = false;
            for (int i = 0; i < data.size(); i++) {
                DataWatcher.Item<?> item = data.get(i);
                DataWatcherObject<?> watcherObject = item.a();
                int watcherId = watcherObject.a();
                if (watcherId == 0 && forceSneak != null) { // 0: Entity flags
                    byte val = (Byte) item.b();
                    if (forceSneak) {
                        val |= 0x02; // 8: Crouching
                    }
                    else {
                        val &= ~0x02;
                    }
                    data.set(i, new DataWatcher.Item(watcherObject, val));
                    any = true;
                }
                else if (watcherId == 2 && nameToApply != null) { // 2: Custom name metadata
                    Optional<IChatBaseComponent> name = Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(nameToApply, ChatColor.WHITE)));
                    data.set(i, new DataWatcher.Item(watcherObject, name));
                    any = true;
                }
                else if (watcherId == 3 && nameToApply != null) { // 3: custom name visible metadata
                    data.set(i, new DataWatcher.Item(watcherObject, true));
                    any = true;
                }
            }
            if (!any) {
                return null;
            }
            PacketPlayOutEntityMetadata altPacket = new PacketPlayOutEntityMetadata();
            copyPacket(metadataPacket, altPacket);
            ENTITY_METADATA_LIST.set(altPacket, data);
            return altPacket;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public boolean processMetadataChangesForPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        if (!(packet instanceof PacketPlayOutEntityMetadata)) {
            return false;
        }
        PacketPlayOutEntityMetadata altPacket = getModifiedMetadataFor((PacketPlayOutEntityMetadata) packet);
        if (altPacket == null) {
            return false;
        }
        oldManager.sendPacket(altPacket, genericfuturelistener);
        return true;
    }

    public void tryProcessMovePacketForAttach(Packet<?> packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUniqueID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUniqueID());
                if (attMap.attached.isValid() && att != null) {
                    Packet pNew = (Packet) duplo(packet);
                    ENTITY_ID_PACKENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    if (att.positionalOffset != null && (packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMove || packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook)) {
                        boolean isRotate = packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
                        byte yaw, pitch;
                        if (att.noRotate) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.yaw);
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.pitch);
                        }
                        else if (isRotate) {
                            yaw = YAW_PACKENT.getByte(packet);
                            pitch = PITCH_PACKENT.getByte(packet);
                        }
                        else {
                            yaw = EntityAttachmentHelper.compressAngle(e.yaw);
                            pitch = EntityAttachmentHelper.compressAngle(e.pitch);
                        }
                        byte newYaw = yaw;
                        if (isRotate) {
                            newYaw = EntityAttachmentHelper.adaptedCompressedAngle(newYaw, att.positionalOffset.getYaw());
                            pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                        }
                        Vector goalPosition = att.fixedForOffset(new Vector(e.locX(), e.locY(), e.locZ()), e.yaw, e.pitch);
                        Vector oldPos = att.visiblePositions.get(player.getUniqueID());
                        boolean forceTele = false;
                        if (oldPos == null) {
                            oldPos = att.attached.getLocation().toVector();
                            forceTele = true;
                        }
                        Vector moveNeeded = goalPosition.clone().subtract(oldPos);
                        att.visiblePositions.put(player.getUniqueID(), goalPosition.clone());
                        int offX = (int) (moveNeeded.getX() * (32 * 128));
                        int offY = (int) (moveNeeded.getY() * (32 * 128));
                        int offZ = (int) (moveNeeded.getZ() * (32 * 128));
                        if (forceTele || offX < Short.MIN_VALUE || offX > Short.MAX_VALUE
                                || offY < Short.MIN_VALUE || offY > Short.MAX_VALUE
                                || offZ < Short.MIN_VALUE || offZ > Short.MAX_VALUE) {
                            PacketPlayOutEntityTeleport newTeleportPacket = new PacketPlayOutEntityTeleport(e);
                            ENTITY_ID_PACKTELENT.setInt(newTeleportPacket, att.attached.getBukkitEntity().getEntityId());
                            POS_X_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getX());
                            POS_Y_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getY());
                            POS_Z_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getZ());
                            YAW_PACKTELENT.setByte(newTeleportPacket, newYaw);
                            PITCH_PACKTELENT.setByte(newTeleportPacket, pitch);
                            if (NMSHandler.debugPackets) {
                                Debug.log("Attach Move-Tele Packet (" + forceTele + ": "+ moveNeeded + " == " + offX + "," + offY + "," + offZ + "): "
                                        + newTeleportPacket.getClass().getCanonicalName() + " for " + att.attached.getUUID()
                                        + " sent to " + player.getName() + " with original yaw " + yaw + " adapted to " + newYaw);
                            }
                            oldManager.sendPacket(newTeleportPacket);
                        }
                        else {
                            POS_X_PACKENT.setShort(pNew, (short) MathHelper.clamp(offX, Short.MIN_VALUE, Short.MAX_VALUE));
                            POS_Y_PACKENT.setShort(pNew, (short) MathHelper.clamp(offY, Short.MIN_VALUE, Short.MAX_VALUE));
                            POS_Z_PACKENT.setShort(pNew, (short) MathHelper.clamp(offZ, Short.MIN_VALUE, Short.MAX_VALUE));
                            if (isRotate) {
                                YAW_PACKENT.setByte(pNew, yaw);
                                PITCH_PACKENT.setByte(pNew, pitch);
                            }
                            if (NMSHandler.debugPackets) {
                                Debug.log("Attach Move Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getName() + " with original yaw " + yaw + " adapted to " + newYaw);
                            }
                            oldManager.sendPacket(pNew);
                        }
                    }
                    else {
                        if (NMSHandler.debugPackets) {
                            Debug.log("Attach Replica-Move Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getName());
                        }
                        oldManager.sendPacket(pNew);
                    }
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessMovePacketForAttach(packet, ent);
            }
        }
    }

    public void tryProcessVelocityPacketForAttach(Packet<?> packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUniqueID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUniqueID());
                if (attMap.attached.isValid() && att != null) {
                    Packet pNew = (Packet) duplo(packet);
                    ENTITY_ID_PACKVELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    if (NMSHandler.debugPackets) {
                        Debug.log("Attach Velocity Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getName());
                    }
                    oldManager.sendPacket(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessVelocityPacketForAttach(packet, ent);
            }
        }
    }

    public void tryProcessTeleportPacketForAttach(Packet<?> packet, Entity e, Vector relative) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUniqueID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUniqueID());
                if (attMap.attached.isValid() && att != null) {
                    Packet pNew = (Packet) duplo(packet);
                    ENTITY_ID_PACKTELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    Vector resultPos = new Vector(POS_X_PACKTELENT.getDouble(pNew), POS_Y_PACKTELENT.getDouble(pNew), POS_Z_PACKTELENT.getDouble(pNew)).add(relative);
                    if (att.positionalOffset != null) {
                        resultPos = att.fixedForOffset(resultPos, e.yaw, e.pitch);
                        byte yaw, pitch;
                        if (att.noRotate) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.yaw);
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.pitch);
                        }
                        else {
                            yaw = YAW_PACKTELENT.getByte(packet);
                            pitch = PITCH_PACKTELENT.getByte(packet);
                        }
                        byte newYaw = EntityAttachmentHelper.adaptedCompressedAngle(yaw, att.positionalOffset.getYaw());
                        pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                        POS_X_PACKTELENT.setDouble(pNew, resultPos.getX());
                        POS_Y_PACKTELENT.setDouble(pNew, resultPos.getY());
                        POS_Z_PACKTELENT.setDouble(pNew, resultPos.getZ());
                        YAW_PACKTELENT.setByte(pNew, newYaw);
                        PITCH_PACKTELENT.setByte(pNew, pitch);
                        if (NMSHandler.debugPackets) {
                            Debug.log("Attach Teleport Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getName() + " with raw yaw " + yaw + " adapted to " + newYaw);
                        }
                    }
                    att.visiblePositions.put(player.getUniqueID(), resultPos.clone());
                    oldManager.sendPacket(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessTeleportPacketForAttach(packet, ent, new Vector(ent.locX() - e.locX(), ent.locY() - e.locY(), ent.locZ() - e.locZ()));
            }
        }
    }

    public static Vector VECTOR_ZERO = new Vector(0, 0, 0);

    public boolean processAttachToForPacket(Packet<?> packet) {
        if (EntityAttachmentHelper.toEntityToData.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof PacketPlayOutEntity) {
                int ider = ENTITY_ID_PACKENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                if (!e.isPassenger()) {
                    tryProcessMovePacketForAttach(packet, e);
                }
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUniqueID(), e.getUniqueID());
            }
            else if (packet instanceof PacketPlayOutEntityVelocity) {
                int ider = ENTITY_ID_PACKVELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                tryProcessVelocityPacketForAttach(packet, e);
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUniqueID(), e.getUniqueID());
            }
            else if (packet instanceof PacketPlayOutEntityTeleport) {
                int ider = ENTITY_ID_PACKTELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                tryProcessTeleportPacketForAttach(packet, e, VECTOR_ZERO);
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUniqueID(), e.getUniqueID());
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    public boolean isHidden(Entity entity) {
        return entity != null && HideEntitiesHelper.playerShouldHide(player.getBukkitEntity().getUniqueId(), entity.getBukkitEntity());
    }

    public boolean processHiddenEntitiesForPacket(Packet<?> packet) {
        if (!HideEntitiesHelper.hasAnyHides()) {
            return false;
        }
        try {
            if (packet instanceof PacketPlayOutNamedEntitySpawn
                    || packet instanceof PacketPlayOutSpawnEntity
                    || packet instanceof PacketPlayOutSpawnEntityLiving
                    || packet instanceof PacketPlayOutSpawnEntityPainting
                    || packet instanceof PacketPlayOutSpawnEntityExperienceOrb) {
                PacketOutSpawnEntity spawnEntity = new PacketOutSpawnEntityImpl(player, packet);
                Entity entity = player.getWorld().getEntity(spawnEntity.getEntityId());
                if (isHidden(entity)) {
                    return true;
                }
                processFakePlayerSpawn(entity);
            }
            int ider = -1;
            if (packet instanceof PacketPlayOutEntity) {
                ider = ENTITY_ID_PACKENT.getInt(packet);
            }
            else if (packet instanceof PacketPlayOutEntityMetadata) {
                ider = ENTITY_METADATA_EID.getInt(packet);
            }
            else if (packet instanceof PacketPlayOutEntityVelocity) {
                ider = ENTITY_ID_PACKVELENT.getInt(packet);
            }
            else if (packet instanceof PacketPlayOutEntityTeleport) {
                ider = ENTITY_ID_PACKTELENT.getInt(packet);
            }
            if (ider != -1) {
                Entity e = player.getWorld().getEntity(ider);
                if (isHidden(e)) {
                    return true;
                }
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    public void processFakePlayerSpawn(Entity entity) {
        if (entity instanceof EntityFakePlayerImpl) {
            final EntityFakePlayerImpl fakePlayer = (EntityFakePlayerImpl) entity;
            sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fakePlayer));
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(),
                    () -> sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fakePlayer)), 5);
        }
    }

    public boolean processMirrorForPacket(Packet<?> packet) {
        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo playerInfo = (PacketPlayOutPlayerInfo) packet;
            ProfileEditorImpl.updatePlayerProfiles(playerInfo);
            if (!ProfileEditorImpl.handleAlteredProfiles(playerInfo, this)) {
                return true;
            }
        }
        return false;
    }

    public boolean processPacketHandlerForPacket(Packet<?> packet) {
        if (packet instanceof PacketPlayOutChat && DenizenPacketHandler.instance.shouldInterceptChatPacket()) {
            PacketOutChatImpl packetHelper = new PacketOutChatImpl((PacketPlayOutChat) packet);
            PlayerReceivesMessageScriptEvent result = DenizenPacketHandler.instance.sendPacket(player.getBukkitEntity(), packetHelper);
            if (result != null) {
                if (result.cancelled) {
                    return true;
                }
                if (result.modified) {
                    packetHelper.setRawJson(ComponentSerializer.toString(result.altMessageDetermination));
                }
            }
        }
        else if (packet instanceof PacketPlayOutEntityMetadata && DenizenPacketHandler.instance.shouldInterceptMetadata()) {
            return DenizenPacketHandler.instance.sendPacket(player.getBukkitEntity(), new PacketOutEntityMetadataImpl((PacketPlayOutEntityMetadata) packet));
        }
        return false;
    }

    public boolean processShowFakeForPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        if (FakeBlock.blocks.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof PacketPlayOutMapChunk) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(player.getUniqueID());
                if (map == null) {
                    return false;
                }
                int chunkX = CHUNKX_MAPCHUNK.getInt(packet);
                int chunkZ = CHUNKZ_MAPCHUNK.getInt(packet);
                ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, player.getWorld().getWorld().getName());
                List<FakeBlock> blocks = FakeBlock.getFakeBlocksFor(player.getUniqueID(), chunkCoord);
                if (blocks == null || blocks.isEmpty()) {
                    return false;
                }
                PacketPlayOutMapChunk newPacket = FakeBlockHelper.handleMapChunkPacket((PacketPlayOutMapChunk) packet, blocks);
                oldManager.sendPacket(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof PacketPlayOutMultiBlockChange) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(player.getUniqueID());
                if (map == null) {
                    return false;
                }
                SectionPosition coord = (SectionPosition) SECTIONPOS_MULTIBLOCKCHANGE.get(packet);
                ChunkCoordinate coordinateDenizen = new ChunkCoordinate(coord.getX(), coord.getZ(), player.getWorld().getWorld().getName());
                if (!map.byChunk.containsKey(coordinateDenizen)) {
                    return false;
                }
                PacketPlayOutMultiBlockChange newPacket = new PacketPlayOutMultiBlockChange();
                copyPacket(packet, newPacket);
                LocationTag location = new LocationTag(player.getWorld().getWorld(), 0, 0, 0);
                short[] originalOffsetArray = (short[])OFFSETARRAY_MULTIBLOCKCHANGE.get(newPacket);
                IBlockData[] originalDataArray = (IBlockData[])BLOCKARRAY_MULTIBLOCKCHANGE.get(newPacket);
                short[] offsetArray = Arrays.copyOf(originalOffsetArray, originalOffsetArray.length);
                IBlockData[] dataArray = Arrays.copyOf(originalDataArray, originalDataArray.length);
                OFFSETARRAY_MULTIBLOCKCHANGE.set(newPacket, offsetArray);
                BLOCKARRAY_MULTIBLOCKCHANGE.set(newPacket, dataArray);
                for (int i = 0; i < offsetArray.length; i++) {
                    short offset = offsetArray[i];
                    BlockPosition pos = coord.g(offset);
                    location.setX(pos.getX());
                    location.setY(pos.getY());
                    location.setZ(pos.getZ());
                    FakeBlock block = map.byLocation.get(location);
                    if (block != null) {
                        dataArray[i] = FakeBlockHelper.getNMSState(block);
                    }
                }
                oldManager.sendPacket(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof PacketPlayOutBlockChange) {
                BlockPosition pos = (BlockPosition) BLOCKPOS_BLOCKCHANGE.get(packet);
                LocationTag loc = new LocationTag(player.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUniqueID(), loc);
                if (block != null) {
                    PacketPlayOutBlockChange newPacket = new PacketPlayOutBlockChange();
                    copyPacket(packet, newPacket);
                    newPacket.block = FakeBlockHelper.getNMSState(block);
                    oldManager.sendPacket(newPacket, genericfuturelistener);
                    return true;
                }
            }
            else if (packet instanceof PacketPlayOutBlockBreak) {
                BlockPosition pos = (BlockPosition) BLOCKPOS_BLOCKBREAK.get(packet);
                LocationTag loc = new LocationTag(player.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUniqueID(), loc);
                if (block != null) {
                    PacketPlayOutBlockBreak newPacket = new PacketPlayOutBlockBreak();
                    copyPacket(packet, newPacket);
                    BLOCKDATA_BLOCKBREAK.set(newPacket, FakeBlockHelper.getNMSState(block));
                    oldManager.sendPacket(newPacket, genericfuturelistener);
                    return true;
                }
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    public void processBlockLightForPacket(Packet<?> packet) {
        if (BlockLight.lightsByChunk.isEmpty()) {
            return;
        }
        if (packet instanceof PacketPlayOutLightUpdate) {
            BlockLightImpl.checkIfLightsBrokenByPacket((PacketPlayOutLightUpdate) packet, player.world);
        }
        else if (packet instanceof PacketPlayOutBlockChange) {
            BlockLightImpl.checkIfLightsBrokenByPacket((PacketPlayOutBlockChange) packet, player.world);
        }
    }

    @Override
    public void a() {
        oldManager.a();
    }

    @Override
    public SocketAddress getSocketAddress() {
        return oldManager.getSocketAddress();
    }

    @Override
    public void close(IChatBaseComponent ichatbasecomponent) {
        oldManager.close(ichatbasecomponent);
    }

    @Override
    public boolean isLocal() {
        return oldManager.isLocal();
    }

    @Override
    public void a(Cipher cipher, Cipher cipher1) {
        oldManager.a(cipher, cipher1);
    }

    @Override
    public boolean isConnected() {
        return oldManager.isConnected();
    }

    @Override
    public boolean i() {
        return oldManager.i();
    }

    @Override
    public PacketListener j() {
        return oldManager.j();
    }

    @Override
    public IChatBaseComponent k() {
        return oldManager.k();
    }

    @Override
    public void stopReading() {
        oldManager.stopReading();
    }

    @Override
    public void setCompressionLevel(int i) {
        oldManager.setCompressionLevel(i);
    }

    @Override
    public void handleDisconnection() {
        oldManager.handleDisconnection();
    }

    @Override
    public float n() {
        return oldManager.n();
    }

    @Override
    public SocketAddress getRawAddress() {
        return oldManager.getRawAddress();
    }

    //////////////////////////////////
    //// Reflection Methods/Fields
    ///////////

    private static final Field protocolDirectionField;
    private static final MethodHandle networkManagerField;

    static {
        Field directionField = null;
        MethodHandle managerField = null;
        try {
            directionField = NetworkManager.class.getDeclaredField("h");
            directionField.setAccessible(true);
            managerField = ReflectionHelper.getFinalSetter(PlayerConnection.class, "networkManager");
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        protocolDirectionField = directionField;
        networkManagerField = managerField;
    }

    private static EnumProtocolDirection getProtocolDirection(NetworkManager networkManager) {
        EnumProtocolDirection direction = null;
        try {
            direction = (EnumProtocolDirection) protocolDirectionField.get(networkManager);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        return direction;
    }

    private static void setNetworkManager(PlayerConnection playerConnection, NetworkManager networkManager) {
        try {
            networkManagerField.invoke(playerConnection, networkManager);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
