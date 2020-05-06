package com.denizenscript.denizen.nms.v1_15.impl.network.handlers;

import com.denizenscript.denizen.nms.v1_15.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_15.impl.network.packets.*;
import com.denizenscript.denizen.nms.v1_15.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_15.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.crypto.SecretKey;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;

public class DenizenNetworkManagerImpl extends NetworkManager {

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
    public static Field POS_X_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("b");
    public static Field POS_Y_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("c");
    public static Field POS_Z_PACKTELENT = ReflectionHelper.getFields(PacketPlayOutEntityTeleport.class).get("d");
    public static Field POS_X_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("b");
    public static Field POS_Y_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("c");
    public static Field POS_Z_PACKENT = ReflectionHelper.getFields(PacketPlayOutEntity.class).get("d");
    public static Field BLOCKPOS_BLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutBlockChange.class).get("a");
    public static Field CHUNKCOORD_MULTIBLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutMultiBlockChange.class).get("a");
    public static Field INFOARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(PacketPlayOutMultiBlockChange.class).get("b");
    public static Field CHUNKX_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("a");
    public static Field CHUNKZ_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("b");
    public static Field BLOCKPOS_BLOCKBREAK = ReflectionHelper.getFields(PacketPlayOutBlockBreak.class).get("c");
    public static Field BLOCKDATA_BLOCKBREAK = ReflectionHelper.getFields(PacketPlayOutBlockBreak.class).get("d");

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
            || processMirrorForPacket(packet)) {
            return;
        }
        processMirrorForPacket(packet);
        processShowFakeForPacket(packet);
        processBlockLightForPacket(packet);
        oldManager.sendPacket(packet, genericfuturelistener);
    }

    public boolean shouldSendAttachOriginal(Entity e) {
        UUID attached = NMSHandler.getInstance().attachmentsA.get(e.getUniqueID());
        return attached != null && !attached.equals(player.getUniqueID());
    }

    public boolean processAttachToForPacket(Packet<?> packet) {
        try {
            if (packet instanceof PacketPlayOutEntity) {
                int ider = ENTITY_ID_PACKENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                List<UUID> attList = NMSHandler.getInstance().attachments2.get(e.getUniqueID());
                if (attList != null) {
                    for (UUID att : attList) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKENT.setInt(pNew, target.getEntityId());
                            Vector offset = NMSHandler.getInstance().attachmentOffsets.get(att);
                            if (offset != null && (packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMove || packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook)) {
                                boolean rotationBasis = NMSHandler.getInstance().attachmentRotations.contains(att);
                                Vector goalPosition;
                                if (!rotationBasis) {
                                    goalPosition = new Vector(e.locX(), e.locY(), e.locZ()).add(offset);
                                }
                                else {
                                    goalPosition = new Vector(e.locX(), e.locY(), e.locZ()).add(NMSHandler.fixOffset(offset, -e.yaw, e.pitch));
                                }
                                Vector oldPos = NMSHandler.getInstance().visiblePositions.get(target.getUniqueId());
                                if (oldPos == null) {
                                    oldPos = target.getLocation().toVector();
                                }
                                Vector moveNeeded = goalPosition.clone().subtract(oldPos);
                                NMSHandler.getInstance().visiblePositions.put(target.getUniqueId(), goalPosition.clone());
                                int offX = (int) (moveNeeded.getX() * (32 * 128));
                                int offY = (int) (moveNeeded.getY() * (32 * 128));
                                int offZ = (int) (moveNeeded.getZ() * (32 * 128));
                                if (offX < Short.MIN_VALUE || offX > Short.MAX_VALUE
                                        || offY < Short.MIN_VALUE || offY > Short.MAX_VALUE
                                        || offZ < Short.MIN_VALUE || offZ > Short.MAX_VALUE) {
                                    PacketPlayOutEntityTeleport newTeleportPacket = new PacketPlayOutEntityTeleport(e);
                                    ENTITY_ID_PACKTELENT.setInt(newTeleportPacket, target.getEntityId());
                                    POS_X_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getX());
                                    POS_Y_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getY());
                                    POS_Z_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getZ());
                                    oldManager.sendPacket(newTeleportPacket);
                                }
                                else {
                                    POS_X_PACKENT.setShort(pNew, (short) MathHelper.clamp(offX, Short.MIN_VALUE, Short.MAX_VALUE));
                                    POS_Y_PACKENT.setShort(pNew, (short) MathHelper.clamp(offY, Short.MIN_VALUE, Short.MAX_VALUE));
                                    POS_Z_PACKENT.setShort(pNew, (short) MathHelper.clamp(offZ, Short.MIN_VALUE, Short.MAX_VALUE));
                                    oldManager.sendPacket(pNew);
                                }
                            }
                            else {
                                oldManager.sendPacket(pNew);
                            }
                        }
                    }
                }
                return shouldSendAttachOriginal(e);
            }
            else if (packet instanceof PacketPlayOutEntityVelocity) {
                int ider = ENTITY_ID_PACKVELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                List<UUID> attList = NMSHandler.getInstance().attachments2.get(e.getUniqueID());
                if (attList != null) {
                    for (UUID att : attList) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKVELENT.setInt(pNew, target.getEntityId());
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
                return shouldSendAttachOriginal(e);
            }
            else if (packet instanceof PacketPlayOutEntityTeleport) {
                int ider = ENTITY_ID_PACKTELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    return false;
                }
                List<UUID> attList = NMSHandler.getInstance().attachments2.get(e.getUniqueID());
                if (attList != null) {
                    for (UUID att : attList) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKTELENT.setInt(pNew, target.getEntityId());
                            Vector offset = NMSHandler.getInstance().attachmentOffsets.get(att);
                            Vector resultPos = new Vector(POS_X_PACKTELENT.getDouble(pNew), POS_Y_PACKTELENT.getDouble(pNew), POS_Z_PACKTELENT.getDouble(pNew));
                            if (offset != null) {
                                boolean rotationBasis = NMSHandler.getInstance().attachmentRotations.contains(att);
                                Vector goalOffset;
                                if (!rotationBasis) {
                                    goalOffset = offset;
                                }
                                else {
                                    goalOffset = NMSHandler.fixOffset(offset, -e.yaw, e.pitch);
                                }
                                POS_X_PACKTELENT.setDouble(pNew, POS_X_PACKTELENT.getDouble(pNew) + goalOffset.getX());
                                POS_Y_PACKTELENT.setDouble(pNew, POS_Y_PACKTELENT.getDouble(pNew) + goalOffset.getY());
                                POS_Z_PACKTELENT.setDouble(pNew, POS_Z_PACKTELENT.getDouble(pNew) + goalOffset.getZ());
                                resultPos.add(goalOffset);
                            }
                            NMSHandler.getInstance().visiblePositions.put(target.getUniqueId(), resultPos);
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
                return shouldSendAttachOriginal(e);
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
            if (packet instanceof PacketPlayOutEntity) {
                int ider = ENTITY_ID_PACKENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (isHidden(e)) {
                    return true;
                }
            }
            else if (packet instanceof PacketPlayOutEntityVelocity) {
                int ider = ENTITY_ID_PACKVELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (isHidden(e)) {
                    return true;
                }
            }
            else if (packet instanceof PacketPlayOutEntityTeleport) {
                int ider = ENTITY_ID_PACKTELENT.getInt(packet);
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
            if (!ProfileEditorImpl.handleMirrorProfiles(playerInfo, this)) {
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

    public void processShowFakeForPacket(Packet<?> packet) {
        try {
            if (packet instanceof PacketPlayOutMapChunk) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(player.getUniqueID());
                if (map == null) {
                    return;
                }
                int chunkX = CHUNKX_MAPCHUNK.getInt(packet);
                int chunkZ = CHUNKZ_MAPCHUNK.getInt(packet);
                ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, player.getWorld().getWorld().getName());
                List<FakeBlock> blocks = FakeBlock.getFakeBlocksFor(player.getUniqueID(), chunkCoord);
                if (blocks == null) {
                    return;
                }
                FakeBlockHelper.handleMapChunkPacket((PacketPlayOutMapChunk) packet, blocks);
            }
            else if (packet instanceof PacketPlayOutMultiBlockChange) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(player.getUniqueID());
                if (map == null) {
                    return;
                }
                ChunkCoordIntPair coord = (ChunkCoordIntPair) CHUNKCOORD_MULTIBLOCKCHANGE.get(packet);
                ChunkCoordinate coordinateDenizen = new ChunkCoordinate(coord.x, coord.z, player.getWorld().getWorld().getName());
                if (!map.byChunk.containsKey(coordinateDenizen)) {
                    return;
                }
                LocationTag location = new LocationTag(player.getWorld().getWorld(), 0, 0, 0);
                PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] changeArr = (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[]) INFOARRAY_MULTIBLOCKCHANGE.get(packet);
                for (int i = 0; i < changeArr.length; i++) {
                    short blockInd = changeArr[i].b();
                    int x = blockInd & 0xF0;
                    int y = (blockInd & 0x00FF) >> 8;
                    int z = (blockInd & 0X0F) >> 4;
                    location.setX((coord.x << 4) + x);
                    location.setY(y);
                    location.setZ((coord.z << 4) + z);
                    FakeBlock block = map.byLocation.get(location);
                    if (block != null) {
                        changeArr[i] = ((PacketPlayOutMultiBlockChange) packet).new MultiBlockChangeInfo(blockInd, FakeBlockHelper.getNMSState(block));
                    }
                }
            }
            else if (packet instanceof PacketPlayOutBlockChange) {
                BlockPosition pos = (BlockPosition) BLOCKPOS_BLOCKCHANGE.get(packet);
                LocationTag loc = new LocationTag(player.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUniqueID(), loc);
                if (block != null) {
                    ((PacketPlayOutBlockChange) packet).block = FakeBlockHelper.getNMSState(block);
                }
            }
            else if (packet instanceof PacketPlayOutBlockBreak) {
                BlockPosition pos = (BlockPosition) BLOCKPOS_BLOCKBREAK.get(packet);
                LocationTag loc = new LocationTag(player.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUniqueID(), loc);
                if (block != null) {
                    BLOCKDATA_BLOCKBREAK.set(packet, FakeBlockHelper.getNMSState(block));
                }
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
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
    public void a(SecretKey secretkey) {
        oldManager.a(secretkey);
    }

    @Override
    public boolean isConnected() {
        return oldManager.isConnected();
    }

    @Override
    public boolean h() {
        return oldManager.h();
    }

    @Override
    public PacketListener i() {
        return oldManager.i();
    }

    @Override
    public IChatBaseComponent j() {
        return oldManager.j();
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
