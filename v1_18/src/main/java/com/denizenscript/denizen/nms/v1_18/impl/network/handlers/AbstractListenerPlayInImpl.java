package com.denizenscript.denizen.nms.v1_18.impl.network.handlers;

import com.denizenscript.denizen.nms.NMSHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;

public class AbstractListenerPlayInImpl extends ServerGamePacketListenerImpl {

    public final ServerGamePacketListenerImpl oldListener;
    public final DenizenNetworkManagerImpl denizenNetworkManager;

    public AbstractListenerPlayInImpl(DenizenNetworkManagerImpl networkManager, ServerPlayer entityPlayer, ServerGamePacketListenerImpl oldListener) {
        super(MinecraftServer.getServer(), networkManager, entityPlayer);
        this.oldListener = oldListener;
        this.denizenNetworkManager = networkManager;
    }

    /*
    @Override
    public CraftPlayer getPlayer() {
        return oldListener.getPlayer();
    }*/

    @Override
    public Connection getConnection() {
        return this.connection;
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
    public void dismount(double d0, double d1, double d2, float f, float f1) {
        oldListener.dismount(d0, d1, d2, f, f1);
    }

    @Override
    public void dismount(double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
        oldListener.dismount(d0, d1, d2, f, f1, cause);
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
    public void teleport(double d0, double d1, double d2, float f, float f1, Set<ClientboundPlayerPositionPacket.RelativeArgument> set) {
        oldListener.teleport(d0, d1, d2, f, f1, set);
    }

    @Override
    public void teleport(double d0, double d1, double d2, float f, float f1, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, PlayerTeleportEvent.TeleportCause cause) {
        oldListener.teleport(d0, d1, d2, f, f1, set, cause);
    }

    @Override
    public boolean teleport(double d0, double d1, double d2, float f, float f1, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, boolean flag, PlayerTeleportEvent.TeleportCause cause) {
        return oldListener.teleport(d0, d1, d2, f, f1, set, flag, cause);
    }

    @Override
    public void teleport(Location dest) {
        oldListener.teleport(dest);
    }

    @Override
    public void chat(String s, boolean async) {
        oldListener.chat(s, async);
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
    public void onDisconnect(Component ichatbasecomponent) {
        oldListener.onDisconnect(ichatbasecomponent);
    }

    @Override
    public void send(Packet<?> packet) {
        oldListener.send(packet);
    }

    @Override
    public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        oldListener.send(packet, genericfuturelistener);
    }

    public void handlePacketIn(Packet<ServerGamePacketListener> packet) {
        denizenNetworkManager.packetsReceived++;
        if (NMSHandler.debugPackets) {
            DenizenNetworkManagerImpl.doPacketOutput("Packet: " + packet.getClass().getCanonicalName() + " sent from " + player.getScoreboardName());
        }
    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePlayerInput(packet);
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {
        handlePacketIn(packet);
        oldListener.handleMoveVehicle(packet);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {
        handlePacketIn(packet);
        oldListener.handleAcceptTeleportPacket(packet);
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {
        handlePacketIn(packet);
        oldListener.handleRecipeBookSeenRecipePacket(packet);
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {
        handlePacketIn(packet);
        oldListener.handleRecipeBookChangeSettingsPacket(packet);
    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSeenAdvancements(packet);
    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {
        handlePacketIn(packet);
        oldListener.handleCustomCommandSuggestions(packet);
    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetCommandBlock(packet);
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetCommandMinecart(packet);
    }

    @Override
    public void handlePickItem(ServerboundPickItemPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePickItem(packet);
    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket packet) {
        handlePacketIn(packet);
        oldListener.handleRenameItem(packet);
    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetBeaconPacket(packet);
    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetStructureBlock(packet);
    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetJigsawBlock(packet);
    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {
        handlePacketIn(packet);
        oldListener.handleJigsawGenerate(packet);
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket packet) {
        handlePacketIn(packet);
        oldListener.handleSelectTrade(packet);
    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket packet) {
        handlePacketIn(packet);
        oldListener.handleEditBook(packet);
    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQuery packet) {
        handlePacketIn(packet);
        oldListener.handleEntityTagQuery(packet);
    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) {
        handlePacketIn(packet);
        oldListener.handleBlockEntityTagQuery(packet);
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packet) {
        handlePacketIn(packet);
        oldListener.handleMovePlayer(packet);
    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePlayerAction(packet);
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket packet) {
        handlePacketIn(packet);
        oldListener.handleUseItemOn(packet);
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket packet) {
        handlePacketIn(packet);
        oldListener.handleUseItem(packet);
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {
        handlePacketIn(packet);
        oldListener.handleTeleportToEntityPacket(packet);
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {
        handlePacketIn(packet);
        oldListener.handleResourcePackResponse(packet);
    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePaddleBoat(packet);
    }

    @Override
    public void handlePong(ServerboundPongPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePong(packet);
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetCarriedItem(packet);
    }

    @Override
    public void handleChat(ServerboundChatPacket packet) {
        handlePacketIn(packet);
        oldListener.handleChat(packet);
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket packet) {
        handlePacketIn(packet);
        oldListener.handleAnimate(packet);
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePlayerCommand(packet);
    }

    @Override
    public void handleInteract(ServerboundInteractPacket packet) {
        handlePacketIn(packet);
        oldListener.handleInteract(packet);
    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket packet) {
        handlePacketIn(packet);
        oldListener.handleClientCommand(packet);
    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket packet) {
        handlePacketIn(packet);
        oldListener.handleContainerClose(packet);
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {
        handlePacketIn(packet);
        oldListener.handleContainerClick(packet);
    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {
        handlePacketIn(packet);
        oldListener.handlePlaceRecipe(packet);
    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {
        handlePacketIn(packet);
        oldListener.handleContainerButtonClick(packet);
    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {
        handlePacketIn(packet);
        oldListener.handleSetCreativeModeSlot(packet);
    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {
        handlePacketIn(packet);
        oldListener.handleSignUpdate(packet);
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket packet) {
        handlePacketIn(packet);
        oldListener.handleKeepAlive(packet);
    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {
        handlePacketIn(packet);
        oldListener.handlePlayerAbilities(packet);
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packet) {
        handlePacketIn(packet);
        oldListener.handleClientInformation(packet);
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        handlePacketIn(packet);
        oldListener.handleCustomPayload(packet);
    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {
        handlePacketIn(packet);
        oldListener.handleChangeDifficulty(packet);
    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {
        handlePacketIn(packet);
        oldListener.handleLockDifficulty(packet);
    }
}
