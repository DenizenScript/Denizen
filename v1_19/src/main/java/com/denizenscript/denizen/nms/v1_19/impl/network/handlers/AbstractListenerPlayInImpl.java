package com.denizenscript.denizen.nms.v1_19.impl.network.handlers;

import com.denizenscript.denizen.events.player.PlayerSendPacketScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.Set;

public class AbstractListenerPlayInImpl extends ServerGamePacketListenerImpl {

    public final ServerGamePacketListenerImpl oldListener;
    public final DenizenNetworkManagerImpl denizenNetworkManager;

    public AbstractListenerPlayInImpl(DenizenNetworkManagerImpl networkManager, ServerPlayer entityPlayer, ServerGamePacketListenerImpl oldListener) {
        super(MinecraftServer.getServer(), networkManager, entityPlayer);
        this.oldListener = oldListener;
        this.denizenNetworkManager = networkManager;
    }

    @Override
    public void disconnect(Component ichatbasecomponent) {
        oldListener.disconnect(ichatbasecomponent);
    }

    @Override
    public void disconnect(String s) {
        oldListener.disconnect(s);
    }

    @Override
    public void teleport(double d0, double d1, double d2, float f, float f1) {
        oldListener.teleport(d0, d1, d2, f, f1);
    }

    @Override
    public void teleport(double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
        oldListener.teleport(d0, d1, d2, f, f1, cause);
    }

