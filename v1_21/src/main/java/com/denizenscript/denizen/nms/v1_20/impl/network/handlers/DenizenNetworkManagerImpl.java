package com.denizenscript.denizen.nms.v1_20.impl.network.handlers;

import com.denizenscript.denizen.events.player.PlayerReceivesPacketScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptCodeGen;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R4.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DenizenNetworkManagerImpl extends Connection {
    // TODO: 1.20.6: there are some new methods that should potentially be overriden

    // TODO: 1.20.6: this can be cleaned up by decoding with the codec and returning the new packet
    public static <T extends Packet<?>, B extends FriendlyByteBuf> RegistryFriendlyByteBuf copyPacket(T original, StreamCodec<B, T> packetCodec) {
        try {
            RegistryFriendlyByteBuf copier = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
            packetCodec.encode((B) copier, original);
            return copier;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    @FunctionalInterface
    public interface PacketHandler<T extends Packet<ClientGamePacketListener>> {
        Packet<ClientGamePacketListener> handlePacket(DenizenNetworkManagerImpl networkManager, T packet) throws Exception;
    }

    public static final Map<Class<? extends Packet<ClientGamePacketListener>>, List<PacketHandler<?>>> packetHandlers = new HashMap<>();

    public static <T extends Packet<ClientGamePacketListener>> void registerPacketHandler(Class<T> packetClass, PacketHandler<T> handler) {
        packetHandlers.computeIfAbsent(packetClass, k -> new ArrayList<>()).add(handler);
    }

    public static <T extends Packet<ClientGamePacketListener>> void registerPacketHandler(Class<T> packetClass, BiConsumer<DenizenNetworkManagerImpl, T> handler) {
        registerPacketHandler(packetClass, (networkManager, packet) -> {
            handler.accept(networkManager, packet);
            return packet;
        });
    }

    public final Connection oldManager;
    public final DenizenPacketListenerImpl packetListener;
    public final ServerPlayer player;
    public int packetsSent, packetsReceived;

    public DenizenNetworkManagerImpl(ServerPlayer entityPlayer, Connection oldManager) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.player = entityPlayer;
        packetListener = (DenizenPacketListenerImpl) NetworkInterceptCodeGen.generateAppropriateInterceptor(this, entityPlayer, DenizenPacketListenerImpl.class, AbstractListenerPlayInImpl.class, ServerGamePacketListenerImpl.class);
        if (!(oldManager.getPacketListener() instanceof ServerConfigurationPacketListener)) {
            setListener(packetListener);
        }
    }

    public void setListener(PacketListener listener) {
        try {
            Connection_packetListener.set(oldManager, listener);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
            throw new RuntimeException("Failed to set packet listener due to reflection error", e);
        }
    }

    public static Connection getConnection(ServerPlayer player) {
        try {
            return (Connection) ServerGamePacketListener_ConnectionField.get(player.connection);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            throw new RuntimeException("Failed to get connection from player due to reflection error", ex);
        }
    }

    public static Connection getConnection(Player player) {
        return getConnection(((CraftPlayer) player).getHandle());
    }

    public static DenizenNetworkManagerImpl getNetworkManager(ServerPlayer player) {
        return (DenizenNetworkManagerImpl) getConnection(player);
    }

    public static DenizenNetworkManagerImpl getNetworkManager(Player player) {
        return getNetworkManager(((CraftPlayer) player).getHandle());
    }

    public static void setNetworkManager(Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl playerConnection = entityPlayer.connection;
        setNetworkManager(playerConnection, new DenizenNetworkManagerImpl(entityPlayer, getConnection(entityPlayer)));
    }

    public static void enableNetworkManager() {
        for (World w : Bukkit.getWorlds()) {
            for (ChunkMap.TrackedEntity tracker : ((CraftWorld) w).getHandle().getChunkSource().chunkMap.entityMap.values()) {
                ArrayList<ServerPlayerConnection> connections = new ArrayList<>(tracker.seenBy);
                tracker.seenBy.clear();
                for (ServerPlayerConnection connection : connections) {
                    tracker.seenBy.add(connection.getPlayer().connection);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return oldManager.hashCode();
    }

    @Override
    public boolean equals(Object c2) {
        return oldManager.equals(c2);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        oldManager.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        oldManager.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
        oldManager.channelActive(channelhandlercontext);
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelhandlercontext) {
        oldManager.channelInactive(channelhandlercontext);
    }

    @Override
    public boolean isSharable() {
        return oldManager.isSharable();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        oldManager.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        oldManager.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
        oldManager.exceptionCaught(channelhandlercontext, throwable);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet packet) {
        if (oldManager.channel.isOpen()) {
            try {
                packet.handle(this.packetListener);
            }
            catch (Exception e) {
                // Do nothing
            }
        }
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetlistener) {
        oldManager.setListenerForServerboundHandshake(packetlistener);
    }

    @Override
    public void initiateServerboundStatusConnection(String s, int i, ClientStatusPacketListener packetstatusoutlistener) {
        oldManager.initiateServerboundStatusConnection(s, i, packetstatusoutlistener);
    }

    @Override
    public void initiateServerboundPlayConnection(String s, int i, ClientLoginPacketListener packetloginoutlistener) {
        oldManager.initiateServerboundPlayConnection(s, i, packetloginoutlistener);
    }

    @Override
    public void send(Packet<?> packet) {
        send(packet, null);
    }

    public static void doPacketOutput(String text) {
        if (!NMSHandler.debugPackets) {
            return;
        }
        if (NMSHandler.debugPacketFilter == null || NMSHandler.debugPacketFilter.trim().isEmpty()
                || CoreUtilities.toLowerCase(text).contains(NMSHandler.debugPacketFilter)) {
            Debug.log(text);
        }
    }

    public void debugOutputPacket(Packet<?> packet) {
        if (packet instanceof ClientboundSetEntityDataPacket) {
            StringBuilder output = new StringBuilder(128);
            output.append("Packet: ClientboundSetEntityDataPacket sent to ").append(player.getScoreboardName()).append(" for entity ID: ").append(((ClientboundSetEntityDataPacket) packet).id()).append(": ");
            List<SynchedEntityData.DataValue<?>> list = ((ClientboundSetEntityDataPacket) packet).packedItems();
            if (list == null) {
                output.append("None");
            }
            else {
                for (SynchedEntityData.DataValue<?> data : list) {
                    output.append('[').append(data.id()).append(": ").append(data.value()).append("], ");
                }
            }
            doPacketOutput(output.toString());
        }
        else if (packet instanceof ClientboundSetEntityMotionPacket) {
            ClientboundSetEntityMotionPacket velPacket = (ClientboundSetEntityMotionPacket) packet;
            doPacketOutput("Packet: ClientboundSetEntityMotionPacket sent to " + player.getScoreboardName() + " for entity ID: " + velPacket.getId() + ": " + velPacket.getXa() + "," + velPacket.getYa() + "," + velPacket.getZa());
        }
        else if (packet instanceof ClientboundAddEntityPacket) {
            ClientboundAddEntityPacket addEntityPacket = (ClientboundAddEntityPacket) packet;
            doPacketOutput("Packet: ClientboundAddEntityPacket sent to " + player.getScoreboardName() + " for entity ID: " + addEntityPacket.getId() + ": " + "uuid: " + addEntityPacket.getUUID()
                    + ", type: " + addEntityPacket.getType() + ", at: " + addEntityPacket.getX() + "," + addEntityPacket.getY() + "," + addEntityPacket.getZ() + ", data: " + addEntityPacket.getData());
        }
        else if (packet instanceof ClientboundMapItemDataPacket) {
            ClientboundMapItemDataPacket mapPacket = (ClientboundMapItemDataPacket) packet;
            doPacketOutput("Packet: ClientboundMapItemDataPacket sent to " + player.getScoreboardName() + " for map ID: " + mapPacket.mapId() + ", scale: " + mapPacket.scale() + ", locked: " + mapPacket.locked());
        }
        else if (packet instanceof ClientboundRemoveEntitiesPacket) {
            ClientboundRemoveEntitiesPacket removePacket = (ClientboundRemoveEntitiesPacket) packet;
            doPacketOutput("Packet: ClientboundRemoveEntitiesPacket sent to " + player.getScoreboardName() + " for entities: " + removePacket.getEntityIds().stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        else if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
            ClientboundPlayerInfoUpdatePacket playerInfoPacket = (ClientboundPlayerInfoUpdatePacket) packet;
            doPacketOutput("Packet: ClientboundPlayerInfoPacket sent to " + player.getScoreboardName() + " of types " + playerInfoPacket.actions() + " for player profiles: " +
                    playerInfoPacket.entries().stream().map(p -> "mode=" + p.gameMode() + "/latency=" + p.latency() + "/display=" + p.displayName() + "/name=" + p.profile().getName() + "/id=" + p.profile().getId() + "/"
                            + p.profile().getProperties().asMap().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().stream().map(v -> v.value() + ";" + v.signature()).collect(Collectors.joining(";;;"))).collect(Collectors.joining("/"))).collect(Collectors.joining(", ")));
        }
        else {
            doPacketOutput("Packet: " + packet.getClass().getCanonicalName() + " sent to " + player.getScoreboardName());
        }
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener genericfuturelistener) {
        send(packet, genericfuturelistener, true);
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketSendListener genericfuturelistener, boolean flush) {
        if (!Bukkit.isPrimaryThread()) {
            if (Settings.cache_warnOnAsyncPackets
                    && !(packet instanceof ClientboundSystemChatPacket) && !(packet instanceof ClientboundPlayerChatPacket) // Vanilla supports an async chat system, though it's normally disabled, some plugins use this as justification for sending messages async
                    && !(packet instanceof ClientboundCommandSuggestionsPacket)) { // Async tab complete is wholly unsupported in Spigot (and will cause an exception), however Paper explicitly adds async support (for unclear reasons), so let it through too
                Debug.echoError("Warning: packet sent off main thread! This is completely unsupported behavior! Denizen network interceptor ignoring packet to avoid crash. Packet class: "
                        + packet.getClass().getCanonicalName() + " sent to " + player.getScoreboardName() + " identify the sender of the packet from the stack trace:");
                try {
                    throw new RuntimeException("Trace");
                }
                catch (Exception ex) {
                    Debug.echoError(ex);
                }
            }
            oldManager.send(packet, genericfuturelistener, flush);
            return;
        }
        if (NMSHandler.debugPackets) {
            debugOutputPacket(packet);
        }
        packetsSent++;
        if (packet instanceof ClientboundBundlePacket bundlePacket) {
            List<Packet<? super ClientGamePacketListener>> processedPackets = new ArrayList<>();
            boolean anyChange = false;
            for (Packet<? super ClientGamePacketListener> _subPacket : bundlePacket.subPackets()) {
                // Bundle packets with non-game packets shouldn't ever be sent while the Denizen interceptor is active
                Packet<ClientGamePacketListener> subPacket = (Packet<ClientGamePacketListener>) _subPacket;
                Packet<ClientGamePacketListener> processed = processPacketHandlersFor(subPacket);
                anyChange = anyChange || processed != subPacket;
                if (processed != null) {
                    processedPackets.add(processed);
                }
            }
            if (processedPackets.isEmpty()) {
                return;
            }
            if (anyChange) {
                packet = new ClientboundBundlePacket(processedPackets);
            }
        }
        else {
            Packet<?> processed = processPacketHandlersFor((Packet<ClientGamePacketListener>) packet);
            if (processed == null) {
                return;
            }
            packet = processed;
        }
        oldManager.send(packet, genericfuturelistener, flush);
    }

    @Override
    public void runOnceConnected(Consumer<Connection> consumer) {
        oldManager.runOnceConnected(consumer);
    }

    @Override
    public void flushChannel() {
        oldManager.flushChannel();
    }

    public Packet<ClientGamePacketListener> processPacketHandlersFor(Packet<ClientGamePacketListener> packet) {
        if (packet == null) {
            return null;
        }
        List<PacketHandler<?>> packetHandlers = DenizenNetworkManagerImpl.packetHandlers.get(packet.getClass());
        if (packetHandlers != null) {
            for (PacketHandler<?> _packetHandler : packetHandlers) {
                PacketHandler<Packet<ClientGamePacketListener>> packetHandler = (PacketHandler<Packet<ClientGamePacketListener>>) _packetHandler;
                Packet<ClientGamePacketListener> processed;
                try {
                    processed = packetHandler.handlePacket(this, packet);
                }
                catch (Exception ex) {
                    Debug.echoError("Packet handler for " + packet.getClass().getCanonicalName() + " threw an exception:");
                    Debug.echoError(ex);
                    continue;
                }
                if (processed == null) {
                    if (NMSHandler.debugPackets) {
                        doPacketOutput("DENIED PACKET - " + packet.getClass().getCanonicalName() + " DENIED FROM SEND TO " + player.getScoreboardName());
                    }
                    return null;
                }
                packet = processed;
            }
        }
        if (PlayerReceivesPacketScriptEvent.instance.eventData.isEnabled & PlayerReceivesPacketScriptEvent.fireFor(player.getBukkitEntity(), packet)) {
            if (NMSHandler.debugPackets) {
                doPacketOutput("DENIED PACKET - " + packet.getClass().getCanonicalName() + " DENIED FROM SEND TO " + player.getScoreboardName() + " due to event");
            }
            return null;
        }
        return packet;
    }

    static {
        ActionBarEventPacketHandlers.registerHandlers();
        AttachPacketHandlers.registerHandlers();
        BlockLightPacketHandlers.registerHandlers();
        DenizenPacketHandlerPacketHandlers.registerHandlers();
        EntityMetadataPacketHandlers.registerHandlers();
        DisguisePacketHandlers.registerHandlers();
        FakeBlocksPacketHandlers.registerHandlers();
        FakeEquipmentPacketHandlers.registerHandlers();
        FakePlayerPacketHandlers.registerHandlers();
        HiddenEntitiesPacketHandlers.registerHandlers();
        HideParticlesPacketHandlers.registerHandlers();
        PlayerHearsSoundEventPacketHandlers.registerHandlers();
        ProfileEditorImpl.registerHandlers();
        TablistUpdateEventPacketHandlers.registerHandlers();
    }

    @Override
    public void tick() {
        oldManager.tick();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return oldManager.getRemoteAddress();
    }

    @Override
    public String getLoggableAddress(boolean flag) {
        return oldManager.getLoggableAddress(flag);
    }

    @Override
    public void disconnect(Component ichatbasecomponent) {
        if (!player.getBukkitEntity().isOnline()) { // Workaround Paper duplicate quit event issue
            return;
        }
        oldManager.disconnect(ichatbasecomponent);
    }

    @Override
    public boolean isMemoryConnection() {
        return oldManager != null && oldManager.isMemoryConnection();
    }

    @Override
    public PacketFlow getReceiving() {
        return oldManager.getReceiving();
    }

    @Override
    public PacketFlow getSending() {
        return oldManager.getSending();
    }

    @Override
    public void configurePacketHandler(ChannelPipeline channelpipeline) {
        oldManager.configurePacketHandler(channelpipeline);
    }

    @Override
    public void setEncryptionKey(Cipher cipher, Cipher cipher1) {
        oldManager.setEncryptionKey(cipher, cipher1);
    }

    @Override
    public boolean isEncrypted() {
        return oldManager.isEncrypted();
    }

    @Override
    public boolean isConnected() {
        return oldManager.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return oldManager.isConnecting();
    }

    @Override
    public PacketListener getPacketListener() {
        return oldManager.getPacketListener();
    }

    @Override
    public Component getDisconnectedReason() {
        return oldManager.getDisconnectedReason();
    }

    @Override
    public void setReadOnly() {
        oldManager.setReadOnly();
    }

    @Override
    public void setupCompression(int i, boolean b) {
        oldManager.setupCompression(i, b);
    }

    @Override
    public void handleDisconnection() {
        oldManager.handleDisconnection();
    }

    @Override
    public float getAverageReceivedPackets() {
        return oldManager.getAverageReceivedPackets();
    }

    @Override
    public float getAverageSentPackets() {
        return oldManager.getAverageSentPackets();
    }

    //////////////////////////////////
    //// Reflection Methods/Fields
    ///////////

    private static final Field protocolDirectionField = ReflectionHelper.getFields(Connection.class).get(ReflectionMappingsInfo.Connection_receiving, PacketFlow.class);
    public static final Field Connection_packetListener = ReflectionHelper.getFields(Connection.class).get(ReflectionMappingsInfo.Connection_packetListener, PacketListener.class);
    private static final Field ServerGamePacketListener_ConnectionField = ReflectionHelper.getFields(ServerCommonPacketListenerImpl.class).get(ReflectionMappingsInfo.ServerCommonPacketListenerImpl_connection);
    private static final MethodHandle ServerGamePacketListener_ConnectionSetter = ReflectionHelper.getFinalSetter(ServerCommonPacketListenerImpl.class, ReflectionMappingsInfo.ServerCommonPacketListenerImpl_connection);

    private static PacketFlow getProtocolDirection(Connection networkManager) {
        PacketFlow direction = null;
        try {
            direction = (PacketFlow) protocolDirectionField.get(networkManager);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        return direction;
    }

    private static void setNetworkManager(ServerGamePacketListenerImpl playerConnection, Connection networkManager) {
        try {
            ServerGamePacketListener_ConnectionSetter.invoke(playerConnection, networkManager);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return oldManager.acceptInboundMessage(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        oldManager.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        oldManager.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        oldManager.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        oldManager.channelWritabilityChanged(ctx);
    }
}
