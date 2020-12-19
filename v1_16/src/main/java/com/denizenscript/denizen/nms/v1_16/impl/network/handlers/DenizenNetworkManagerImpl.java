package com.denizenscript.denizen.nms.v1_16.impl.network.handlers;

import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizen.nms.v1_16.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.packets.*;
import com.denizenscript.denizen.nms.v1_16.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_16.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.scripts.commands.player.DisguiseCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
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
            com.denizenscript.denizen.utilities.debugging.Debug.echoError(ex);
        }
    }

    public final NetworkManager oldManager;
    public final DenizenPacketListenerImpl packetListener;
    public final EntityPlayer player;
    public final DenizenPacketHandler packetHandler;

    public DenizenNetworkManagerImpl(EntityPlayer entityPlayer, NetworkManager oldManager, DenizenPacketHandler packetHandler) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = new DenizenPacketListenerImpl(this, entityPlayer);
        oldManager.setPacketListener(packetListener);
        this.player = this.packetListener.player;
        this.packetHandler = packetHandler;
    }

    public static void setNetworkManager(Player player, DenizenPacketHandler packetHandler) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        setNetworkManager(playerConnection, new DenizenNetworkManagerImpl(entityPlayer, playerConnection.networkManager, packetHandler));
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
        if (processAttachToForPacket(packet)
            || processHiddenEntitiesForPacket(packet)
            || processPacketHandlerForPacket(packet)
            || processMirrorForPacket(packet)
            || processDisguiseForPacket(packet, genericfuturelistener)
            || processShowFakeForPacket(packet, genericfuturelistener)) {
            return;
        }
        processBlockLightForPacket(packet);
        processCustomNameForPacket(packet);
        oldManager.sendPacket(packet, genericfuturelistener);
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
                            super.sendPacket(altPacket, genericfuturelistener);
                            return true;
                        }
                    }
                }
                else {
                    PacketPlayOutEntityMetadata altPacket = new PacketPlayOutEntityMetadata(ent.getId(), ((CraftEntity) disguise.as.entity).getHandle().getDataWatcher(), true);
                    super.sendPacket(altPacket, genericfuturelistener);
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

    public void processCustomNameForPacket(Packet<?> packet) {
        if (!(packet instanceof PacketPlayOutEntityMetadata)) {
            return;
        }
        if (!RenameCommand.hasAnyDynamicRenames()) {
            return;
        }
        PacketPlayOutEntityMetadata metadataPacket = (PacketPlayOutEntityMetadata) packet;
        try {
            int eid = ENTITY_METADATA_EID.getInt(metadataPacket);
            Entity ent = player.world.getEntity(eid);
            if (ent == null) {
                return; // If it doesn't exist on-server, it's definitely not relevant, so move on
            }
            String nameToApply = RenameCommand.getCustomNameFor(ent.getUniqueID(), player.getBukkitEntity());
            if (nameToApply == null) {
                return;
            }
            List<DataWatcher.Item<?>> data = (List<DataWatcher.Item<?>>) ENTITY_METADATA_LIST.get(metadataPacket);
            for (DataWatcher.Item item : data) {
                DataWatcherObject<?> watcherObject = item.a();
                int watcherId = watcherObject.a();
                if (watcherId == 2) { // 2: Custom name metadata
                    Optional<IChatBaseComponent> name = Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(nameToApply)));
                    item.a(name);
                }
                else if (watcherId == 3) { // 3: custom name visible metadata
                    item.a(true);
                }
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

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
                                if (isRotate) {
                                    yaw = EntityAttachmentHelper.adaptedCompressedAngle(yaw, att.positionalOffset.getYaw());
                                    pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                                }
                                Vector goalPosition = att.fixedForOffset(new Vector(e.locX(), e.locY(), e.locZ()), e.yaw, e.pitch);
                                Vector oldPos = att.visiblePosition;
                                if (oldPos == null) {
                                    oldPos = att.attached.getLocation().toVector();
                                }
                                Vector moveNeeded = goalPosition.clone().subtract(oldPos);
                                att.visiblePosition = goalPosition.clone();
                                int offX = (int) (moveNeeded.getX() * (32 * 128));
                                int offY = (int) (moveNeeded.getY() * (32 * 128));
                                int offZ = (int) (moveNeeded.getZ() * (32 * 128));
                                if (offX < Short.MIN_VALUE || offX > Short.MAX_VALUE
                                        || offY < Short.MIN_VALUE || offY > Short.MAX_VALUE
                                        || offZ < Short.MIN_VALUE || offZ > Short.MAX_VALUE) {
                                    PacketPlayOutEntityTeleport newTeleportPacket = new PacketPlayOutEntityTeleport(e);
                                    ENTITY_ID_PACKTELENT.setInt(newTeleportPacket, att.attached.getBukkitEntity().getEntityId());
                                    POS_X_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getX());
                                    POS_Y_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getY());
                                    POS_Z_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getZ());
                                    YAW_PACKTELENT.setByte(newTeleportPacket, yaw);
                                    PITCH_PACKTELENT.setByte(newTeleportPacket, pitch);
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
                                    oldManager.sendPacket(pNew);
                                }
                            }
                            else {
                                oldManager.sendPacket(pNew);
                            }
                        }
                    }
                }
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUniqueID(), e.getUniqueID());
            }
            else if (packet instanceof PacketPlayOutEntityVelocity) {
                int ider = ENTITY_ID_PACKVELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUniqueID());
                if (attList != null) {
                    for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                        EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUniqueID());
                        if (attMap.attached.isValid() && att != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKVELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUniqueID(), e.getUniqueID());
            }
            else if (packet instanceof PacketPlayOutEntityTeleport) {
                int ider = ENTITY_ID_PACKTELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUniqueID());
                if (attList != null) {
                    for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                        EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUniqueID());
                        if (attMap.attached.isValid() && att != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKTELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                            Vector resultPos = new Vector(POS_X_PACKTELENT.getDouble(pNew), POS_Y_PACKTELENT.getDouble(pNew), POS_Z_PACKTELENT.getDouble(pNew));
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
                                yaw = EntityAttachmentHelper.adaptedCompressedAngle(yaw, att.positionalOffset.getYaw());
                                pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                                POS_X_PACKTELENT.setDouble(pNew, resultPos.getX());
                                POS_Y_PACKTELENT.setDouble(pNew, resultPos.getY());
                                POS_Z_PACKTELENT.setDouble(pNew, resultPos.getZ());
                                YAW_PACKTELENT.setByte(pNew, yaw);
                                PITCH_PACKTELENT.setByte(pNew, pitch);
                            }
                            att.visiblePosition = resultPos.clone();
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUniqueID(), e.getUniqueID());
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    public boolean isHidden(Entity entity) {
        return entity != null && NMSHandler.getEntityHelper().isHidden(player.getBukkitEntity(), entity.getBukkitEntity().getUniqueId());
    }

    public boolean processHiddenEntitiesForPacket(Packet<?> packet) {
        if (EntityHelper.hiddenEntitiesEntPl.isEmpty()) {
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
            if (!ProfileEditorImpl.handleAlteredProfiles(playerInfo, this)) {
                return true;
            }
            ProfileEditorImpl.updatePlayerProfiles(playerInfo);
        }
        return false;
    }

    public boolean processPacketHandlerForPacket(Packet<?> packet) {
        if (packet instanceof PacketPlayOutChat) {
            return packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutChatImpl((PacketPlayOutChat) packet));
        }
        else if (packet instanceof PacketPlayOutEntityMetadata) {
            return packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutEntityMetadataImpl((PacketPlayOutEntityMetadata) packet));
        }
        return false;
    }

    public boolean processShowFakeForPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
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
                if (blocks == null) {
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