    @Override
    public void teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set) {
        oldListener.teleport(d0, d1, d2, f, f1, set);
    }

    @Override
    public boolean teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set, PlayerTeleportEvent.TeleportCause cause) {
        return oldListener.teleport(d0, d1, d2, f, f1, set, cause);
    }

    @Override
    public void teleport(Location dest) {
        oldListener.teleport(dest);
    }

    @Override
    public CraftPlayer getCraftPlayer() {
        return oldListener.getCraftPlayer();
    }

    @Override
    public void tick() {
        oldListener.tick();
    }

    @Override
    public void resetPosition() {
        oldListener.resetPosition();
    }

    @Override
    public boolean isAcceptingMessages() {
        return oldListener.isAcceptingMessages();
    }

    @Override
    public void onDisconnect(Component ichatbasecomponent) {
        oldListener.onDisconnect(ichatbasecomponent);
    }

    @Override
    public void ackBlockChangesUpTo(int i) {
        oldListener.ackBlockChangesUpTo(i);
    }

    @Override
    public void send(Packet<?> packet) {
        oldListener.send(packet);
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener listener) {
        oldListener.send(packet, listener);
    }

    public static Field AWAITING_POS_FIELD = ReflectionHelper.getFields(ServerGamePacketListenerImpl.class).get(ReflectionMappingsInfo.ServerGamePacketListenerImpl_awaitingPositionFromClient, Vec3.class);
    public static Field AWAITING_TELEPORT_FIELD = ReflectionHelper.getFields(ServerGamePacketListenerImpl.class).get(ReflectionMappingsInfo.ServerGamePacketListenerImpl_awaitingTeleport, int.class);

    public void debugPacketOutput(Packet<ServerGamePacketListener> packet) {
        try {
            if (packet instanceof ServerboundMovePlayerPacket) {
                ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket) packet;
                DenizenNetworkManagerImpl.doPacketOutput("Packet ServerboundMovePlayerPacket sent from " + player.getScoreboardName() + " with XYZ="
                        + movePacket.x + ", " + movePacket.y + ", " + movePacket.z + ", yRot=" + movePacket.yRot + ", xRot=" + movePacket.xRot
                        + ", onGround=" + movePacket.isOnGround() + ", hasPos=" + movePacket.hasPos + ", hasRot=" + movePacket.hasRot);
            }
            else if (packet instanceof ServerboundAcceptTeleportationPacket) {
                Vec3 awaitPos = (Vec3) AWAITING_POS_FIELD.get(oldListener);
                int awaitTeleportId = AWAITING_TELEPORT_FIELD.getInt(oldListener);
                ServerboundAcceptTeleportationPacket acceptPacket = (ServerboundAcceptTeleportationPacket) packet;
                DenizenNetworkManagerImpl.doPacketOutput("Packet ServerboundAcceptTeleportationPacket sent from " + player.getScoreboardName()
                        + " with ID=" + acceptPacket.getId() + ", awaitingTeleport=" + awaitTeleportId + ", awaitPos=" + awaitPos);
            }
            else {
                DenizenNetworkManagerImpl.doPacketOutput("Packet: " + packet.getClass().getCanonicalName() + " sent from " + player.getScoreboardName());
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public boolean handlePacketIn(Packet<ServerGamePacketListener> packet) {
        denizenNetworkManager.packetsReceived++;
        if (NMSHandler.debugPackets) {
            debugPacketOutput(packet);
        }
        if (PlayerSendPacketScriptEvent.instance.eventData.isEnabled) {
            if (PlayerSendPacketScriptEvent.fireFor(player.getBukkitEntity(), packet)) {
                if (NMSHandler.debugPackets) {
                    DenizenNetworkManagerImpl.doPacketOutput("Denied packet-in " + packet.getClass().getCanonicalName() + " from " + player.getScoreboardName() + " due to event");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleChatAck(ServerboundChatAckPacket serverboundchatackpacket) {
        oldListener.handleChatAck(serverboundchatackpacket);
    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePlayerInput(packet);
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleMoveVehicle(packet);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleAcceptTeleportPacket(packet);
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleRecipeBookSeenRecipePacket(packet);
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleRecipeBookChangeSettingsPacket(packet);
    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSeenAdvancements(packet);
    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleCustomCommandSuggestions(packet);
    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetCommandBlock(packet);
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetCommandMinecart(packet);
    }

    @Override
    public void handlePickItem(ServerboundPickItemPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePickItem(packet);
    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleRenameItem(packet);
    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetBeaconPacket(packet);
    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetStructureBlock(packet);
    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetJigsawBlock(packet);
    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleJigsawGenerate(packet);
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSelectTrade(packet);
    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleEditBook(packet);
    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQuery packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleEntityTagQuery(packet);
    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleBlockEntityTagQuery(packet);
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleMovePlayer(packet);
    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePlayerAction(packet);
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleUseItemOn(packet);
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleUseItem(packet);
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleTeleportToEntityPacket(packet);
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleResourcePackResponse(packet);
    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePaddleBoat(packet);
    }

    @Override
    public void handlePong(ServerboundPongPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePong(packet);
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetCarriedItem(packet);
    }

    @Override
    public void handleChat(ServerboundChatPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleChat(packet);
    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleChatCommand(packet);
    }

    @Override
    public void chat(String s, PlayerChatMessage original, boolean async) {
        oldListener.chat(s, original, async);
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleAnimate(packet);
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePlayerCommand(packet);
    }

    @Override
    public void addPendingMessage(PlayerChatMessage playerchatmessage) {
        oldListener.addPendingMessage(playerchatmessage);
    }

    @Override
    public void sendPlayerChatMessage(PlayerChatMessage playerchatmessage, ChatType.Bound chatmessagetype_a) {
        oldListener.sendPlayerChatMessage(playerchatmessage, chatmessagetype_a);
    }

    @Override
    public void sendDisguisedChatMessage(Component ichatbasecomponent, ChatType.Bound chatmessagetype_a) {
        oldListener.sendDisguisedChatMessage(ichatbasecomponent, chatmessagetype_a);
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return oldListener.getRemoteAddress();
    }

    @Override
    public SocketAddress getRawAddress() {
        return oldListener.getRawAddress();
    }

    @Override
    public void handleInteract(ServerboundInteractPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleInteract(packet);
    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleClientCommand(packet);
    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleContainerClose(packet);
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleContainerClick(packet);
    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePlaceRecipe(packet);
    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleContainerButtonClick(packet);
    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSetCreativeModeSlot(packet);
    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleSignUpdate(packet);
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleKeepAlive(packet);
    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handlePlayerAbilities(packet);
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleClientInformation(packet);
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleCustomPayload(packet);
    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleChangeDifficulty(packet);
    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {
        if (handlePacketIn(packet)) { return; }
        oldListener.handleLockDifficulty(packet);
    }

    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundchatsessionupdatepacket) {
        oldListener.handleChatSessionUpdate(serverboundchatsessionupdatepacket);
    }

    @Override
    public ServerPlayer getPlayer() {
        return oldListener.getPlayer();
    }

    @Override
    public boolean shouldPropagateHandlingExceptions() {
        return oldListener.shouldPropagateHandlingExceptions();
    }
}
