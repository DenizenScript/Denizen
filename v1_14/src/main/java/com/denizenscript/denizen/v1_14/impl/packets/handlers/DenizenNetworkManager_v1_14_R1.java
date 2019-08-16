package com.denizenscript.denizen.v1_14.impl.packets.handlers;

import com.denizenscript.denizen.v1_14.impl.blocks.BlockLight_v1_14_R1;
import com.denizenscript.denizen.v1_14.impl.entities.EntityFakePlayer_v1_14_R1;
import com.denizenscript.denizen.nms.impl.packets.*;
import com.denizenscript.denizen.nms.interfaces.packets.PacketHandler;
import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizen.v1_14.impl.packets.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.v1_14.impl.ProfileEditor_v1_14_R1;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.crypto.SecretKey;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.UUID;

public class DenizenNetworkManager_v1_14_R1 extends NetworkManager {

    public final NetworkManager oldManager;
    public final DenizenPacketListener_v1_14_R1 packetListener;
    public final EntityPlayer player;
    public final PacketHandler packetHandler;

    public DenizenNetworkManager_v1_14_R1(EntityPlayer entityPlayer, NetworkManager oldManager, PacketHandler packetHandler) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = new DenizenPacketListener_v1_14_R1(this, entityPlayer);
        oldManager.setPacketListener(packetListener);
        this.player = this.packetListener.player;
        this.packetHandler = packetHandler;
    }

    public static void setNetworkManager(Player player, PacketHandler packetHandler) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        setNetworkManager(playerConnection, new DenizenNetworkManager_v1_14_R1(entityPlayer, playerConnection.networkManager, packetHandler));
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
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet packet) throws Exception { // TODO: Check mapping update. Previously overrode 'a', and channelRead0 was overriden separately.
        if (oldManager.channel.isOpen()) {
            try {
                packet.a(this.packetListener);
            }
            catch (Exception e) {
                // Do nothing
                //dB.echoError(e);
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
        // If the packet sending isn't cancelled, allow normal sending
        if (packet instanceof PacketPlayOutChat) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutChat_v1_14_R1((PacketPlayOutChat) packet))) {
                oldManager.sendPacket(packet, genericfuturelistener);
            }
        }
        else if (packet instanceof PacketPlayOutEntity) {
            try {
                int ider = ENTITY_ID_PACKENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    oldManager.sendPacket(packet, genericfuturelistener);
                }
                else {
                    if (!NMSHandler.getInstance().attachmentsA.containsKey(e.getUniqueID())
                            || NMSHandler.getInstance().attachmentsA.get(e.getUniqueID()).equals(player.getUniqueID())) {
                        oldManager.sendPacket(packet, genericfuturelistener);
                    }
                    UUID att = NMSHandler.getInstance().attachments2.get(e.getUniqueID());
                    if (att != null) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKENT.setInt(pNew, target.getEntityId());
                            Vector offset = NMSHandler.getInstance().attachmentOffsets.get(att);
                            if (offset != null && (packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMove || packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook)) {
                                boolean rotationBasis = NMSHandler.getInstance().attachmentRotations.contains(att);
                                Vector goalPosition;
                                if (!rotationBasis) {
                                    goalPosition = new Vector(e.locX, e.locY, e.locZ).add(offset);
                                }
                                else {
                                    goalPosition = new Vector(e.locX, e.locY, e.locZ).add(NMSHandler.fixOffset(offset, -e.yaw, e.pitch));
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
                                    return;
                                }
                                POS_X_PACKENT.setInt(pNew, MathHelper.clamp(offX, Short.MIN_VALUE, Short.MAX_VALUE));
                                POS_Y_PACKENT.setInt(pNew, MathHelper.clamp(offY, Short.MIN_VALUE, Short.MAX_VALUE));
                                POS_Z_PACKENT.setInt(pNew, MathHelper.clamp(offZ, Short.MIN_VALUE, Short.MAX_VALUE));
                            }
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
        else if (packet instanceof PacketPlayOutEntityVelocity) {
            try {
                int ider = ENTITY_ID_PACKVELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    oldManager.sendPacket(packet, genericfuturelistener);
                }
                else {
                    if (!NMSHandler.getInstance().attachmentsA.containsKey(e.getUniqueID())
                            || NMSHandler.getInstance().attachmentsA.get(e.getUniqueID()).equals(player.getUniqueID())) {
                        oldManager.sendPacket(packet, genericfuturelistener);
                    }
                    UUID att = NMSHandler.getInstance().attachments2.get(e.getUniqueID());
                    if (att != null) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKVELENT.setInt(pNew, target.getEntityId());
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
        else if (packet instanceof PacketPlayOutEntityTeleport) {
            try {
                int ider = ENTITY_ID_PACKTELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    oldManager.sendPacket(packet, genericfuturelistener);
                }
                else {
                    if (!NMSHandler.getInstance().attachmentsA.containsKey(e.getUniqueID())
                            || NMSHandler.getInstance().attachmentsA.get(e.getUniqueID()).equals(player.getUniqueID())) {
                        oldManager.sendPacket(packet, genericfuturelistener);
                    }
                    UUID att = NMSHandler.getInstance().attachments2.get(e.getUniqueID());
                    if (att != null) {
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
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
        else if (packet instanceof PacketPlayOutNamedEntitySpawn
                || packet instanceof PacketPlayOutSpawnEntity
                || packet instanceof PacketPlayOutSpawnEntityLiving
                || packet instanceof PacketPlayOutSpawnEntityPainting
                || packet instanceof PacketPlayOutSpawnEntityExperienceOrb) {
            PacketOutSpawnEntity spawnEntity = new PacketOutSpawnEntity_v1_14_R1(player, packet);
            final Entity entity = player.getWorld().getEntity(spawnEntity.getEntityId());
            if (entity == null) {
                oldManager.sendPacket(packet, genericfuturelistener);
            }
            else if (!NMSHandler.getInstance().getEntityHelper().isHidden(player.getBukkitEntity(), entity.getBukkitEntity())) {
                if (entity instanceof EntityFakePlayer_v1_14_R1) {
                    final EntityFakePlayer_v1_14_R1 fakePlayer = (EntityFakePlayer_v1_14_R1) entity;
                    sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fakePlayer));
                    Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(),
                            () -> sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fakePlayer)), 5);
                }
                oldManager.sendPacket(packet, genericfuturelistener);
            }
        }
        else if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo playerInfo = (PacketPlayOutPlayerInfo) packet;
            if (ProfileEditor_v1_14_R1.handleMirrorProfiles(playerInfo, this, genericfuturelistener)) {
                ProfileEditor_v1_14_R1.updatePlayerProfiles(playerInfo);
                oldManager.sendPacket(playerInfo);
            }
        }
        else if (packet instanceof PacketPlayOutEntityMetadata) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutEntityMetadata_v1_14_R1((PacketPlayOutEntityMetadata) packet))) {
                oldManager.sendPacket(packet, genericfuturelistener);
            }
        }
        else if (packet instanceof PacketPlayOutSetSlot) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutSetSlot_v1_14_R1((PacketPlayOutSetSlot) packet))) {
                oldManager.sendPacket(packet, genericfuturelistener);
            }
        }
        else if (packet instanceof PacketPlayOutWindowItems) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutWindowItems_v1_14_R1((PacketPlayOutWindowItems) packet))) {
                oldManager.sendPacket(packet, genericfuturelistener);
            }
        }
        else if (packet instanceof PacketPlayOutOpenWindowMerchant) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutTradeList_v1_14_R1((PacketPlayOutOpenWindowMerchant) packet))) {
                oldManager.sendPacket(packet, genericfuturelistener);
            }
        }
        else if (packet instanceof PacketPlayOutLightUpdate) {
            BlockLight_v1_14_R1.checkIfLightsBrokenByPacket((PacketPlayOutLightUpdate) packet, player.world);
            oldManager.sendPacket(packet, genericfuturelistener);
        }
        else if (packet instanceof PacketPlayOutBlockChange) {
            BlockLight_v1_14_R1.checkIfLightsBrokenByPacket((PacketPlayOutBlockChange) packet, player.world);
            oldManager.sendPacket(packet, genericfuturelistener);
        }
        else {
            oldManager.sendPacket(packet, genericfuturelistener);
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
