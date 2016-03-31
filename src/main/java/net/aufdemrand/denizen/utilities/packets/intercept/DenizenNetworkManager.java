package net.aufdemrand.denizen.utilities.packets.intercept;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.net.SocketAddress;

public class DenizenNetworkManager extends NetworkManager {

    private final NetworkManager oldManager;
    private final DenizenPacketListener packetListener;
    private final EntityPlayer player;

    public DenizenNetworkManager(EntityPlayer entityPlayer, NetworkManager oldManager) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = new DenizenPacketListener(this, entityPlayer);
        oldManager.setPacketListener(packetListener);
        this.player = this.packetListener.player;
    }

    public static void setNetworkManager(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        setNetworkManager(playerConnection, new DenizenNetworkManager(entityPlayer, playerConnection.networkManager));
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
        if (!PacketOutHandler.sendPacket(player, packet)) {
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
            dB.echoError(e);
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
            dB.echoError(e);
        }
        return direction;
    }

    private static void setNetworkManager(PlayerConnection playerConnection, NetworkManager networkManager) {
        try {
            networkManagerField.set(playerConnection, networkManager);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }
}
