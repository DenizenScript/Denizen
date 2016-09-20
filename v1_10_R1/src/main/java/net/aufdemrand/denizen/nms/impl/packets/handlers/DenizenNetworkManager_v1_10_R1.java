package net.aufdemrand.denizen.nms.impl.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.aufdemrand.denizen.nms.impl.EntityFakePlayer_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutChat_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutEntityMetadata_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.packets.PacketOutSpawnEntity_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketHandler;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import net.minecraft.server.v1_10_R1.*;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.net.SocketAddress;

public class DenizenNetworkManager_v1_10_R1 extends NetworkManager {

    private final NetworkManager oldManager;
    private final DenizenPacketListener_v1_10_R1 packetListener;
    private final EntityPlayer player;
    private final JavaPlugin plugin;
    private final PacketHandler packetHandler;

    public DenizenNetworkManager_v1_10_R1(EntityPlayer entityPlayer, NetworkManager oldManager, JavaPlugin plugin, PacketHandler packetHandler) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = new DenizenPacketListener_v1_10_R1(this, entityPlayer);
        oldManager.setPacketListener(packetListener);
        this.player = this.packetListener.player;
        this.plugin = plugin;
        this.packetHandler = packetHandler;
    }

    public static void setNetworkManager(Player player, JavaPlugin plugin, PacketHandler packetHandler) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        setNetworkManager(playerConnection, new DenizenNetworkManager_v1_10_R1(entityPlayer, playerConnection.networkManager, plugin, packetHandler));
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

    public void sendPacket(Packet packet) {
        // If the packet sending isn't cancelled, allow normal sending
        if (packet instanceof PacketPlayOutChat) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutChat_v1_10_R1((PacketPlayOutChat) packet))) {
                oldManager.sendPacket(packet);
            }
        }
        /*else if (packet instanceof PacketPlayOutSetSlot) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutSetSlot_v1_10_R1((PacketPlayOutSetSlot) packet))) {
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutWindowItems) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutWindowItems_v1_10_R1((PacketPlayOutWindowItems) packet))) {
                oldManager.sendPacket(packet);
            }
        }*/
        else if (packet instanceof PacketPlayOutNamedEntitySpawn
                || packet instanceof PacketPlayOutSpawnEntity
                || packet instanceof PacketPlayOutSpawnEntityLiving
                || packet instanceof PacketPlayOutSpawnEntityPainting
                || packet instanceof PacketPlayOutSpawnEntityExperienceOrb) {
            PacketOutSpawnEntity spawnEntity = new PacketOutSpawnEntity_v1_10_R1(packet);
            if (!packetHandler.sendPacket(player.getBukkitEntity(), spawnEntity)) {
                Entity entity = ((WorldServer) player.getWorld()).getEntity(spawnEntity.getEntityUuid());
                if (entity instanceof EntityFakePlayer_v1_10_R1) {
                    final EntityFakePlayer_v1_10_R1 fakePlayer = (EntityFakePlayer_v1_10_R1) entity;
                    sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, fakePlayer));
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, fakePlayer));
                        }
                    }, 5);
                }
                oldManager.sendPacket(packet);
            }
        }
        else if (packet instanceof PacketPlayOutEntityMetadata) {
            if (!packetHandler.sendPacket(player.getBukkitEntity(), new PacketOutEntityMetadata_v1_10_R1((PacketPlayOutEntityMetadata) packet))) {
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
