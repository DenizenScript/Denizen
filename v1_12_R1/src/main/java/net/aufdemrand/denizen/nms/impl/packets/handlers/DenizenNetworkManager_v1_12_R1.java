package net.aufdemrand.denizen.nms.impl.packets.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.aufdemrand.denizen.nms.Handler_v1_12_R1;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.impl.ProfileEditor_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.entities.EntityFakePlayer_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutChat_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutEntityMetadata_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutSetSlot_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutSpawnEntity_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutTradeList_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutWindowItems_v1_12_R1;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketHandler;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.UUID;

public class DenizenNetworkManager_v1_12_R1 extends NetworkManager {

    private final NetworkManager oldManager;
    private final DenizenPacketListener_v1_12_R1 packetListener;
    private final EntityPlayer player;
    private final PacketHandler packetHandler;

    public DenizenNetworkManager_v1_12_R1(EntityPlayer entityPlayer, NetworkManager oldManager, PacketHandler packetHandler) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = new DenizenPacketListener_v1_12_R1(this, entityPlayer);
        oldManager.setPacketListener(packetListener);
        this.player = this.packetListener.player;
        this.packetHandler = packetHandler;
    }

    public static void setNetworkManager(Player player, PacketHandler packetHandler) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        setNetworkManager(playerConnection, new DenizenNetworkManager_v1_12_R1(entityPlayer, playerConnection.networkManager, packetHandler));
    }

    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
        oldManager.channelActive(channelhandlercontext);
    }

    public void setProtocol(EnumProtocol enumprotocol) {
        oldManager.setProtocol(enumprotocol);
    }

    public void channelInactive(ChannelHandlerContext channelhandlercontext) throws Exception {
        oldManager.channelInactive(channelhandlercontext);
    }

    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) throws Exception {
        oldManager.exceptionCaught(channelhandlercontext, throwable);
    }

    protected void a(ChannelHandlerContext channelhandlercontext, Packet packet) throws Exception {
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
            e.printStackTrace();
            return null;
        }
    }

    public void sendPacket(Packet packet) {
        // If the packet sending isn't cancelled, allow normal sending
        if (packet instanceof PacketPlayOutChat) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutChat_v1_12_R1((PacketPlayOutChat) packet))) {
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutEntity) {
            try {
                int ider = ENTITY_ID_PACKENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    oldManager.sendPacket(packet);
                }
                else {
                    if (!((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentsA.containsKey(e.getUniqueID())
                            || ((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentsA.get(e.getUniqueID()).equals(player.getUniqueID())) {
                        oldManager.sendPacket(packet);
                    }
                    UUID att = ((Handler_v1_12_R1) NMSHandler.getInstance()).attachments2.get(e.getUniqueID());
                    if (att != null) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKENT.setInt(pNew, target.getEntityId());
                            Vector offset = ((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentOffsets.get(att);
                            if (offset != null && (packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMove || packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook)) {
                                Vector goalPosition = new Vector(e.locX, e.locY, e.locZ).add(offset);
                                Vector oldPos = ((Handler_v1_12_R1) NMSHandler.getInstance()).visiblePositions.get(target.getUniqueId());
                                if (oldPos == null) {
                                    oldPos = target.getLocation().toVector();
                                }
                                Vector moveNeeded = goalPosition.clone().subtract(oldPos);
                                ((Handler_v1_12_R1) NMSHandler.getInstance()).visiblePositions.put(target.getUniqueId(), goalPosition.clone());
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
                e.printStackTrace();
            }
        }
        else if (packet instanceof PacketPlayOutEntityVelocity) {
            try {
                int ider = ENTITY_ID_PACKVELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    oldManager.sendPacket(packet);
                }
                else {
                    if (!((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentsA.containsKey(e.getUniqueID())
                            || ((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentsA.get(e.getUniqueID()).equals(player.getUniqueID())) {
                        oldManager.sendPacket(packet);
                    }
                    UUID att = ((Handler_v1_12_R1) NMSHandler.getInstance()).attachments2.get(e.getUniqueID());
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
                e.printStackTrace();
            }
        }
        else if (packet instanceof PacketPlayOutEntityTeleport) {
            try {
                int ider = ENTITY_ID_PACKTELENT.getInt(packet);
                Entity e = player.getWorld().getEntity(ider);
                if (e == null) {
                    oldManager.sendPacket(packet);
                }
                else {
                    if (!((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentsA.containsKey(e.getUniqueID())
                            || ((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentsA.get(e.getUniqueID()).equals(player.getUniqueID())) {
                        oldManager.sendPacket(packet);
                    }
                    UUID att = ((Handler_v1_12_R1) NMSHandler.getInstance()).attachments2.get(e.getUniqueID());
                    if (att != null) {
                        org.bukkit.entity.Entity target = Bukkit.getEntity(att);
                        if (target != null) {
                            Packet pNew = (Packet) duplo(packet);
                            ENTITY_ID_PACKTELENT.setInt(pNew, target.getEntityId());
                            Vector offset = ((Handler_v1_12_R1) NMSHandler.getInstance()).attachmentOffsets.get(att);
                            Vector resultPos = new Vector(POS_X_PACKTELENT.getDouble(pNew), POS_Y_PACKTELENT.getDouble(pNew), POS_Z_PACKTELENT.getDouble(pNew));
                            if (offset != null) {
                                POS_X_PACKTELENT.setDouble(pNew, POS_X_PACKTELENT.getDouble(pNew) + offset.getX());
                                POS_Y_PACKTELENT.setDouble(pNew, POS_Y_PACKTELENT.getDouble(pNew) + offset.getY());
                                POS_Z_PACKTELENT.setDouble(pNew, POS_Z_PACKTELENT.getDouble(pNew) + offset.getZ());
                                resultPos.add(offset);
                            }
                            ((Handler_v1_12_R1) NMSHandler.getInstance()).visiblePositions.put(target.getUniqueId(), resultPos);
                            oldManager.sendPacket(pNew);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (packet instanceof PacketPlayOutNamedEntitySpawn
                || packet instanceof PacketPlayOutSpawnEntity
                || packet instanceof PacketPlayOutSpawnEntityLiving
                || packet instanceof PacketPlayOutSpawnEntityPainting
                || packet instanceof PacketPlayOutSpawnEntityExperienceOrb) {
            PacketOutSpawnEntity spawnEntity = new PacketOutSpawnEntity_v1_12_R1(player, packet);
            final Entity entity = player.getWorld().getEntity(spawnEntity.getEntityId());
            if (entity == null) {
                oldManager.sendPacket(packet);
            }
            else if (!NMSHandler.getInstance().getEntityHelper().isHidden(player.getBukkitEntity(), entity.getBukkitEntity())) {
                if (entity instanceof EntityFakePlayer_v1_12_R1) {
                    final EntityFakePlayer_v1_12_R1 fakePlayer = (EntityFakePlayer_v1_12_R1) entity;
                    sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, fakePlayer));
                    Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, fakePlayer));
                        }
                    }, 5);
                }
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo playerInfo = (PacketPlayOutPlayerInfo) packet;
            ProfileEditor_v1_12_R1.updatePlayerProfiles(playerInfo);
            oldManager.sendPacket(playerInfo);
        }
        else if (packet instanceof PacketPlayOutEntityMetadata) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutEntityMetadata_v1_12_R1((PacketPlayOutEntityMetadata) packet))) {
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutSetSlot) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutSetSlot_v1_12_R1((PacketPlayOutSetSlot) packet))) {
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutWindowItems) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutWindowItems_v1_12_R1((PacketPlayOutWindowItems) packet))) {
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutCustomPayload) {
            PacketPlayOutCustomPayload payload = (PacketPlayOutCustomPayload) packet;
            PacketDataSerializer original = new PacketDataSerializer(Unpooled.buffer());
            try {
                payload.b(original);
                // Copy the data without removing it from the original
                PacketDataSerializer serializer = new PacketDataSerializer(original.getBytes(original.readerIndex(),
                        new byte[original.readableBytes()]));
                // Write the original back to avoid odd errors
                payload.a(original);
                String name = serializer.e(20);
                if (name != null && name.equals("MC|TrList")) {
                    if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutTradeList_v1_12_R1(payload, serializer))) {
                        oldManager.sendPacket(packet);
                    }
                }
                else {
                    oldManager.sendPacket(packet);
                }
            }
            catch (Exception e) {
                oldManager.sendPacket(packet);
            }
        }
        else {
            oldManager.sendPacket(packet);
        }
    }

    public void sendPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener, GenericFutureListener<? extends Future<? super Void>>... agenericfuturelistener) {
        oldManager.sendPacket(packet, genericfuturelistener, agenericfuturelistener);
    }

    public void a() {
        oldManager.a();
    }

    public SocketAddress getSocketAddress() {
        return oldManager.getSocketAddress();
    }

    public void close(IChatBaseComponent ichatbasecomponent) {
        oldManager.close(ichatbasecomponent);
    }

    public boolean isLocal() {
        return oldManager.isLocal();
    }

    public void a(SecretKey secretkey) {
        oldManager.a(secretkey);
    }

    public boolean isConnected() {
        return oldManager.isConnected();
    }

    public boolean h() {
        return oldManager.h();
    }

    public PacketListener i() {
        return oldManager.i();
    }

    public IChatBaseComponent j() {
        return oldManager.j();
    }

    public void stopReading() {
        oldManager.stopReading();
    }

    public void setCompressionLevel(int i) {
        oldManager.setCompressionLevel(i);
    }

    public void handleDisconnection() {
        oldManager.handleDisconnection();
    }

    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet object) throws Exception {
        this.a(channelhandlercontext, object);
    }

    public SocketAddress getRawAddress() {
        return oldManager.getRawAddress();
    }

    //////////////////////////////////
    //// Reflection Methods/Fields
    ///////////

    private static final Field protocolDirectionField;
    private static final Field networkManagerField;

    static {
        Field directionField = null;
        Field managerField = null;
        try {
            directionField = NetworkManager.class.getDeclaredField("h");
            directionField.setAccessible(true);
            managerField = PlayerConnection.class.getDeclaredField("networkManager");
            managerField.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return direction;
    }

    private static void setNetworkManager(PlayerConnection playerConnection, NetworkManager networkManager) {
        try {
            networkManagerField.set(playerConnection, networkManager);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
