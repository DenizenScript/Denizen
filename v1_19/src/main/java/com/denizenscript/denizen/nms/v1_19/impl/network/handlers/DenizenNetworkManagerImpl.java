package com.denizenscript.denizen.nms.v1_19.impl.network.handlers;

import com.denizenscript.denizen.events.player.*;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.v1_19.Handler;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_19.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.packets.*;
import com.denizenscript.denizen.nms.v1_19.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_19.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.denizenscript.denizen.scripts.commands.entity.InvisibleCommand;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.scripts.commands.entity.SneakCommand;
import com.denizenscript.denizen.scripts.commands.player.DisguiseCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizen.utilities.entity.HideEntitiesHelper;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizen.utilities.packets.HideParticles;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptCodeGen;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.crypto.Cipher;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class DenizenNetworkManagerImpl extends Connection {

    public static FriendlyByteBuf copyPacket(Packet<?> original) {
        try {
            FriendlyByteBuf copier = new FriendlyByteBuf(Unpooled.buffer());
            original.write(copier);
            return copier;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public final Connection oldManager;
    public final DenizenPacketListenerImpl packetListener;
    public final ServerPlayer player;
    public int packetsSent, packetsReceived;

    public DenizenNetworkManagerImpl(ServerPlayer entityPlayer, Connection oldManager) {
        super(getProtocolDirection(oldManager));
        this.oldManager = oldManager;
        this.channel = oldManager.channel;
        this.packetListener = (DenizenPacketListenerImpl) NetworkInterceptCodeGen.generateAppropriateInterceptor(this, entityPlayer, DenizenPacketListenerImpl.class, AbstractListenerPlayInImpl.class, ServerGamePacketListenerImpl.class);
        oldManager.setListener(packetListener);
        this.player = this.packetListener.player;
    }

    public static void setNetworkManager(Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl playerConnection = entityPlayer.connection;
        setNetworkManager(playerConnection, new DenizenNetworkManagerImpl(entityPlayer, playerConnection.connection));
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
    public void setProtocol(ConnectionProtocol enumprotocol) {
        oldManager.setProtocol(enumprotocol);
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
    public void setListener(PacketListener packetlistener) {
        oldManager.setListener(packetlistener);
    }

    public static Field ENTITY_ID_PACKVELENT = ReflectionHelper.getFields(ClientboundSetEntityMotionPacket.class).get(ReflectionMappingsInfo.ClientboundSetEntityMotionPacket_id, int.class);
    public static Field ENTITY_ID_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_id, int.class);
    public static Field POS_X_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_x, double.class);
    public static Field POS_Y_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_y, double.class);
    public static Field POS_Z_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_z, double.class);
    public static Field YAW_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_yRot, byte.class);
    public static Field PITCH_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_xRot, byte.class);
    public static Field POS_X_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_xa, short.class);
    public static Field POS_Y_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_ya, short.class);
    public static Field POS_Z_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_za, short.class);
    public static Field YAW_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_yRot, byte.class);
    public static Field PITCH_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_xRot, byte.class);
    public static Field SECTIONPOS_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_sectionPos, SectionPos.class);
    public static Field OFFSETARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_positions, short[].class);
    public static Field BLOCKARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_states, BlockState[].class);
    public static Field ENTITY_METADATA_LIST = ReflectionHelper.getFields(ClientboundSetEntityDataPacket.class).get(ReflectionMappingsInfo.ClientboundSetEntityDataPacket_packedItems, List.class);

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
            output.append("Packet: ClientboundSetEntityDataPacket sent to ").append(player.getScoreboardName()).append(" for entity ID: ").append(((ClientboundSetEntityDataPacket) packet).getId()).append(": ");
            List<SynchedEntityData.DataItem<?>> list = ((ClientboundSetEntityDataPacket) packet).getUnpackedData();
            if (list == null) {
                output.append("None");
            }
            else {
                for (SynchedEntityData.DataItem<?> data : list) {
                    output.append('[').append(data.getAccessor().getId()).append(": ").append(data.getValue()).append("], ");
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
            doPacketOutput("Packet: ClientboundMapItemDataPacket sent to " + player.getScoreboardName() + " for map ID: " + mapPacket.getMapId() + ", scale: " + mapPacket.getScale() + ", locked: " + mapPacket.isLocked());
        }
        else if (packet instanceof ClientboundRemoveEntitiesPacket) {
            ClientboundRemoveEntitiesPacket removePacket = (ClientboundRemoveEntitiesPacket) packet;
            doPacketOutput("Packet: ClientboundRemoveEntitiesPacket sent to " + player.getScoreboardName() + " for entities: " + removePacket.getEntityIds().stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        else if (packet instanceof ClientboundPlayerInfoPacket) {
            ClientboundPlayerInfoPacket playerInfoPacket = (ClientboundPlayerInfoPacket) packet;
            doPacketOutput("Packet: ClientboundPlayerInfoPacket sent to " + player.getScoreboardName() + " of type " + playerInfoPacket.getAction() + " for player profiles: " +
                    playerInfoPacket.getEntries().stream().map(p -> "mode=" + p.getGameMode() + "/latency=" + p.getLatency() + "/display=" + p.getDisplayName() + "/name=" + p.getProfile().getName() + "/id=" + p.getProfile().getId() + "/"
                            + p.getProfile().getProperties().asMap().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().stream().map(v -> v.getValue() + ";" + v.getSignature()).collect(Collectors.joining(";;;"))).collect(Collectors.joining("/"))).collect(Collectors.joining(", ")));
        }
        else {
            doPacketOutput("Packet: " + packet.getClass().getCanonicalName() + " sent to " + player.getScoreboardName());
        }
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener genericfuturelistener) {
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
            oldManager.send(packet, genericfuturelistener);
            return;
        }
        if (NMSHandler.debugPackets) {
            debugOutputPacket(packet);
        }
        packetsSent++;
        if (processAttachToForPacket(packet)
            || processHiddenEntitiesForPacket(packet)
            || processMirrorForPacket(packet)
            || processParticlesForPacket(packet)
            || processSoundPacket(packet)
            || processPacketHandlerForPacket(packet, genericfuturelistener)
            || processTablistPacket(packet, genericfuturelistener)
            || processActionbarPacket(packet, genericfuturelistener)
            || processDisguiseForPacket(packet, genericfuturelistener)
            || processMetadataChangesForPacket(packet, genericfuturelistener)
            || processEquipmentForPacket(packet, genericfuturelistener)
            || processShowFakeForPacket(packet, genericfuturelistener)) {
            if (NMSHandler.debugPackets) {
                doPacketOutput("DENIED PACKET " + packet.getClass().getCanonicalName() + " DENIED FROM SEND TO " + player.getScoreboardName());
            }
            return;
        }
        if (PlayerReceivesPacketScriptEvent.enabled) {
            if (PlayerReceivesPacketScriptEvent.fireFor(player.getBukkitEntity(), packet)) {
                if (NMSHandler.debugPackets) {
                    doPacketOutput("DENIED PACKET " + packet.getClass().getCanonicalName() + " DENIED FROM SEND TO " + player.getScoreboardName() + " due to event");
                }
                return;
            }
        }
        processBlockLightForPacket(packet);
        processFakePlayerSpawnForPacket(packet);
        oldManager.send(packet, genericfuturelistener);
    }

    public boolean processTablistPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (!PlayerReceivesTablistUpdateScriptEvent.enabled) {
            return false;
        }
        if (packet instanceof ClientboundPlayerInfoPacket) {
            ClientboundPlayerInfoPacket infoPacket = (ClientboundPlayerInfoPacket) packet;
            String mode;
            switch (infoPacket.getAction()) {
                case ADD_PLAYER:
                    mode = "add";
                    break;
                case REMOVE_PLAYER:
                    mode = "remove";
                    break;
                case UPDATE_LATENCY:
                    mode = "update_latency";
                    break;
                case UPDATE_GAME_MODE:
                    mode = "update_gamemode";
                    break;
                case UPDATE_DISPLAY_NAME:
                    mode = "update_display";
                    break;
                default:
                    return false;
            }
            boolean isOverriding = false;
            for (ClientboundPlayerInfoPacket.PlayerUpdate update : infoPacket.getEntries()) {
                GameProfile profile = update.getProfile();
                String texture = null, signature = null;
                if (profile.getProperties().containsKey("textures")) {
                    Property property = profile.getProperties().get("textures").stream().findFirst().get();
                    texture = property.getValue();
                    signature = property.getSignature();
                }
                String modeText = update.getGameMode() == null ? null : update.getGameMode().name();
                PlayerReceivesTablistUpdateScriptEvent.TabPacketData data = new PlayerReceivesTablistUpdateScriptEvent.TabPacketData(mode, profile.getId(), profile.getName(),
                        update.getDisplayName() == null ? null : FormattedTextHelper.stringify(Handler.componentToSpigot(update.getDisplayName())), modeText, texture, signature, update.getLatency());
                PlayerReceivesTablistUpdateScriptEvent.fire(player.getBukkitEntity(), data);
                if (data.modified) {
                    if (!isOverriding) {
                        isOverriding = true;
                        ClientboundPlayerInfoPacket priorsPacket = new ClientboundPlayerInfoPacket(infoPacket.getAction());
                        for (ClientboundPlayerInfoPacket.PlayerUpdate priorUpdate : infoPacket.getEntries()) {
                            if (priorUpdate == update) {
                                break;
                            }
                            priorsPacket.getEntries().add(priorUpdate);
                        }
                        if (!priorsPacket.getEntries().isEmpty()) {
                            oldManager.send(priorsPacket, genericfuturelistener);
                        }
                    }
                    if (!data.cancelled) {
                        ClientboundPlayerInfoPacket newPacket = new ClientboundPlayerInfoPacket(infoPacket.getAction());
                        GameProfile newProfile = new GameProfile(data.id, data.name);
                        if (data.texture != null) {
                            newProfile.getProperties().put("textures", new Property("textures", data.texture, data.signature));
                        }
                        newPacket.getEntries().add(new ClientboundPlayerInfoPacket.PlayerUpdate(newProfile, data.latency, data.gamemode == null ? null : GameType.byName(CoreUtilities.toLowerCase(data.gamemode)),
                                data.display == null ? null : Handler.componentToNMS(FormattedTextHelper.parse(data.display, ChatColor.WHITE)), update.getProfilePublicKey()));
                        oldManager.send(newPacket, genericfuturelistener);
                    }
                }
                else if (isOverriding) {
                    ClientboundPlayerInfoPacket newPacket = new ClientboundPlayerInfoPacket(infoPacket.getAction());
                    newPacket.getEntries().add(update);
                    oldManager.send(newPacket, genericfuturelistener);
                }
            }
            return isOverriding;
        }
        return false;
    }

    public boolean processActionbarPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (!PlayerReceivesActionbarScriptEvent.instance.loaded) {
            return false;
        }
        if (packet instanceof ClientboundSetActionBarTextPacket) {
            ClientboundSetActionBarTextPacket actionbarPacket = (ClientboundSetActionBarTextPacket) packet;
            PlayerReceivesActionbarScriptEvent event = PlayerReceivesActionbarScriptEvent.instance;
            Component baseComponent = actionbarPacket.getText();
            event.reset();
            event.message = new ElementTag(FormattedTextHelper.stringify(Handler.componentToSpigot(baseComponent)));
            event.rawJson = new ElementTag(Component.Serializer.toJson(baseComponent));
            event.system = new ElementTag(false);
            event.player = PlayerTag.mirrorBukkitPlayer(player.getBukkitEntity());
            event = (PlayerReceivesActionbarScriptEvent) event.triggerNow();
            if (event.cancelled) {
                return true;
            }
            if (event.modified) {
                Component component = Handler.componentToNMS(event.altMessageDetermination);
                ClientboundSetActionBarTextPacket newPacket = new ClientboundSetActionBarTextPacket(component);
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
        }
        return false;
    }

    public boolean processSoundPacket(Packet<?> packet) {
        if (!PlayerHearsSoundScriptEvent.enabled) {
            return false;
        }
        if (packet instanceof ClientboundSoundPacket) {
            ClientboundSoundPacket spacket = (ClientboundSoundPacket) packet;
            return PlayerHearsSoundScriptEvent.instance.run(player.getBukkitEntity(), spacket.getSound().getLocation().getPath(), spacket.getSource().name(),
                    false, null, new Location(player.getBukkitEntity().getWorld(), spacket.getX(), spacket.getY(), spacket.getZ()), spacket.getVolume(), spacket.getPitch());
        }
        else if (packet instanceof ClientboundSoundEntityPacket) {
            ClientboundSoundEntityPacket spacket = (ClientboundSoundEntityPacket) packet;
            Entity entity = player.getLevel().getEntity(spacket.getId());
            if (entity == null) {
                return false;
            }
            return PlayerHearsSoundScriptEvent.instance.run(player.getBukkitEntity(), spacket.getSound().getLocation().getPath(), spacket.getSource().name(),
                    false, entity.getBukkitEntity(), null, spacket.getVolume(), spacket.getPitch());
        }
        else if (packet instanceof ClientboundCustomSoundPacket) {
            ClientboundCustomSoundPacket spacket = (ClientboundCustomSoundPacket) packet;
            return PlayerHearsSoundScriptEvent.instance.run(player.getBukkitEntity(), spacket.getName().toString(), spacket.getSource().name(),
                    true, null, new Location(player.getBukkitEntity().getWorld(), spacket.getX(), spacket.getY(), spacket.getZ()), spacket.getVolume(), spacket.getPitch());
        }
        return false;
    }

    public boolean processEquipmentForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof ClientboundSetEquipmentPacket) {
                int eid = ((ClientboundSetEquipmentPacket) packet).getEntity();
                Entity ent = player.level.getEntity(eid);
                if (ent == null) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(ent.getUUID(), player.getBukkitEntity());
                if (override == null) {
                    return false;
                }
                List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> equipment = new ArrayList<>(((ClientboundSetEquipmentPacket) packet).getSlots());
                ClientboundSetEquipmentPacket newPacket = new ClientboundSetEquipmentPacket(eid, equipment);
                for (int i = 0; i < equipment.size(); i++) {
                    Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack> pair =  equipment.get(i);
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
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundEntityEventPacket) {
                Entity ent = ((ClientboundEntityEventPacket) packet).getEntity(player.level);
                if (!(ent instanceof net.minecraft.world.entity.LivingEntity)) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(ent.getUUID(), player.getBukkitEntity());
                if (override == null || (override.hand == null && override.offhand == null)) {
                    return false;
                }
                if (((ClientboundEntityEventPacket) packet).getEventId() != (byte) 55) {
                    return false;
                }
                List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
                ItemStack hand = override.hand != null ? CraftItemStack.asNMSCopy(override.hand.getItemStack()) : ((net.minecraft.world.entity.LivingEntity) ent).getMainHandItem();
                ItemStack offhand = override.offhand != null ? CraftItemStack.asNMSCopy(override.offhand.getItemStack()) : ((net.minecraft.world.entity.LivingEntity) ent).getOffhandItem();
                equipment.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.MAINHAND, hand));
                equipment.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.OFFHAND, offhand));
                ClientboundSetEquipmentPacket newPacket = new ClientboundSetEquipmentPacket(ent.getId(), equipment);
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundContainerSetContentPacket) {
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
                if (override == null) {
                    return false;
                }
                int window = ((ClientboundContainerSetContentPacket) packet).getContainerId();
                if (window != 0) {
                    return false;
                }
                NonNullList<ItemStack> items = (NonNullList<ItemStack>) ((ClientboundContainerSetContentPacket) packet).getItems();
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
                    items.set(player.getInventory().selected + 36, CraftItemStack.asNMSCopy(override.hand.getItemStack()));
                }
                ClientboundContainerSetContentPacket newPacket = new ClientboundContainerSetContentPacket(window, ((ClientboundContainerSetContentPacket) packet).getStateId(), items, ((ClientboundContainerSetContentPacket) packet).getCarriedItem());
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundContainerSetSlotPacket) {
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
                if (override == null) {
                    return false;
                }
                int window = ((ClientboundContainerSetSlotPacket) packet).getContainerId();
                if (window != 0) {
                    return false;
                }
                int slot = ((ClientboundContainerSetSlotPacket) packet).getSlot();
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
                else if (slot == player.getInventory().selected + 36 && override.hand != null) {
                    item = override.hand.getItemStack();
                }
                if (item == null) {
                    return false;
                }
                ClientboundContainerSetSlotPacket newPacket = new ClientboundContainerSetSlotPacket(window, ((ClientboundContainerSetSlotPacket) packet).getStateId(), slot, CraftItemStack.asNMSCopy(item));
                oldManager.send(newPacket, genericfuturelistener);
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
            if (packet instanceof ClientboundLevelParticlesPacket) {
                HashSet<Particle> hidden = HideParticles.hidden.get(player.getUUID());
                if (hidden == null) {
                    return false;
                }
                ParticleOptions particle = ((ClientboundLevelParticlesPacket) packet).getParticle();
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

    public boolean processDisguiseForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (DisguiseCommand.disguises.isEmpty() || antiDuplicate) {
            return false;
        }
        try {
            int ider = -1;
            if (packet instanceof ClientboundSetEntityDataPacket) {
                ider = ((ClientboundSetEntityDataPacket) packet).getId();
            }
            if (packet instanceof ClientboundUpdateAttributesPacket) {
                ider = ((ClientboundUpdateAttributesPacket) packet).getEntityId();
            }
            if (packet instanceof ClientboundAddPlayerPacket) {
                ider = ((ClientboundAddPlayerPacket) packet).getEntityId();
            }
            else if (packet instanceof ClientboundAddEntityPacket) {
                ider = ((ClientboundAddEntityPacket) packet).getId();
            }
            if (ider != -1) {
                Entity e = player.getLevel().getEntity(ider);
                if (e == null) {
                    return false;
                }
                HashMap<UUID, DisguiseCommand.TrackedDisguise> playerMap = DisguiseCommand.disguises.get(e.getUUID());
                if (playerMap == null) {
                    return false;
                }
                DisguiseCommand.TrackedDisguise disguise = playerMap.get(player.getUUID());
                if (disguise == null) {
                    disguise = playerMap.get(null);
                    if (disguise == null) {
                        return false;
                    }
                }
                if (!disguise.isActive) {
                    return false;
                }
                if (NMSHandler.debugPackets) {
                    doPacketOutput("DISGUISED packet " + packet.getClass().getName() + " for entity " + ider + " to player " + player.getScoreboardName());
                }
                if (packet instanceof ClientboundSetEntityDataPacket) {
                    ClientboundSetEntityDataPacket metadataPacket = (ClientboundSetEntityDataPacket) packet;
                    if (e.getId() == player.getId()) {
                        if (!disguise.shouldFake) {
                            return false;
                        }
                        List<SynchedEntityData.DataItem<?>> data = metadataPacket.getUnpackedData();
                        for (SynchedEntityData.DataItem item : data) {
                            EntityDataAccessor<?> watcherObject = item.getAccessor();
                            int watcherId = watcherObject.getId();
                            if (watcherId == 0) { // Entity flags
                                ClientboundSetEntityDataPacket altPacket = new ClientboundSetEntityDataPacket(copyPacket(metadataPacket));
                                data = new ArrayList<>(data);
                                ENTITY_METADATA_LIST.set(altPacket, data);
                                data.remove(item);
                                byte flags = (byte) item.getValue();
                                flags |= 0x20; // Invisible flag
                                data.add(new SynchedEntityData.DataItem(watcherObject, flags));
                                ClientboundSetEntityDataPacket updatedPacket = getModifiedMetadataFor(altPacket);
                                oldManager.send(updatedPacket == null ? altPacket : updatedPacket, genericfuturelistener);
                                return true;
                            }
                        }
                    }
                    else {
                        ClientboundSetEntityDataPacket altPacket = new ClientboundSetEntityDataPacket(e.getId(), ((CraftEntity) disguise.toOthers.entity.entity).getHandle().getEntityData(), true);
                        oldManager.send(altPacket, genericfuturelistener);
                        return true;
                    }
                    return false;
                }
                if (packet instanceof ClientboundUpdateAttributesPacket) {
                    FakeEntity fake = ider == player.getId() ? disguise.fakeToSelf : disguise.toOthers;
                    if (fake == null) {
                        return false;
                    }
                    if (fake.entity.entity instanceof LivingEntity) {
                        return false;
                    }
                    return true; // Non-living don't have attributes
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

    public ClientboundSetEntityDataPacket getModifiedMetadataFor(ClientboundSetEntityDataPacket metadataPacket) {
        if (!RenameCommand.hasAnyDynamicRenames() && SneakCommand.forceSetSneak.isEmpty() && InvisibleCommand.invisibleEntities.isEmpty()) {
            return null;
        }
        try {
            int eid = metadataPacket.getId();
            Entity ent = player.level.getEntity(eid);
            if (ent == null) {
                return null; // If it doesn't exist on-server, it's definitely not relevant, so move on
            }
            String nameToApply = RenameCommand.getCustomNameFor(ent.getUUID(), player.getBukkitEntity(), false);
            Boolean forceSneak = SneakCommand.shouldSneak(ent.getUUID(), player.getUUID());
            Boolean isInvisible = InvisibleCommand.isInvisible(ent.getBukkitEntity(), player.getUUID(), true);
            if (nameToApply == null && forceSneak == null && isInvisible == null) {
                return null;
            }
            List<SynchedEntityData.DataItem<?>> data = new ArrayList<>(metadataPacket.getUnpackedData());
            boolean any = false;
            for (int i = 0; i < data.size(); i++) {
                SynchedEntityData.DataItem<?> item = data.get(i);
                EntityDataAccessor<?> watcherObject = item.getAccessor();
                int watcherId = watcherObject.getId();
                if (watcherId == 0 && (forceSneak != null || isInvisible != null)) { // 0: Entity flags
                    byte val = (Byte) item.getValue();
                    if (forceSneak != null) {
                        if (forceSneak) {
                            val |= 0x02; // 8: Crouching
                        }
                        else {
                            val &= ~0x02;
                        }
                    }
                    if (isInvisible != null) {
                        if (isInvisible) {
                            val |= 0x20;
                        }
                        else {
                            val &= ~0x20;
                        }
                    }
                    data.set(i, new SynchedEntityData.DataItem(watcherObject, val));
                    any = true;
                }
                else if (watcherId == 2 && nameToApply != null) { // 2: Custom name metadata
                    Optional<Component> name = Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(nameToApply, ChatColor.WHITE)));
                    data.set(i, new SynchedEntityData.DataItem(watcherObject, name));
                    any = true;
                }
                else if (watcherId == 3 && nameToApply != null) { // 3: custom name visible metadata
                    data.set(i, new SynchedEntityData.DataItem(watcherObject, true));
                    any = true;
                }
            }
            if (!any) {
                return null;
            }
            ClientboundSetEntityDataPacket altPacket = new ClientboundSetEntityDataPacket(copyPacket(metadataPacket));
            ENTITY_METADATA_LIST.set(altPacket, data);
            return altPacket;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public boolean processMetadataChangesForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (!(packet instanceof ClientboundSetEntityDataPacket)) {
            return false;
        }
        ClientboundSetEntityDataPacket altPacket = getModifiedMetadataFor((ClientboundSetEntityDataPacket) packet);
        if (altPacket == null) {
            return false;
        }
        oldManager.send(altPacket, genericfuturelistener);
        return true;
    }

    public void tryProcessMovePacketForAttach(ClientboundMoveEntityPacket packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    ClientboundMoveEntityPacket pNew;
                    int newId = att.attached.getBukkitEntity().getEntityId();
                    if (packet instanceof ClientboundMoveEntityPacket.Pos) {
                        pNew = new ClientboundMoveEntityPacket.Pos(newId, packet.getXa(), packet.getYa(), packet.getZa(), packet.isOnGround());
                    }
                    else if (packet instanceof ClientboundMoveEntityPacket.Rot) {
                        pNew = new ClientboundMoveEntityPacket.Rot(newId, packet.getyRot(), packet.getxRot(), packet.isOnGround());
                    }
                    else if (packet instanceof ClientboundMoveEntityPacket.PosRot) {
                        pNew = new ClientboundMoveEntityPacket.PosRot(newId, packet.getXa(), packet.getYa(), packet.getZa(), packet.getyRot(), packet.getxRot(), packet.isOnGround());
                    }
                    else {
                        if (CoreConfiguration.debugVerbose) {
                            Debug.echoError("Impossible move-entity packet class: " + packet.getClass().getCanonicalName());
                        }
                        return;
                    }
                    if (att.positionalOffset != null && (packet instanceof ClientboundMoveEntityPacket.Pos || packet instanceof ClientboundMoveEntityPacket.PosRot)) {
                        boolean isRotate = packet instanceof ClientboundMoveEntityPacket.PosRot;
                        byte yaw, pitch;
                        if (att.noRotate) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.getYRot());
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        else if (isRotate) {
                            yaw = packet.getyRot();
                            pitch = packet.getxRot();
                        }
                        else {
                            yaw = EntityAttachmentHelper.compressAngle(e.getYRot());
                            pitch = EntityAttachmentHelper.compressAngle(e.getXRot());
                        }
                        if (att.noPitch) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        byte newYaw = yaw;
                        if (isRotate) {
                            newYaw = EntityAttachmentHelper.adaptedCompressedAngle(newYaw, att.positionalOffset.getYaw());
                            pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                        }
                        Vector goalPosition = att.fixedForOffset(new Vector(e.getX(), e.getY(), e.getZ()), e.getYRot(), e.getXRot());
                        Vector oldPos = att.visiblePositions.get(player.getUUID());
                        boolean forceTele = false;
                        if (oldPos == null) {
                            oldPos = att.attached.getLocation().toVector();
                            forceTele = true;
                        }
                        Vector moveNeeded = goalPosition.clone().subtract(oldPos);
                        att.visiblePositions.put(player.getUUID(), goalPosition.clone());
                        int offX = (int) (moveNeeded.getX() * (32 * 128));
                        int offY = (int) (moveNeeded.getY() * (32 * 128));
                        int offZ = (int) (moveNeeded.getZ() * (32 * 128));
                        if (forceTele || offX < Short.MIN_VALUE || offX > Short.MAX_VALUE
                                || offY < Short.MIN_VALUE || offY > Short.MAX_VALUE
                                || offZ < Short.MIN_VALUE || offZ > Short.MAX_VALUE) {
                            ClientboundTeleportEntityPacket newTeleportPacket = new ClientboundTeleportEntityPacket(e);
                            ENTITY_ID_PACKTELENT.setInt(newTeleportPacket, att.attached.getBukkitEntity().getEntityId());
                            POS_X_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getX());
                            POS_Y_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getY());
                            POS_Z_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getZ());
                            YAW_PACKTELENT.setByte(newTeleportPacket, newYaw);
                            PITCH_PACKTELENT.setByte(newTeleportPacket, pitch);
                            if (NMSHandler.debugPackets) {
                                doPacketOutput("Attach Move-Tele Packet: " + newTeleportPacket.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getScoreboardName() + " with original yaw " + yaw + " adapted to " + newYaw);
                            }
                            oldManager.send(newTeleportPacket);
                        }
                        else {
                            POS_X_PACKENT.setShort(pNew, (short) Mth.clamp(offX, Short.MIN_VALUE, Short.MAX_VALUE));
                            POS_Y_PACKENT.setShort(pNew, (short) Mth.clamp(offY, Short.MIN_VALUE, Short.MAX_VALUE));
                            POS_Z_PACKENT.setShort(pNew, (short) Mth.clamp(offZ, Short.MIN_VALUE, Short.MAX_VALUE));
                            if (isRotate) {
                                YAW_PACKENT.setByte(pNew, yaw);
                                PITCH_PACKENT.setByte(pNew, pitch);
                            }
                            if (NMSHandler.debugPackets) {
                                doPacketOutput("Attach Move Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getScoreboardName() + " with original yaw " + yaw + " adapted to " + newYaw);
                            }
                            oldManager.send(pNew);
                        }
                    }
                    else {
                        if (NMSHandler.debugPackets) {
                            doPacketOutput("Attach Replica-Move Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getScoreboardName());
                        }
                        oldManager.send(pNew);
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

    public void tryProcessVelocityPacketForAttach(ClientboundSetEntityMotionPacket packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    ClientboundSetEntityMotionPacket pNew = new ClientboundSetEntityMotionPacket(copyPacket(packet));
                    ENTITY_ID_PACKVELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    if (NMSHandler.debugPackets) {
                        doPacketOutput("Attach Velocity Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + player.getScoreboardName());
                    }
                    oldManager.send(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessVelocityPacketForAttach(packet, ent);
            }
        }
    }

    public void tryProcessTeleportPacketForAttach(ClientboundTeleportEntityPacket packet, Entity e, Vector relative) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    ClientboundTeleportEntityPacket pNew = new ClientboundTeleportEntityPacket(copyPacket(packet));
                    ENTITY_ID_PACKTELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    Vector resultPos = new Vector(POS_X_PACKTELENT.getDouble(pNew), POS_Y_PACKTELENT.getDouble(pNew), POS_Z_PACKTELENT.getDouble(pNew)).add(relative);
                    if (att.positionalOffset != null) {
                        resultPos = att.fixedForOffset(resultPos, e.getYRot(), e.getXRot());
                        byte yaw, pitch;
                        if (att.noRotate) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.getYRot());
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        else {
                            yaw = packet.getyRot();
                            pitch = packet.getxRot();
                        }
                        if (att.noPitch) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        byte newYaw = EntityAttachmentHelper.adaptedCompressedAngle(yaw, att.positionalOffset.getYaw());
                        pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                        POS_X_PACKTELENT.setDouble(pNew, resultPos.getX());
                        POS_Y_PACKTELENT.setDouble(pNew, resultPos.getY());
                        POS_Z_PACKTELENT.setDouble(pNew, resultPos.getZ());
                        YAW_PACKTELENT.setByte(pNew, newYaw);
                        PITCH_PACKTELENT.setByte(pNew, pitch);
                        if (NMSHandler.debugPackets) {
                            doPacketOutput("Attach Teleport Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID()
                                    + " sent to " + player.getScoreboardName() + " with raw yaw " + yaw + " adapted to " + newYaw);
                        }
                    }
                    att.visiblePositions.put(player.getUUID(), resultPos.clone());
                    oldManager.send(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessTeleportPacketForAttach(packet, ent, new Vector(ent.getX() - e.getX(), ent.getY() - e.getY(), ent.getZ() - e.getZ()));
            }
        }
    }

    public static Vector VECTOR_ZERO = new Vector(0, 0, 0);

    public boolean processAttachToForPacket(Packet<?> packet) {
        if (EntityAttachmentHelper.toEntityToData.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof ClientboundMoveEntityPacket) {
                Entity e = ((ClientboundMoveEntityPacket) packet).getEntity(player.getLevel());
                if (e == null) {
                    return false;
                }
                if (!e.isPassenger()) {
                    tryProcessMovePacketForAttach((ClientboundMoveEntityPacket) packet, e);
                }
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUUID(), e.getUUID());
            }
            else if (packet instanceof ClientboundSetEntityMotionPacket) {
                int ider = ((ClientboundSetEntityMotionPacket) packet).getId();
                Entity e = player.getLevel().getEntity(ider);
                if (e == null) {
                    return false;
                }
                tryProcessVelocityPacketForAttach((ClientboundSetEntityMotionPacket) packet, e);
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUUID(), e.getUUID());
            }
            else if (packet instanceof ClientboundTeleportEntityPacket) {
                int ider = ((ClientboundTeleportEntityPacket) packet).getId();
                Entity e = player.getLevel().getEntity(ider);
                if (e == null) {
                    return false;
                }
                tryProcessTeleportPacketForAttach((ClientboundTeleportEntityPacket) packet, e, VECTOR_ZERO);
                return EntityAttachmentHelper.denyOriginalPacketSend(player.getUUID(), e.getUUID());
            }
            else if (packet instanceof ClientboundRemoveEntitiesPacket) {
                for (int id : ((ClientboundRemoveEntitiesPacket) packet).getEntityIds()) {
                    Entity e = player.getLevel().getEntity(id);
                    if (e != null) {
                        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
                        if (attList != null) {
                            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(player.getUUID());
                                if (attMap.attached.isValid() && att != null) {
                                    att.visiblePositions.remove(player.getUUID());
                                }
                            }
                        }
                    }
                }
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
            int ider = -1;
            Entity e = null;
            if (packet instanceof ClientboundAddPlayerPacket) {
                ider = ((ClientboundAddPlayerPacket) packet).getEntityId();
            }
            else if (packet instanceof ClientboundAddEntityPacket) {
                ider = ((ClientboundAddEntityPacket) packet).getId();
            }
            else if (packet instanceof ClientboundAddExperienceOrbPacket) {
                ider = ((ClientboundAddExperienceOrbPacket) packet).getId();
            }
            else if (packet instanceof ClientboundMoveEntityPacket) {
                e = ((ClientboundMoveEntityPacket) packet).getEntity(player.getLevel());
            }
            else if (packet instanceof ClientboundSetEntityDataPacket) {
                ider = ((ClientboundSetEntityDataPacket) packet).getId();
            }
            else if (packet instanceof ClientboundSetEntityMotionPacket) {
                ider = ((ClientboundSetEntityMotionPacket) packet).getId();
            }
            else if (packet instanceof ClientboundTeleportEntityPacket) {
                ider = ((ClientboundTeleportEntityPacket) packet).getId();
            }
            if (e == null && ider != -1) {
                e = player.getLevel().getEntity(ider);
            }
            if (e != null) {
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

    public void processFakePlayerSpawnForPacket(Packet<?> packet) {
        if (packet instanceof ClientboundAddPlayerPacket) {
            int id = ((ClientboundAddPlayerPacket) packet).getEntityId();
            if (id != -1) {
                Entity e = player.getLevel().getEntity(id);
                processFakePlayerSpawn(e);
            }
        }
    }

    public void processFakePlayerSpawn(Entity entity) {
        if (entity instanceof EntityFakePlayerImpl) {
            final EntityFakePlayerImpl fakePlayer = (EntityFakePlayerImpl) entity;
            send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, fakePlayer));
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(),
                    () -> send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, fakePlayer)), 5);
        }
    }

    public boolean processMirrorForPacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerInfoPacket) {
            ClientboundPlayerInfoPacket playerInfo = (ClientboundPlayerInfoPacket) packet;
            ProfileEditorImpl.updatePlayerProfiles(playerInfo);
            if (!ProfileEditorImpl.handleAlteredProfiles(playerInfo, this)) {
                return true;
            }
        }
        return false;
    }

    public boolean processPacketHandlerForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (DenizenPacketHandler.instance.shouldInterceptChatPacket()) {
            PacketOutChatImpl packetHelper = null;
            boolean isActionbar = false;
            if (packet instanceof ClientboundSystemChatPacket) {
                isActionbar = ((ClientboundSystemChatPacket) packet).overlay();
                if (((ClientboundSystemChatPacket) packet).content() == null) { // Makes no sense but this can be null in weird edge cases
                    return false;
                }
                packetHelper = new PacketOutChatImpl((ClientboundSystemChatPacket) packet);
            }
            else if (packet instanceof ClientboundPlayerChatPacket) {
                packetHelper = new PacketOutChatImpl((ClientboundPlayerChatPacket) packet);
            }
            if (packetHelper != null) {
                PlayerReceivesMessageScriptEvent result = DenizenPacketHandler.instance.sendPacket(player.getBukkitEntity(), packetHelper);
                if (result != null) {
                    if (result.cancelled) {
                        return true;
                    }
                    if (result.modified) {
                        oldManager.send(new ClientboundSystemChatPacket(result.altMessageDetermination, isActionbar), genericfuturelistener);
                        return true;
                    }
                }
            }
        }
        if (packet instanceof ClientboundSetEntityDataPacket && DenizenPacketHandler.instance.shouldInterceptMetadata()) {
            return DenizenPacketHandler.instance.sendPacket(player.getBukkitEntity(), new PacketOutEntityMetadataImpl((ClientboundSetEntityDataPacket) packet));
        }
        return false;
    }

    public boolean processShowFakeForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (FakeBlock.blocks.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof ClientboundLevelChunkWithLightPacket) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(player.getUUID());
                if (map == null) {
                    return false;
                }
                int chunkX = ((ClientboundLevelChunkWithLightPacket) packet).getX();
                int chunkZ = ((ClientboundLevelChunkWithLightPacket) packet).getZ();
                ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, player.getLevel().getWorld().getName());
                List<FakeBlock> blocks = FakeBlock.getFakeBlocksFor(player.getUUID(), chunkCoord);
                if (blocks == null || blocks.isEmpty()) {
                    return false;
                }
                ClientboundLevelChunkWithLightPacket newPacket = FakeBlockHelper.handleMapChunkPacket(player.getBukkitEntity().getWorld(), (ClientboundLevelChunkWithLightPacket) packet, chunkX, chunkZ, blocks);
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(player.getUUID());
                if (map == null) {
                    return false;
                }
                SectionPos coord = (SectionPos) SECTIONPOS_MULTIBLOCKCHANGE.get(packet);
                ChunkCoordinate coordinateDenizen = new ChunkCoordinate(coord.getX(), coord.getZ(), player.getLevel().getWorld().getName());
                if (!map.byChunk.containsKey(coordinateDenizen)) {
                    return false;
                }
                ClientboundSectionBlocksUpdatePacket newPacket = new ClientboundSectionBlocksUpdatePacket(copyPacket(packet));
                LocationTag location = new LocationTag(player.getLevel().getWorld(), 0, 0, 0);
                short[] originalOffsetArray = (short[])OFFSETARRAY_MULTIBLOCKCHANGE.get(newPacket);
                BlockState[] originalDataArray = (BlockState[])BLOCKARRAY_MULTIBLOCKCHANGE.get(newPacket);
                short[] offsetArray = Arrays.copyOf(originalOffsetArray, originalOffsetArray.length);
                BlockState[] dataArray = Arrays.copyOf(originalDataArray, originalDataArray.length);
                OFFSETARRAY_MULTIBLOCKCHANGE.set(newPacket, offsetArray);
                BLOCKARRAY_MULTIBLOCKCHANGE.set(newPacket, dataArray);
                for (int i = 0; i < offsetArray.length; i++) {
                    short offset = offsetArray[i];
                    BlockPos pos = coord.relativeToBlockPos(offset);
                    location.setX(pos.getX());
                    location.setY(pos.getY());
                    location.setZ(pos.getZ());
                    FakeBlock block = map.byLocation.get(location);
                    if (block != null) {
                        dataArray[i] = FakeBlockHelper.getNMSState(block);
                    }
                }
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundBlockUpdatePacket) {
                BlockPos pos = ((ClientboundBlockUpdatePacket) packet).getPos();
                LocationTag loc = new LocationTag(player.getLevel().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUUID(), loc);
                if (block != null) {
                    ClientboundBlockUpdatePacket newPacket = new ClientboundBlockUpdatePacket(((ClientboundBlockUpdatePacket) packet).getPos(), FakeBlockHelper.getNMSState(block));
                    oldManager.send(newPacket, genericfuturelistener);
                    return true;
                }
            }
            else if (packet instanceof ClientboundBlockChangedAckPacket) {
                // TODO: 1.19: Can no longer determine what block this packet is for. Would have to track separately? Possibly from the inbound packet rather than the outbound one.
                /*
                ClientboundBlockChangedAckPacket origPack = (ClientboundBlockChangedAckPacket) packet;
                BlockPos pos = origPack.pos();
                LocationTag loc = new LocationTag(player.getLevel().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUUID(), loc);
                if (block != null) {
                    ClientboundBlockChangedAckPacket newPacket = new ClientboundBlockChangedAckPacket(origPack.pos(), FakeBlockHelper.getNMSState(block), origPack.action(), false);
                    oldManager.send(newPacket, genericfuturelistener);
                    return true;
                }*/
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return false;
    }

    public void processBlockLightForPacket(Packet<?> packet) {
        if (BlockLight.lightsByChunk.isEmpty()) {
            return;
        }
        if (packet instanceof ClientboundLightUpdatePacket) {
            BlockLightImpl.checkIfLightsBrokenByPacket((ClientboundLightUpdatePacket) packet, player.level);
        }
        else if (packet instanceof ClientboundBlockUpdatePacket) {
            BlockLightImpl.checkIfLightsBrokenByPacket((ClientboundBlockUpdatePacket) packet, player.level);
        }
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
    public void disconnect(Component ichatbasecomponent) {
        if (!player.getBukkitEntity().isOnline()) { // Workaround Paper duplicate quit event issue
            return;
        }
        oldManager.disconnect(ichatbasecomponent);
    }

    @Override
    public boolean isMemoryConnection() {
        return oldManager.isMemoryConnection();
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

    @Override
    public SocketAddress getRawAddress() {
        return oldManager.getRawAddress();
    }

    //////////////////////////////////
    //// Reflection Methods/Fields
    ///////////

    private static final Field protocolDirectionField = ReflectionHelper.getFields(Connection.class).get(ReflectionMappingsInfo.Connection_receiving, PacketFlow.class);
    private static final MethodHandle networkManagerField = ReflectionHelper.getFinalSetter(ServerGamePacketListenerImpl.class, ReflectionMappingsInfo.ServerGamePacketListenerImpl_connection);

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
            networkManagerField.invoke(playerConnection, networkManager);
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
